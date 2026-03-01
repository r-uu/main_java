package de.ruu.app.jeeeraaah.frontend.ui.fx.types;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static de.ruu.lib.util.BooleanFunctions.not;

@AllArgsConstructor
@Slf4j
class DataFactory
{
	private int groupCount;
	private int mainTasksPerGroupCount;
	private int subTasksPerMainTaskCount;

	Set<TaskGroupBean> data()
	{
		Set<TaskGroupBean> result = new HashSet<>();
		for (int i = 0; i < groupCount; i++) { result.add(createTaskGroupBean(i)); }
		log.debug("groups.size: {}", result.size());
		return result;
	}

	private TaskGroupBean createTaskGroupBean(int groupId)
	{
		TaskGroupBean result = new TaskGroupBean("group " + groupId + " - " + ThreadLocalRandom.current().nextInt());
		for (int mainTaskId = 0; mainTaskId < mainTasksPerGroupCount; mainTaskId++)
		{
			createMainTaskBean(result, groupId, mainTaskId);
		}
		log.debug("group {}, mainTasks.size: {}", groupId, result.tasks().get().size());
		return result;
	}

	private void createMainTaskBean(TaskGroupBean group, int groupId, int mainTaskId)
	{
		TaskBean mainTask =
				new TaskBean(
						group,
						"main task " + groupId + "." + mainTaskId + " - " + ThreadLocalRandom.current().nextInt());
		for (int subTaskId = 0; subTaskId < subTasksPerMainTaskCount; subTaskId++)
		{
			mainTask.addSubTask(createSubTaskBean(group, groupId, mainTaskId, subTaskId));
		}
//		mainTask.subTasks().ifPresent(c -> createPredecessorsAndSuccessorsAmongChildren(c));
		log.debug("group {}, mainTask {}, mainTask.subTasks.size {}",
				groupId, mainTaskId, mainTask.subTasks().get().size());
	}

	private TaskBean createSubTaskBean(TaskGroupBean group, int groupId, int mainTaskId, int subTaskId)
	{
		return new TaskBean(
				group,
				"sub task " + groupId + "." + mainTaskId + "." + subTaskId + " - " + ThreadLocalRandom.current().nextInt());
	}

	private void createPredecessorsAndSuccessorsAmongChildren(Set<TaskBean> subTasks)
	{
		TaskBean recentPredecessor = null;
		for (TaskBean task : subTasks)
		{
			if (not(recentPredecessor == null)) recentPredecessor.addSuccessor(task);
			recentPredecessor = task;
		}
	}
}