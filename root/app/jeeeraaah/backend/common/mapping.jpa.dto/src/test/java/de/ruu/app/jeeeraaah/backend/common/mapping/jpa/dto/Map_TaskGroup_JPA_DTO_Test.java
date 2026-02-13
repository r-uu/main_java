package de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import de.ruu.app.jeeeraaah.backend.persistence.jpa.JPAFactory;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskGroupJPA;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskJPA;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTO;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;

class Map_TaskGroup_JPA_DTO_Test
{
	@Test void map_usesObjectFactory_andCopiesBasicFields()
	{
		// arrange
		String       name  = "group A";
		TaskGroupJPA group = JPAFactory.createTaskGroupJPAWithName(name);

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskGroupDTO dto = Map_TaskGroup_JPA_DTO.INSTANCE.map(group, context);

		// assert
		assertThat(dto          , notNullValue());
		assertThat(dto.id()     , nullValue());
		assertThat(dto.version(), nullValue());
		assertThat(dto.name()   , is(name));
		assertThat("tasks should not be initialized by default", dto.tasks().isPresent(), is(false));
	}

	@Test void afterMapping_mapsRelatedTasks_intoContext_andAddsToDTO_whenNotAlreadyMapped()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("group B");
		TaskJPA      task1 = new TaskJPA(group, "task 1");
		TaskJPA      task2 = new TaskJPA(group, "task 2");

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// precondition
		assertThat(group.tasks().isPresent(), is(true));

		// act
		TaskGroupDTO dto = Map_TaskGroup_JPA_DTO.INSTANCE.map(group, context);

		// assert: tasks should be in context
		assertThat(dto, notNullValue());
		TaskDTO mappedT1 = context.get(task1, TaskDTO.class);
		TaskDTO mappedT2 = context.get(task2, TaskDTO.class);
		assertThat(mappedT1, notNullValue());
		assertThat(mappedT2, notNullValue());

		// assert: tasks are automatically added to DTO via TaskDTO constructor
		assertThat("tasks should be in DTO via constructor", dto.tasks().isPresent(), is(true));
		assertThat(dto.tasks().get().size(), is(2));
		assertThat(dto.tasks().get().contains(mappedT1), is(true));
		assertThat(dto.tasks().get().contains(mappedT2), is(true));
	}

	@Test void afterMapping_skipsRemapping_whenTaskAlreadyInContext()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("group C");
		TaskJPA      task  = new TaskJPA(group, "task 3");

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// pre-map task into context
		TaskDTO preMapped = Map_Task_JPA_DTO.INSTANCE.map(task, context);
		assertThat(preMapped, notNullValue());
		assertThat(context.get(task, TaskDTO.class), sameInstance(preMapped));

		// act: mapping the group should not override the existing mapping for task
		TaskGroupDTO dto = Map_TaskGroup_JPA_DTO.INSTANCE.map(group, context);

		// assert: mapping still the same instance in context
		TaskDTO post = context.get(task, TaskDTO.class);
		assertThat(post, sameInstance(preMapped));

		// assert: task is in DTO because it was added via TaskDTO constructor when first mapped
		assertThat(dto.tasks().isPresent()              , is(true));
		assertThat(dto.tasks().get().size()             , is(1));
		assertThat(dto.tasks().get().contains(preMapped), is(true));
	}

	@Test void map_handlesDescription_whenPresent()
	{
		// arrange
		String       description = "A detailed description";
		TaskGroupJPA group       = new TaskGroupJPA("group with description");
		group.description(description);

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskGroupDTO dto = Map_TaskGroup_JPA_DTO.INSTANCE.map(group, context);

		// assert
		assertThat(dto.description(), is(Optional.of(description)));
	}

	@Test void map_handlesDescription_whenNull()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("group without description");
		group.description(null);

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskGroupDTO dto = Map_TaskGroup_JPA_DTO.INSTANCE.map(group, context);

		// assert
		assertThat(dto.description(), is(Optional.empty()));
	}

	@Test void map_addsGroupToContext()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("group for context test");

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskGroupDTO dto = Map_TaskGroup_JPA_DTO.INSTANCE.map(group, context);

		// assert: group should be registered in context after mapping
		TaskGroupDTO fromContext = context.get(group, TaskGroupDTO.class);
		assertThat(fromContext, notNullValue());
		assertThat(fromContext, sameInstance(dto));
	}

	@Test void map_handlesEmptyTasksSet()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("group with no tasks");
		// TaskGroupJPA constructor doesn't initialize tasks - they remain null (lazy loading)
		assertThat(group.tasks().isPresent(), is(false));

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskGroupDTO dto = Map_TaskGroup_JPA_DTO.INSTANCE.map(group, context);

		// assert: tasks should remain uninitialized in DTO when source tasks are null
		assertThat(dto, notNullValue());
		assertThat("null source tasks should not initialize DTO tasks", dto.tasks().isPresent(), is(false));
	}

	@Test void map_handlesMultipleTasks()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("group with multiple tasks");
		TaskJPA      task1 = new TaskJPA(group, "task 1");
		TaskJPA      task2 = new TaskJPA(group, "task 2");
		TaskJPA      task3 = new TaskJPA(group, "task 3");

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskGroupDTO dto = Map_TaskGroup_JPA_DTO.INSTANCE.map(group, context);

		// assert: all tasks should be mapped into context
		assertThat(context.get(task1, TaskDTO.class), notNullValue());
		assertThat(context.get(task2, TaskDTO.class), notNullValue());
		assertThat(context.get(task3, TaskDTO.class), notNullValue());

		// assert: all tasks are in DTO (added automatically via TaskDTO constructor)
		assertThat(dto.tasks().isPresent(), is(true));
		assertThat(dto.tasks().get().size(), is(3));
	}

	@Test void objectFactory_createsCorrectDTOType()
	{
		// arrange
		TaskGroupJPA group = JPAFactory.createTaskGroupJPAWithName("factory test");

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskGroupDTO dto = Map_TaskGroup_JPA_DTO.INSTANCE.map(group, context);

		// assert: verify correct DTO class is created
		assertThat(dto                                                               , notNullValue());
		assertThat("should create TaskGroupDTO instance", dto instanceof TaskGroupDTO, is(true));
	}
}
