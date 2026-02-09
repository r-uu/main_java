package de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt2;

import de.ruu.app.jeeeraaah.frontend.ui.fx.taskgroup.selector.TaskGroupSelectorService.TaskGroupSelectorComponentReadyEvent;
import de.ruu.lib.fx.comp.FXCAppRunner;
import lombok.extern.slf4j.Slf4j;

/**
 * Runner for Gantt2App - TableView-based Gantt Chart.
 */
@Slf4j
public class Gantt2AppRunner extends FXCAppRunner
{
	public static void main(String[] args)
	{
		log.info("Starting Gantt2AppRunner...");
		// Configure JPMS module access for Weld CDI
		FXCAppRunner.configureModuleAccessForCDI();
		// Register application-specific events for CDI
		TaskGroupSelectorComponentReadyEvent.addReadsUnnamedModule();
		FXCAppRunner.run(Gantt2App.class, args);
	}
}


