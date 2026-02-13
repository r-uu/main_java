package de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskGroupJPA;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskJPA;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTO;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;

public class Map_Task_JPA_DTO_Test
{
	@Test
	void map_usesObjectFactory_andCopiesBasicFields()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		String taskName = "task A";
		TaskJPA task = new TaskJPA(group, taskName);
		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskDTO dto = Map_Task_JPA_DTO.INSTANCE.map(task, context);

		// assert
		assertThat(dto, notNullValue());
		assertThat(dto.id(), nullValue());
		assertThat(dto.version(), nullValue());
		assertThat(dto.name(), is(taskName));
		assertThat("taskGroup should be set via ObjectFactory", dto.taskGroup(), notNullValue());
		assertThat(dto.taskGroup().name(), is(group.name()));
	}

	@Test
	void map_handlesDescription_whenPresent()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		String description = "A detailed task description";
		TaskJPA task = new TaskJPA(group, "task with description");
		task.description(description);
		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskDTO dto = Map_Task_JPA_DTO.INSTANCE.map(task, context);

		// assert
		assertThat(dto.description(), is(Optional.of(description)));
	}

	@Test
	void map_handlesDescription_whenNull()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		TaskJPA task = new TaskJPA(group, "task without description");
		task.description(null);
		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskDTO dto = Map_Task_JPA_DTO.INSTANCE.map(task, context);

		// assert
		assertThat(dto.description(), is(Optional.empty()));
	}

	@Test
	void map_addsTaskToContext()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		TaskJPA task = new TaskJPA(group, "context test task");
		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskDTO dto = Map_Task_JPA_DTO.INSTANCE.map(task, context);

		// assert: task should be registered in context after mapping
		TaskDTO fromContext = context.get(task, TaskDTO.class);
		assertThat(fromContext, notNullValue());
		assertThat(fromContext, sameInstance(dto));
	}

	@Test
	void map_addsTaskGroupToContext_whenNotAlreadyPresent()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		TaskJPA task = new TaskJPA(group, "task");
		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskDTO dto = Map_Task_JPA_DTO.INSTANCE.map(task, context);

		// assert: group should be registered in context by ObjectFactory
		TaskGroupDTO groupFromContext = context.get(group, TaskGroupDTO.class);
		assertThat(groupFromContext, notNullValue());
		assertThat(groupFromContext, sameInstance(dto.taskGroup()));
	}

	@Test
	void map_reusesTaskGroupFromContext_whenAlreadyPresent()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		TaskJPA task = new TaskJPA(group, "task");
		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// pre-map group into context
		TaskGroupDTO preMappedGroup = Map_TaskGroup_JPA_DTO.INSTANCE.map(group, context);
		assertThat(preMappedGroup, notNullValue());

		// act: mapping the task should reuse the existing group from context
		TaskDTO dto = Map_Task_JPA_DTO.INSTANCE.map(task, context);

		// assert: same group instance should be reused
		assertThat(dto.taskGroup(), sameInstance(preMappedGroup));
		TaskGroupDTO groupFromContext = context.get(group, TaskGroupDTO.class);
		assertThat(groupFromContext, sameInstance(preMappedGroup));
	}

	// Note: Tests for afterMapping with relational mappings (superTask, subTasks, predecessors, successors)
	// are not included here because they require a JPA persistence context to work with PersistenceUtil.isLoaded().
	// The afterMapping logic checks if lazy-loaded collections are initialized before mapping them,
	// which cannot be tested in a unit test environment without an EntityManager. @Test
	void map_handlesEmptyCollections()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		TaskJPA task = new TaskJPA(group, "task with no relations");
		// TaskJPA constructor doesn't initialize collections - they remain null (lazy loading)
		assertThat(task.subTasks().isPresent(), is(false));
		assertThat(task.predecessors().isPresent(), is(false));
		assertThat(task.successors().isPresent(), is(false));
		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskDTO dto = Map_Task_JPA_DTO.INSTANCE.map(task, context);

		// assert: collections should remain uninitialized in DTO when source is null
		assertThat(dto, notNullValue());
		assertThat(dto.subTasks().isPresent(), is(false));
		assertThat(dto.predecessors().isPresent(), is(false));
		assertThat(dto.successors().isPresent(), is(false));
	}

	@Test
	void objectFactory_createsCorrectDTOType()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		TaskJPA task = new TaskJPA(group, "factory test");
		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskDTO dto = Map_Task_JPA_DTO.INSTANCE.map(task, context);

		// assert: verify correct DTO class is created
		assertThat(dto, notNullValue());
		assertThat("should create TaskDTO instance", dto instanceof TaskDTO, is(true));
	}

}
