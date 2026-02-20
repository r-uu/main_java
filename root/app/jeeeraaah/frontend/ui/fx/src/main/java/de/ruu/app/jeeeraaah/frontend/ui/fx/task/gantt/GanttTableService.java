package de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt;

import de.ruu.app.jeeeraaah.common.api.domain.flat.TaskGroupFlat.TaskGroupWithTasks;
import de.ruu.lib.fx.comp.FXCService;
import lombok.NonNull;

import java.time.LocalDate;

/**
 * Service interface for GanttTable component.
 * <p>
 * Uses {@link TaskGroupWithTasks} for better performance - loads only lightweight task data
 * without expensive relations.
 */
public interface GanttTableService extends FXCService
{
	/**
	 * Populates the Gantt table with tasks from the given task group for the specified date range.
	 *
	 * @param taskGroup the task group to display (can be null to clear the table)
	 * @param start the start date of the visible period
	 * @param end the end date of the visible period
	 */
	void populate(TaskGroupWithTasks taskGroup, @NonNull LocalDate start, @NonNull LocalDate end);
}

