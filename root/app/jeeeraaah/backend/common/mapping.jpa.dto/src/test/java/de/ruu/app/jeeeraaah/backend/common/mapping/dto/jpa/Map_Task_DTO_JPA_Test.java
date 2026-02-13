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
 * Unit tests for {@link Map_Task_DTO_JPA} mapper. These tests verify basic mapping functionality without database.
 */
public class Map_Task_DTO_JPA_Test
{
	@Test
	void map_usesObjectFactory_andCopiesBasicFields()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		TaskGroupDTO groupDTO = new TaskGroupDTO(group);
		TaskDTO dto = new TaskDTO(groupDTO, "test task");
		dto.description("test description");
		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskJPA jpa = Map_Task_DTO_JPA.INSTANCE.map(dto, context);

		// assert: basic fields are copied
		assertThat(jpa, notNullValue());
		assertThat(jpa.name(), is("test task"));
		assertThat(jpa.description().isPresent(), is(true));
		assertThat(jpa.description().get(), is("test description"));

		// assert: task is added to context
		TaskJPA jpaFromContext = context.get(dto, TaskJPA.class);
		assertThat(jpaFromContext, notNullValue());
	}

	@Test
	void afterMapping_mapsSuperTask_whenPresent()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		TaskGroupDTO groupDTO = new TaskGroupDTO(group);
		TaskDTO superTaskDTO = new TaskDTO(groupDTO, "super task");
		TaskDTO dto = new TaskDTO(groupDTO, "test task");
		dto.superTask(superTaskDTO);

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskJPA jpa = Map_Task_DTO_JPA.INSTANCE.map(dto, context);

		// assert: super task should be mapped and in context
		assertThat(jpa.superTask().isPresent(), is(true));
		assertThat(jpa.superTask().get().name(), is("super task"));
		assertThat(context.get(superTaskDTO, TaskJPA.class), notNullValue());
	}

	@Test
	void afterMapping_mapsSubTasks_whenPresent()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		TaskGroupDTO groupDTO = new TaskGroupDTO(group);
		TaskDTO dto = new TaskDTO(groupDTO, "test task");
		TaskDTO subTask1 = new TaskDTO(groupDTO, "sub task 1");
		TaskDTO subTask2 = new TaskDTO(groupDTO, "sub task 2");
		subTask1.superTask(dto);
		subTask2.superTask(dto);

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskJPA jpa = Map_Task_DTO_JPA.INSTANCE.map(dto, context);

		// assert: sub tasks should be mapped and in context
		assertThat(jpa.subTasks().isPresent(), is(true));
		assertThat(jpa.subTasks().get().size(), is(2));
		assertThat(context.get(subTask1, TaskJPA.class), notNullValue());
		assertThat(context.get(subTask2, TaskJPA.class), notNullValue());
	}

	@Test
	void afterMapping_skipsSuperTask_whenNull()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		TaskGroupDTO groupDTO = new TaskGroupDTO(group);
		TaskDTO dto = new TaskDTO(groupDTO, "test task");
		// no super task set

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskJPA jpa = Map_Task_DTO_JPA.INSTANCE.map(dto, context);

		// assert: no super task
		assertThat(jpa.superTask().isPresent(), is(false));
	}

	@Test
	void afterMapping_skipsRemapping_whenRelatedTaskAlreadyInContext()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		TaskGroupDTO groupDTO = new TaskGroupDTO(group);
		TaskDTO superTaskDTO = new TaskDTO(groupDTO, "super task");
		TaskDTO dto = new TaskDTO(groupDTO, "test task");
		dto.superTask(superTaskDTO);

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// pre-map super task
		TaskJPA preMappedSuperTask = Map_Task_DTO_JPA.INSTANCE.map(superTaskDTO, context);

		// act: map main task, should reuse pre-mapped super task
		TaskJPA jpa = Map_Task_DTO_JPA.INSTANCE.map(dto, context);

		// assert: super task should be the same instance from context
		TaskJPA superTaskFromContext = context.get(superTaskDTO, TaskJPA.class);
		assertThat(superTaskFromContext, sameInstance(preMappedSuperTask));
		assertThat(jpa.superTask().get(), sameInstance(preMappedSuperTask));
	}

	@Test
	void map_handlesDescription_whenNull()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		TaskGroupDTO groupDTO = new TaskGroupDTO(group);
		TaskDTO dto = new TaskDTO(groupDTO, "test task");
		// no description set

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskJPA jpa = Map_Task_DTO_JPA.INSTANCE.map(dto, context);

		// assert
		assertThat(jpa.name(), is("test task"));
		assertThat(jpa.description().isPresent(), is(false));
	}

	@Test
	void objectFactory_createsCorrectJPAType()
	{
		// arrange
		TaskGroupJPA group = new TaskGroupJPA("test group");
		TaskGroupDTO groupDTO = new TaskGroupDTO(group);
		TaskDTO dto = new TaskDTO(groupDTO, "test task");
		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskJPA jpa = Map_Task_DTO_JPA.INSTANCE.map(dto, context);

		// assert: correct type is created
		assertThat(jpa, notNullValue());
		assertThat(jpa.getClass().getSimpleName(), is("TaskJPA"));
	}
}
