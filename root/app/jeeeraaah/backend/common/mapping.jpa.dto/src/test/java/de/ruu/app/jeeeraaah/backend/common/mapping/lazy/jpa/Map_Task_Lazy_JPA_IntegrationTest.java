package de.ruu.app.jeeeraaah.backend.common.mapping.lazy.jpa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.jupiter.api.Test;

import de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto.AbstractJPATest;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskGroupJPA;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskJPA;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTOLazy;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTOLazy;
import de.ruu.lib.junit.DisabledOnServerNotListening;

/** Integration tests for Map_Task_Lazy_JPA with persisted entities. */
@DisabledOnServerNotListening(propertyNameHost = "database.host", propertyNamePort = "database.port")
class Map_Task_Lazy_JPA_IntegrationTest extends AbstractJPATest
{
	@Test
	void objectFactory_setsGroupAndName()
	{
		// Arrange
		TaskGroupJPA group = persistAndFlush(new TaskGroupJPA("Group Name"));
		TaskGroupDTOLazy lazyGroup = new TaskGroupDTOLazy(group);
		TaskDTOLazy lazyTask = new TaskDTOLazy(lazyGroup, "Task Name");

		// Act - test the ObjectFactory directly
		de.ruu.lib.mapstruct.ReferenceCycleTracking context = new de.ruu.lib.mapstruct.ReferenceCycleTracking();
		TaskJPA result = Map_Task_Lazy_JPA.INSTANCE.create(lazyTask, group, context);

		// Assert - ObjectFactory correctly sets name from lazyTask.name()
		assertThat(result.taskGroup(), sameInstance(group));
		assertThat(result.name(), equalTo("Task Name"));
	}

	@Test
	void map_setsTaskGroup()
	{
		// Arrange
		TaskGroupJPA group = persistAndFlush(new TaskGroupJPA("Group Name"));

		clearPersistenceContext();
		TaskGroupJPA reloadedGroup = find(TaskGroupJPA.class, group.id());

		TaskGroupDTOLazy lazyGroup = new TaskGroupDTOLazy(reloadedGroup);
		TaskDTOLazy lazyTask = new TaskDTOLazy(lazyGroup, "Task Name");

		// Act
		de.ruu.lib.mapstruct.ReferenceCycleTracking context = new de.ruu.lib.mapstruct.ReferenceCycleTracking();
		TaskJPA result = Map_Task_Lazy_JPA.INSTANCE.map(lazyTask, reloadedGroup, context);

		// Assert - taskGroup and name are set correctly
		assertThat(result, notNullValue());
		assertThat(result.taskGroup(), sameInstance(reloadedGroup));
		assertThat(result.name(), equalTo("Task Name"));
	}

	@Test
	void map_multipleTasksShareSameGroup()
	{
		// Arrange
		TaskGroupJPA group = persistAndFlush(new TaskGroupJPA("Group Name"));

		clearPersistenceContext();
		TaskGroupJPA reloadedGroup = find(TaskGroupJPA.class, group.id());

		TaskGroupDTOLazy lazyGroup = new TaskGroupDTOLazy(reloadedGroup);
		TaskDTOLazy lazyTask1 = new TaskDTOLazy(lazyGroup, "Task 1");
		TaskDTOLazy lazyTask2 = new TaskDTOLazy(lazyGroup, "Task 2");

		// Act
		de.ruu.lib.mapstruct.ReferenceCycleTracking context = new de.ruu.lib.mapstruct.ReferenceCycleTracking();
		TaskJPA result1 = Map_Task_Lazy_JPA.INSTANCE.map(lazyTask1, reloadedGroup, context);
		TaskJPA result2 = Map_Task_Lazy_JPA.INSTANCE.map(lazyTask2, reloadedGroup, context);

		// Assert - both tasks reference the same group and have correct names
		assertThat(result1.taskGroup(), sameInstance(result2.taskGroup()));
		assertThat(result1.taskGroup(), sameInstance(reloadedGroup));
		assertThat(result1.name(), equalTo("Task 1"));
		assertThat(result2.name(), equalTo("Task 2"));
	}
}