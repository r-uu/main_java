package de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.dbcommand;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.domain.RemoveNeighboursFromTaskConfig;
import de.ruu.app.jeeeraaah.common.api.domain.flat.TaskGroupFlat;
import de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.TaskGroupServiceClient;
import de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.TaskServiceClient;
import de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.auth.KeycloakAuthService;
import de.ruu.lib.cdi.se.CDIContainer;
import de.ruu.lib.jpa.core.Entity;
import de.ruu.lib.ws_rs.NonTechnicalException;
import de.ruu.lib.ws_rs.TechnicalException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

@Slf4j
@Dependent
public class DBClean
{
	@Inject private TaskGroupServiceClient taskGroupServiceClient;
	@Inject private TaskServiceClient      taskServiceClient;
	@Inject private KeycloakAuthService    authService;

	@PostConstruct private void postConstruct()
	{
		log.debug("taskGroupServiceClient available: {}", nonNull(taskGroupServiceClient));
		log.debug("taskServiceClient      available: {}", nonNull(taskServiceClient));
		log.debug("authService            available: {}", nonNull(authService));
	}

	public void run() throws NonTechnicalException, TechnicalException
	{
		// Check if testing mode is enabled and perform auto-login if needed
		boolean testingMode =
				ConfigProvider
						.getConfig()
						.getOptionalValue("testing", Boolean.class)
						.orElse(false);

		log.info("=== DBClean Testing Mode Status ===");
		log.info("  testing property: {}", testingMode);
		log.info("  isLoggedIn: {}", authService.isLoggedIn());

		if (testingMode && !authService.isLoggedIn())
		{
		log.info("=== Testing mode enabled - performing automatic login ===");

		String testUsername = ConfigProvider.getConfig()
				.getOptionalValue("keycloak.test.user", String.class)
				.orElse(null);
		String testPassword = ConfigProvider.getConfig()
				.getOptionalValue("keycloak.test.password", String.class)
				.orElse(null);

		if (testUsername != null && testPassword != null)
		{
			log.info("  Test credentials found: username={}", testUsername);
			try
			{
				authService.login(testUsername, testPassword);
				log.info("  ✅ Automatic login successful");
				log.info("  Access token (first 50 chars): {}...",
						authService.getAccessToken().substring(0, Math.min(50, authService.getAccessToken().length())));
			}
			catch (Exception e)
			{
				log.error("""
						❌ Automatic login failed: {}
						  Please ensure:
						    - Keycloak server is running (docker ps | grep keycloak)
						    - Credentials in microprofile-config.properties are correct
						    - Direct Access Grants are enabled for the client""",
						e.getMessage(), e);
				log.error("    - Credentials in microprofile-config.properties are correct (keycloak.test.user, keycloak.test.password)");
				log.error("    - Direct Access Grants are enabled for the client");
				throw new TechnicalException("Automatic login in testing mode failed", e);
			}
		}
		else
		{
			String msg = "Testing mode enabled but credentials missing (expected: keycloak.test.user, keycloak.test.password)";
			log.error(msg);
			throw new TechnicalException(msg);
		}
	}
	else if (!authService.isLoggedIn())
		{
			String msg = "Not logged in and testing mode is disabled. Manual login required.";
			log.error(msg);
			throw new TechnicalException(msg);
		}

		cleanTaskGroups();
	}

	private void cleanTaskGroups() throws NonTechnicalException, TechnicalException
	{
		Set<TaskGroupFlat> groups = taskGroupServiceClient.findAllFlat();

		log.debug("task group count before clean db {}", groups.size());
		for (TaskGroupFlat group : groups) { cleanTasksOfGroup(group); }
		log.debug("task group count after  clean db {}", taskGroupServiceClient.findAllFlat().size());
	}

	private void cleanTasksOfGroup(Entity<Long> group) throws NonTechnicalException, TechnicalException
	{
		Long taskGroupId = requireNonNull(group.id(), "task group id must not be null, persist task group to retrieve id");

		// get task group with tasks from backend
		Optional<TaskGroupBean> optionalTaskGroup = taskGroupServiceClient.findWithTasksAndDirectNeighbours(taskGroupId);

		if (optionalTaskGroup.isPresent())
		{
			TaskGroupBean taskGroup = optionalTaskGroup.get();

			if (taskGroup.tasks().isPresent())
			{
				Set<TaskBean> tasks = taskGroup.tasks().get();
				cleanTasksOfGroup(tasks);
				log.debug("deleted {} tasks for task group {}", tasks.size(), taskGroup);
			}

			log.debug("deleting task group {}", taskGroup);
			taskGroupServiceClient.delete(taskGroupId);
			log.debug("deleted  task group {}", taskGroup);
		}
	}

	private void cleanTasksOfGroup(Set<TaskBean> tasks) throws NonTechnicalException, TechnicalException
	{
		Set<TaskBean> mainTasks = tasks.stream().filter(t -> t.superTask().isEmpty()).collect(Collectors.toSet());

		for (TaskBean mainTask : mainTasks)
		{
			cleanSuperSubTaskHierarchy(mainTask);
			log.debug("deleted main task {}", mainTask);
		}
	}

	private void cleanSuperSubTaskHierarchy(TaskBean task) throws NonTechnicalException, TechnicalException
	{
		// call this method recursively for all subtasks
		if (task.subTasks().isPresent())
				for (TaskBean subTask : task.subTasks().get()) { cleanSuperSubTaskHierarchy(subTask); }

		// remove all neighbours from task
		RemoveNeighboursFromTaskConfig config = newRemoveNeighboursFromTaskConfig(task);
		taskServiceClient.removeNeighboursFromTask(config);

		// delete task itself
		taskServiceClient.delete(requireNonNull(task.id()));

		log.debug("deleted task {}", task);
	}

	private RemoveNeighboursFromTaskConfig newRemoveNeighboursFromTaskConfig(TaskBean task)
	{
		Set<Long> predecessorIds = new HashSet<>();
		Set<Long> subTaskIds     = new HashSet<>();
		Set<Long> successorIds   = new HashSet<>();

		task.predecessors()
				.ifPresent(predecessors -> predecessors.forEach(predecessor -> predecessorIds.add(predecessor.id())));
		task.subTasks()
				.ifPresent(subTasks     -> subTasks    .forEach(subTask     -> subTaskIds    .add(subTask    .id())));
		task.successors()
				.ifPresent(successors   -> successors  .forEach(successor   -> successorIds  .add(successor  .id())));

		return new RemoveNeighboursFromTaskConfig
		(
				task.id(),
				true,
				predecessorIds,
				subTaskIds,
				successorIds
		);
	}

	static void main(String[] args) throws NonTechnicalException, TechnicalException
	{
		CDIContainer.bootstrap(DBClean.class.getClassLoader());
		DBClean command = CDI.current().select(DBClean.class).get();
		command.run();
	}
}