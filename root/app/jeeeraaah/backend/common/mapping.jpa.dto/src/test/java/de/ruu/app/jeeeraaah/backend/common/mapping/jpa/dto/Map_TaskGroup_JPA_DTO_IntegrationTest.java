package de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.jupiter.api.Test;

import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskGroupJPA;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskJPA;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTO;
import de.ruu.lib.junit.DisabledOnServerNotListening;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceUtil;

/**
 * Integration tests for TaskGroup mapping with JPA EntityManager. These tests verify that relational mappings work
 * correctly with lazy loading. Tests are disabled if PostgreSQL is not available.
 */
@DisabledOnServerNotListening(propertyNameHost = "database.host", propertyNamePort = "database.port")
public class Map_TaskGroup_JPA_DTO_IntegrationTest extends AbstractJPATest
{
	@Test
	void afterMapping_mapsTasks_whenLoadedAndPresent()
	{
		// arrange: persist entities first
		TaskGroupJPA group = persistAndFlush(new TaskGroupJPA("test group"));
		TaskJPA task1 = new TaskJPA(group, "task 1");
		TaskJPA task2 = new TaskJPA(group, "task 2");
		persistAndFlush(task1);
		persistAndFlush(task2);

		// Clear persistence context to ensure lazy loading
		clearPersistenceContext();

		// Reload group with tasks eagerly loaded
		TaskGroupJPA reloadedGroup = find(TaskGroupJPA.class, group.id());
		// Force loading of tasks
		reloadedGroup.tasks().ifPresent(tasks -> tasks.size());

		// Verify tasks are loaded
		PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
		assertThat("tasks should be loaded", persistenceUtil.isLoaded(reloadedGroup, "tasks"), is(true));

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskGroupDTO dto = Map_TaskGroup_JPA_DTO.INSTANCE.map(reloadedGroup, context);

		// assert: tasks should be mapped into context
		TaskDTO task1FromContext = context.get(task1, TaskDTO.class);
		TaskDTO task2FromContext = context.get(task2, TaskDTO.class);
		assertThat("task1 should be in context", task1FromContext, notNullValue());
		assertThat("task2 should be in context", task2FromContext, notNullValue());

		// assert: tasks are in DTO (tasks are automatically added to group via TaskDTO constructor)
		assertThat("dto should have tasks", dto.tasks().isPresent(), is(true));
		assertThat("dto should have 2 tasks", dto.tasks().get().size(), is(2));
	}

	@Test
	void afterMapping_skipsUnloadedTasks()
	{
		// arrange: persist entities
		TaskGroupJPA group = persistAndFlush(new TaskGroupJPA("test group"));
		TaskJPA task1 = new TaskJPA(group, "task 1");
		TaskJPA task2 = new TaskJPA(group, "task 2");
		persistAndFlush(task1);
		persistAndFlush(task2);

		// Clear persistence context - tasks will be lazy/unloaded
		clearPersistenceContext();
		TaskGroupJPA reloadedGroup = find(TaskGroupJPA.class, group.id());

		// Do NOT force loading of tasks - they should remain unloaded
		PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
		assertThat("tasks should NOT be loaded", persistenceUtil.isLoaded(reloadedGroup, "tasks"), is(false));

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskGroupDTO dto = Map_TaskGroup_JPA_DTO.INSTANCE.map(reloadedGroup, context);

		// assert: tasks should NOT be mapped (they were not loaded)
		assertThat("unloaded tasks should not be mapped", dto.tasks().isPresent(), is(false));

		// assert: tasks should NOT be in context (were not mapped because collection was not loaded)
		TaskDTO task1FromContext = context.get(task1, TaskDTO.class);
		TaskDTO task2FromContext = context.get(task2, TaskDTO.class);
		assertThat("unloaded task1 should not be in context", task1FromContext, is(nullValue()));
		assertThat("unloaded task2 should not be in context", task2FromContext, is(nullValue()));
	}

