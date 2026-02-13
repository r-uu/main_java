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
import de.ruu.lib.junit.DisabledOnServerNotListening;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceUtil;

/**
 * Integration tests for Task mapping with JPA EntityManager. These tests verify that relational mappings work correctly
 * with lazy loading. Tests are disabled if PostgreSQL is not available.
 */
@DisabledOnServerNotListening(propertyNameHost = "database.host", propertyNamePort = "database.port")
public class Map_Task_JPA_DTO_IntegrationTest extends AbstractJPATest
{
	@Test
	void afterMapping_mapsSuperTask_whenLoadedAndPresent()
	{
		// arrange: persist entities first
		TaskGroupJPA group = persistAndFlush(new TaskGroupJPA("test group"));
		TaskJPA superTask = new TaskJPA(group, "super task");
		TaskJPA subTask = new TaskJPA(group, "sub task");
		subTask.superTask(superTask);
		persistAndFlush(superTask);
		persistAndFlush(subTask);

		// Clear persistence context to ensure lazy loading
		clearPersistenceContext();

		// Reload subTask with superTask eagerly loaded
		TaskJPA reloadedSubTask = find(TaskJPA.class, subTask.id());
		// Force loading of superTask
		reloadedSubTask.superTask().ifPresent(st -> st.name());

		// Verify superTask is loaded
		PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
		assertThat("superTask should be loaded", persistenceUtil.isLoaded(reloadedSubTask, "superTask"), is(true));

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskDTO dto = Map_Task_JPA_DTO.INSTANCE.map(reloadedSubTask, context);

		// assert: superTask should be mapped
		assertThat(dto.superTask().isPresent(), is(true));
		TaskDTO mappedSuperTask = dto.superTask().get();
		assertThat(mappedSuperTask, notNullValue());
		assertThat(mappedSuperTask.name(), is("super task"));

		// assert: superTask should be in context
		TaskDTO superFromContext = context.get(superTask, TaskDTO.class);
		assertThat(superFromContext, notNullValue());
		assertThat(superFromContext, sameInstance(mappedSuperTask));
	}

	@Test
	void afterMapping_mapsSubTasks_whenLoadedAndPresent()
	{
		// arrange: persist entities
		TaskGroupJPA group = persistAndFlush(new TaskGroupJPA("test group"));
		TaskJPA superTask = new TaskJPA(group, "super task");
		TaskJPA subTask1 = new TaskJPA(group, "sub task 1");
		TaskJPA subTask2 = new TaskJPA(group, "sub task 2");
		superTask.addSubTask(subTask1);
		superTask.addSubTask(subTask2);
		persistAndFlush(superTask);
		persistAndFlush(subTask1);
		persistAndFlush(subTask2);

		// Clear and reload with subTasks loaded
		clearPersistenceContext();
		TaskJPA reloadedSuperTask = find(TaskJPA.class, superTask.id());
		// Force loading of subTasks
		reloadedSuperTask.subTasks().ifPresent(st -> st.size());

		// Verify subTasks are loaded
		PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
		assertThat("subTasks should be loaded", persistenceUtil.isLoaded(reloadedSuperTask, "subTasks"), is(true));

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskDTO dto = Map_Task_JPA_DTO.INSTANCE.map(reloadedSuperTask, context);

		// assert: subTasks should be mapped
		assertThat(dto.subTasks().isPresent(), is(true));
		assertThat(dto.subTasks().get().size(), is(2));

		// assert: subTasks should be in context
		TaskDTO sub1FromContext = context.get(subTask1, TaskDTO.class);
		TaskDTO sub2FromContext = context.get(subTask2, TaskDTO.class);
		assertThat(sub1FromContext, notNullValue());
		assertThat(sub2FromContext, notNullValue());
	}

	@Test
	void afterMapping_mapsPredecessors_whenLoadedAndPresent()
	{
		// arrange: persist entities
		TaskGroupJPA group = persistAndFlush(new TaskGroupJPA("test group"));
		TaskJPA predecessor1 = new TaskJPA(group, "predecessor 1");
		TaskJPA predecessor2 = new TaskJPA(group, "predecessor 2");
		TaskJPA task = new TaskJPA(group, "task");
		task.addPredecessor(predecessor1);
		task.addPredecessor(predecessor2);
		persistAndFlush(predecessor1);
		persistAndFlush(predecessor2);
		persistAndFlush(task);

		// Clear and reload with predecessors loaded
		clearPersistenceContext();
		TaskJPA reloadedTask = find(TaskJPA.class, task.id());
		// Force loading of predecessors
		reloadedTask.predecessors().ifPresent(p -> p.size());

		// Verify predecessors are loaded
		PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
		assertThat("predecessors should be loaded", persistenceUtil.isLoaded(reloadedTask, "predecessors"), is(true));

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskDTO dto = Map_Task_JPA_DTO.INSTANCE.map(reloadedTask, context);

		// assert: predecessors should be mapped
		assertThat(dto.predecessors().isPresent(), is(true));
		assertThat(dto.predecessors().get().size(), is(2));

		// assert: predecessors should be in context
		TaskDTO pred1FromContext = context.get(predecessor1, TaskDTO.class);
		TaskDTO pred2FromContext = context.get(predecessor2, TaskDTO.class);
		assertThat(pred1FromContext, notNullValue());
		assertThat(pred2FromContext, notNullValue());
	}

