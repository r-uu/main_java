package de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(fluent = true)
@Dependent
class DataItemFactory
{
	@Inject private TaskFactory taskFactory;

	@NonNull List<TaskTreeTableDataItem> rootItemsInPeriod
			(@NonNull final TaskGroupBean taskGroupBean, @NonNull LocalDate start, @NonNull LocalDate end)
	{
		List<TaskTreeTableDataItem> result = new ArrayList<>();

		for (TaskBean rootTask : taskFactory.rootTasks(taskGroupBean, start, end))
		{
			if (rootTask.start().isPresent() && rootTask.end().isPresent())
			{
				// Task should be displayed if it overlaps with the filter period
				// Overlap exists when: task starts before/on filter end AND task ends after/on filter start
				LocalDate taskStart = rootTask.start().get();
				LocalDate taskEnd = rootTask.end().get();

				boolean overlaps = !taskStart.isAfter(end) && !taskEnd.isBefore(start);

				if (overlaps)
				{
					result.add(new TaskTreeTableDataItem(rootTask, start, end));
				}
			}
		}

		result.sort
		(
		   (i1, i2) ->
		   {
		     if (i1.task().start().isPresent() && i2.task().start().isPresent())
			      // if start dates are available compare by start dates
		        return i1.task().start().get().compareTo( i2.task().start().get());
		     else if (i1.task().start().isEmpty() && i2.task().start().isEmpty())
		     {
					 // none of the start dates are available
		       if (i1.task().end().isPresent() && i2.task().end().isPresent())
						  // if end dates are available compare by end dates
			         return i1.task().end().get().compareTo( i2.task().end().get());
		     }
		     return 0;
		   }
		);
		return result;
	}
}