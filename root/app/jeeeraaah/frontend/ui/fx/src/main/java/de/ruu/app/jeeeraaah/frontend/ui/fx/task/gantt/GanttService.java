package de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt;

import de.ruu.app.jeeeraaah.common.api.domain.flat.TaskGroupFlat.TaskGroupWithTasks;
import de.ruu.lib.fx.comp.FXCService;

/**
 * Service interface for Gantt view.
 * <p>
 * Uses {@link TaskGroupWithTasks} for efficient hierarchy building.
 */
public interface GanttService extends FXCService
{
	/**
	 * Loads initial data from backend after authentication.
	 */
	void loadInitialData();
}

