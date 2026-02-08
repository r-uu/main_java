package de.ruu.app.jeeeraaah.frontend.ui.fx.taskgroup;

import de.ruu.app.jeeeraaah.frontend.ui.fx.BaseAuthenticatedApp;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * TaskGroup Management JavaFX application for Jeeeraaah.
 *
 * <p>This application extends {@link BaseAuthenticatedApp} which provides:</p>
 * <ul>
 *   <li>Configuration health check</li>
 *   <li>Docker environment health check with auto-fix</li>
 *   <li>Keycloak authentication (with optional testing mode)</li>
 *   <li>Standardized startup flow</li>
 * </ul>
 *
 * @see BaseAuthenticatedApp
 */
@Slf4j
public class TaskGroupManagementApp extends BaseAuthenticatedApp
{
	@Override
	protected String getApplicationName()
	{
		return "TaskGroup Management";
	}

	@Override
	protected void initializeUI(Stage primaryStage) throws ExceptionInInitializerError
	{
		initializeStageAndScene(primaryStage);
	}
}