	@Test
	void afterMapping_skipsRemapping_whenTaskAlreadyInContext()
	{
		// arrange: persist entities
		TaskGroupJPA group = persistAndFlush(new TaskGroupJPA("test group"));
		TaskJPA task = new TaskJPA(group, "task");
		persistAndFlush(task);

		// Clear and reload
		clearPersistenceContext();
		TaskGroupJPA reloadedGroup = find(TaskGroupJPA.class, group.id());
		TaskJPA reloadedTask = find(TaskJPA.class, task.id());

		// Force load tasks collection
		reloadedGroup.tasks().ifPresent(tasks -> tasks.size());

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// pre-map task into context
		TaskDTO preMappedTask = Map_Task_JPA_DTO.INSTANCE.map(reloadedTask, context);
		assertThat(preMappedTask, notNullValue());

		// act: mapping the group should reuse the existing task mapping
		TaskGroupDTO dto = Map_TaskGroup_JPA_DTO.INSTANCE.map(reloadedGroup, context);

		// assert: task mapping still the same instance in context
		TaskDTO taskFromContext = context.get(task, TaskDTO.class);
		assertThat("task should be same instance in context", taskFromContext, sameInstance(preMappedTask));

		// assert: task is in DTO (added via TaskDTO constructor when first mapped)
		assertThat("dto should have tasks", dto.tasks().isPresent(), is(true));
	}

	@Test
	void afterMapping_mapsMultipleTasks_andAllArePersisted()
	{
		// arrange: persist entities
		TaskGroupJPA group = persistAndFlush(new TaskGroupJPA("test group"));
		TaskJPA task1 = new TaskJPA(group, "task 1");
		TaskJPA task2 = new TaskJPA(group, "task 2");
		TaskJPA task3 = new TaskJPA(group, "task 3");
		persistAndFlush(task1);
		persistAndFlush(task2);
		persistAndFlush(task3);

		// Clear and reload with tasks loaded
		clearPersistenceContext();
		TaskGroupJPA reloadedGroup = find(TaskGroupJPA.class, group.id());
		// Force loading of tasks
		reloadedGroup.tasks().ifPresent(tasks -> tasks.size());

		// Verify tasks are loaded
		PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
		assertThat("tasks should be loaded", persistenceUtil.isLoaded(reloadedGroup, "tasks"), is(true));

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskGroupDTO dto = Map_TaskGroup_JPA_DTO.INSTANCE.map(reloadedGroup, context);

		// assert: all tasks should be mapped into context
		assertThat(context.get(task1, TaskDTO.class), notNullValue());
		assertThat(context.get(task2, TaskDTO.class), notNullValue());
		assertThat(context.get(task3, TaskDTO.class), notNullValue());

		// assert: all tasks are in DTO
		assertThat(dto.tasks().isPresent(), is(true));
		assertThat(dto.tasks().get().size(), is(3));
	}

	@Test
	void afterMapping_handlesDescription_whenPersisted()
	{
		// arrange: persist group with description
		TaskGroupJPA group = new TaskGroupJPA("test group");
		group.description("A detailed description");
		persistAndFlush(group);

		// Clear and reload
		clearPersistenceContext();
		TaskGroupJPA reloadedGroup = find(TaskGroupJPA.class, group.id());

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskGroupDTO dto = Map_TaskGroup_JPA_DTO.INSTANCE.map(reloadedGroup, context);

		// assert: description should be mapped correctly
		assertThat(dto.description().isPresent(), is(true));
		assertThat(dto.description().get(), is("A detailed description"));
	}

	@Test
	void afterMapping_handlesGroupWithIdAndVersion()
	{
		// arrange: persist group
		TaskGroupJPA group = new TaskGroupJPA("test group");
		persistAndFlush(group);

		// Clear and reload
		clearPersistenceContext();
		TaskGroupJPA reloadedGroup = find(TaskGroupJPA.class, group.id());

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskGroupDTO dto = Map_TaskGroup_JPA_DTO.INSTANCE.map(reloadedGroup, context);

		// assert: id and version should be copied from JPA entity (via TaskGroupDTO constructor)
		assertThat("id should be copied", dto.id(), is(reloadedGroup.id()));
		assertThat("version should be copied", dto.version(), is(reloadedGroup.version()));
		assertThat("name should be copied", dto.name(), is("test group"));
	}
}
