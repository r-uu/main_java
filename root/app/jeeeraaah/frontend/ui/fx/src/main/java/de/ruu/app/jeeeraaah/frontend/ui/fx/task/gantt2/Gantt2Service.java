package de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt2;

import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupFlat.TaskGroupWithTasks;
import de.ruu.lib.fx.comp.FXCService;

/**
 * Service interface for Gantt2 view.
 * <p>
 * Uses {@link TaskGroupWithTasks} for efficient hierarchy building.
 */
public interface Gantt2Service extends FXCService
{
	/**
	 * Loads initial data from backend after authentication.
	 */
	void loadInitialData();
}

