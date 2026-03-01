package de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.dbcommand;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.TaskGroupServiceClient;
import de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.TaskServiceClient;
import de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.auth.KeycloakAuthService;
import de.ruu.lib.cdi.se.CDIContainer;
import de.ruu.lib.ws_rs.TechnicalException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.concurrent.ThreadLocalRandom;

import static de.ruu.lib.util.BooleanFunctions.not;
import static java.util.Objects.isNull;

@Slf4j
public class DBPopulateTiny
{
	@Inject private TaskGroupServiceClient taskGroupService;
	@Inject private TaskServiceClient      taskService;
	@Inject private KeycloakAuthService    authService;

	@PostConstruct private void postConstruct()
	{
		log.debug("initialised ws.rs-clients successfully: {}", not(isNull(taskGroupService)) && not(isNull(taskService)));
		log.debug("authService available: {}", not(isNull(authService)));
	}

	public void run() throws Exception
	{
		// Check if testing mode is enabled and perform auto-login if needed
		boolean testingMode =
				ConfigProvider
						.getConfig()
						.getOptionalValue("testing", Boolean.class)
						.orElse(false);

		log.info("=== DBPopulateTiny Testing Mode Status ===");
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

		populate();
	}

	private void populate() throws Exception
	{
		TaskGroupBean group = new TaskGroupBean("group." + ThreadLocalRandom.current().nextInt());
		group = taskGroupService.create(group);

		TaskBean main = new TaskBean(group, "main." + ThreadLocalRandom.current().nextInt());
		TaskBean sub  = new TaskBean(group, "sub."  + ThreadLocalRandom.current().nextInt());

		main = taskService.create(main);
		sub  = taskService.create(sub );

		taskService.addSubTask(main, sub);
	}

	public static void main(String[] args) throws Exception
	{
		CDIContainer.bootstrap(DBPopulateTiny.class.getClassLoader());
		DBPopulateTiny populate = CDI.current().select(DBPopulateTiny.class).get();
		populate.run();
	}
}