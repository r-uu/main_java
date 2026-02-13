package de.ruu.app.jeeeraaah.backend.common.mapping.dto.jpa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.jupiter.api.Test;

import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskGroupJPA;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskJPA;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTO;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;

/**
 * Unit tests for {@link Map_TaskGroup_DTO_JPA} mapper. These tests verify basic mapping functionality without database.
 */
public class Map_TaskGroup_DTO_JPA_Test
{
	@Test
	void map_usesObjectFactory_andCopiesBasicFields()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		TaskGroupDTO dto = new TaskGroupDTO(group);
		dto.description("test description");
		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskGroupJPA jpa = Map_TaskGroup_DTO_JPA.INSTANCE.map(dto, context);

		// assert: basic fields are copied
		assertThat(jpa, notNullValue());
		assertThat(jpa.name(), is("test group"));
		assertThat(jpa.description().isPresent(), is(true));
		assertThat(jpa.description().get(), is("test description"));

		// assert: group is added to context
		TaskGroupJPA jpaFromContext = context.get(dto, TaskGroupJPA.class);
		assertThat(jpaFromContext, notNullValue());
	}

	@Test
	void afterMapping_mapsRelatedTasks_intoContext_andAddsToJPA_whenNotAlreadyMapped()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		TaskGroupDTO dto = new TaskGroupDTO(group);
		TaskDTO task1 = new TaskDTO(dto, "task 1");
		TaskDTO task2 = new TaskDTO(dto, "task 2");

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskGroupJPA jpa = Map_TaskGroup_DTO_JPA.INSTANCE.map(dto, context);

		// assert: tasks should be mapped into context
		TaskJPA task1Mapped = context.get(task1, TaskJPA.class);
		TaskJPA task2Mapped = context.get(task2, TaskJPA.class);
		assertThat(task1Mapped, notNullValue());
		assertThat(task2Mapped, notNullValue());

		// assert: tasks are in JPA group
		assertThat(jpa.tasks().isPresent(), is(true));
		assertThat(jpa.tasks().get().size(), is(2));
	}

	@Test
	void afterMapping_skipsRemapping_whenTaskAlreadyInContext()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		TaskGroupDTO dto = new TaskGroupDTO(group);
		TaskDTO taskDTO = new TaskDTO(dto, "task");

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// pre-map task into context
		TaskJPA preMappedTask = Map_Task_DTO_JPA.INSTANCE.map(taskDTO, context);
		assertThat(preMappedTask, notNullValue());

		// act: mapping the group should reuse the existing task mapping
		TaskGroupJPA jpa = Map_TaskGroup_DTO_JPA.INSTANCE.map(dto, context);

		// assert: task mapping still the same instance in context
		TaskJPA taskFromContext = context.get(taskDTO, TaskJPA.class);
		assertThat(taskFromContext, sameInstance(preMappedTask));

		// assert: task is in JPA group
		assertThat(jpa.tasks().isPresent(), is(true));
		assertThat(jpa.tasks().get().contains(preMappedTask), is(true));
	}

	@Test
	void map_handlesDescription_whenPresent()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		TaskGroupDTO dto = new TaskGroupDTO(group);
		dto.description("A detailed description");
		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskGroupJPA jpa = Map_TaskGroup_DTO_JPA.INSTANCE.map(dto, context);

		// assert
		assertThat(jpa.description().isPresent(), is(true));
		assertThat(jpa.description().get(), is("A detailed description"));
	}

	@Test
	void map_handlesDescription_whenNull()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		TaskGroupDTO dto = new TaskGroupDTO(group);
		// no description set
		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskGroupJPA jpa = Map_TaskGroup_DTO_JPA.INSTANCE.map(dto, context);

		// assert
		assertThat(jpa.description().isPresent(), is(false));
	}

	@Test
	void map_addsGroupToContext()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		TaskGroupDTO dto = new TaskGroupDTO(group);
		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskGroupJPA jpa = Map_TaskGroup_DTO_JPA.INSTANCE.map(dto, context);

		// assert: group is registered in context
		TaskGroupJPA fromContext = context.get(dto, TaskGroupJPA.class);
		assertThat(fromContext, notNullValue());
		assertThat(fromContext.name(), is("test group"));
	}

	@Test
	void map_handlesEmptyTasksSet()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		TaskGroupDTO dto = new TaskGroupDTO(group);
		// no tasks added - tasks field will be null in DTO
		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskGroupJPA jpa = Map_TaskGroup_DTO_JPA.INSTANCE.map(dto, context);

		// assert: when DTO has no tasks (null), the JPA entity will have no tasks either
		// (tasks is not mapped because of @AfterMapping condition)
		assertThat("JPA should be created", jpa, notNullValue());
		assertThat("name should be copied", jpa.name(), is("test group"));
	}

	@Test
	void map_handlesMultipleTasks()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		TaskGroupDTO dto = new TaskGroupDTO(group);
		new TaskDTO(dto, "task 1");
		new TaskDTO(dto, "task 2");
		new TaskDTO(dto, "task 3");

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskGroupJPA jpa = Map_TaskGroup_DTO_JPA.INSTANCE.map(dto, context);

		// assert: all tasks are mapped
		assertThat(jpa.tasks().isPresent(), is(true));
		assertThat(jpa.tasks().get().size(), is(3));
	}

	@Test
	void objectFactory_createsCorrectJPAType()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		TaskGroupDTO dto = new TaskGroupDTO(group);
		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskGroupJPA jpa = Map_TaskGroup_DTO_JPA.INSTANCE.map(dto, context);

		// assert: correct type is created
		assertThat(jpa, notNullValue());
		assertThat(jpa.getClass().getSimpleName(), is("TaskGroupJPA"));
	}
}