	@Test
	void afterMapping_mapsSuccessors_whenLoadedAndPresent()
	{
		// arrange: persist entities
		TaskGroupJPA group = persistAndFlush(new TaskGroupJPA("test group"));
		TaskJPA successor1 = new TaskJPA(group, "successor 1");
		TaskJPA successor2 = new TaskJPA(group, "successor 2");
		TaskJPA task = new TaskJPA(group, "task");
		task.addSuccessor(successor1);
		task.addSuccessor(successor2);
		persistAndFlush(successor1);
		persistAndFlush(successor2);
		persistAndFlush(task);

		// Clear and reload with successors loaded
		clearPersistenceContext();
		TaskJPA reloadedTask = find(TaskJPA.class, task.id());
		// Force loading of successors
		reloadedTask.successors().ifPresent(s -> s.size());

		// Verify successors are loaded
		PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
		assertThat("successors should be loaded", persistenceUtil.isLoaded(reloadedTask, "successors"), is(true));

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskDTO dto = Map_Task_JPA_DTO.INSTANCE.map(reloadedTask, context);

		// assert: successors should be mapped
		assertThat(dto.successors().isPresent(), is(true));
		assertThat(dto.successors().get().size(), is(2));

		// assert: successors should be in context
		TaskDTO succ1FromContext = context.get(successor1, TaskDTO.class);
		TaskDTO succ2FromContext = context.get(successor2, TaskDTO.class);
		assertThat(succ1FromContext, notNullValue());
		assertThat(succ2FromContext, notNullValue());
	}

	@Test
	void afterMapping_skipsUnloadedCollections()
	{
		// arrange: persist entities
		TaskGroupJPA group = persistAndFlush(new TaskGroupJPA("test group"));
		TaskJPA superTask = new TaskJPA(group, "super task");
		TaskJPA subTask = new TaskJPA(group, "sub task");
		superTask.addSubTask(subTask);
		persistAndFlush(superTask);
		persistAndFlush(subTask);

		// Clear persistence context - collections will be lazy/unloaded
		clearPersistenceContext();
		TaskJPA reloadedSuperTask = find(TaskJPA.class, superTask.id());

		// Do NOT force loading of subTasks - they should remain unloaded
		PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
		assertThat("subTasks should NOT be loaded", persistenceUtil.isLoaded(reloadedSuperTask, "subTasks"), is(false));

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// act
		TaskDTO dto = Map_Task_JPA_DTO.INSTANCE.map(reloadedSuperTask, context);

		// assert: subTasks should NOT be mapped (they were not loaded)
		assertThat("unloaded subTasks should not be mapped", dto.subTasks().isPresent(), is(false));

		// assert: subTask should NOT be in context (was not mapped because collection was not loaded)
		TaskDTO subFromContext = context.get(subTask, TaskDTO.class);
		assertThat("unloaded subTask should not be in context", subFromContext, is(nullValue()));
	}

	@Test
	void afterMapping_skipsRemapping_whenRelatedTaskAlreadyInContext()
	{
		// arrange: persist entities
		TaskGroupJPA group = persistAndFlush(new TaskGroupJPA("test group"));
		TaskJPA superTask = new TaskJPA(group, "super task");
		TaskJPA subTask = new TaskJPA(group, "sub task");
		subTask.superTask(superTask);
		persistAndFlush(superTask);
		persistAndFlush(subTask);

		// Clear and reload
		clearPersistenceContext();
		TaskJPA reloadedSuperTask = find(TaskJPA.class, superTask.id());
		TaskJPA reloadedSubTask = find(TaskJPA.class, subTask.id());

		// Force load superTask reference
		reloadedSubTask.superTask().ifPresent(st -> st.name());

		ReferenceCycleTracking context = new ReferenceCycleTracking();

		// pre-map superTask into context
		TaskDTO preMappedSuper = Map_Task_JPA_DTO.INSTANCE.map(reloadedSuperTask, context);
		assertThat(preMappedSuper, notNullValue());

		// act: mapping the subTask should reuse the existing superTask mapping
		TaskDTO dto = Map_Task_JPA_DTO.INSTANCE.map(reloadedSubTask, context);

		// assert: superTask mapping still the same instance in context
		TaskDTO superFromContext = context.get(superTask, TaskDTO.class);
		assertThat(superFromContext, sameInstance(preMappedSuper));
		assertThat(dto.superTask().get(), sameInstance(preMappedSuper));
	}
}
