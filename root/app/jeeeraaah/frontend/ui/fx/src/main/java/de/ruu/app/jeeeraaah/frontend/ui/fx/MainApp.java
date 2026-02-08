package de.ruu.app.jeeeraaah.frontend.ui.fx;

import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * Main JavaFX application for Jeeeraaah.
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
public class MainApp extends BaseAuthenticatedApp
{
	@Override protected String getApplicationName()
	{
		return "Main";
	}

	@Override protected void initializeUI(Stage primaryStage) throws ExceptionInInitializerError
	{
		initializeStageAndScene(primaryStage);
	}
}