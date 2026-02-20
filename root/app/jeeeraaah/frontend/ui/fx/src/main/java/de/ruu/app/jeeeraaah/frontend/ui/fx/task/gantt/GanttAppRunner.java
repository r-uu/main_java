package de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt;

import de.ruu.app.jeeeraaah.frontend.ui.fx.taskgroup.selector.TaskGroupSelectorService.TaskGroupSelectorComponentReadyEvent;
import de.ruu.lib.fx.comp.FXCAppRunner;
import lombok.extern.slf4j.Slf4j;

/**
 * Runner for GanttApp - TableView-based Gantt Chart.
 */
@Slf4j
public class GanttAppRunner extends FXCAppRunner
{
	public static void main(String[] args)
	{
		log.info("Starting GanttAppRunner...");

		// Set config file path if not already set (portable solution)
		if (System.getProperty("config.file.name") == null)
		{
			System.setProperty("config.file.name", "../../../testing.properties");
		}

		// Configure JPMS module access for Weld CDI
		FXCAppRunner.configureModuleAccessForCDI();
		// Register application-specific events for CDI
		TaskGroupSelectorComponentReadyEvent.addReadsUnnamedModule();
		FXCAppRunner.run(GanttApp.class, args);
	}
}


