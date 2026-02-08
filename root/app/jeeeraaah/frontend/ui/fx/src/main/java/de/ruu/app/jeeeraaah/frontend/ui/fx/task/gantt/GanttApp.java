package de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt;

import de.ruu.app.jeeeraaah.frontend.ui.fx.BaseAuthenticatedApp;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * Gantt Chart JavaFX application for Jeeeraaah.
 *
 * <p>This application extends {@link BaseAuthenticatedApp} which provides:</p>
 * <ul>
 *   <li>Configuration health check</li>
 *   <li>Docker environment health check with auto-fix</li>
 *   <li>Keycloak authentication (with optional testing mode)</li>
 *   <li>Standardized startup flow</li>
 * </ul>
 *
 * <h2>Prerequisites:</h2>
 * <ul>
 *   <li>Keycloak server running on configured URL (default: http://localhost:8080)</li>
 *   <li>Realm "jeeeraaah-realm" configured in Keycloak</li>
 *   <li>Public client "jeeeraaah-frontend" created in realm</li>
 *   <li>Direct Access Grants enabled for client</li>
 *   <li>User account created in Keycloak with appropriate roles</li>
 * </ul>
 *
 * @see BaseAuthenticatedApp
 */
@Slf4j
public class GanttApp extends BaseAuthenticatedApp
{
	@Override protected String getApplicationName() { return "Gantt Chart"; }

	@Override protected void initializeUI(Stage primaryStage) throws ExceptionInInitializerError
	{
		primaryStage.setResizable(true);
		initializeStageAndScene(primaryStage);

		// Ensure window can be maximized - override sizeToScene constraints
		primaryStage.setMaxWidth(Double.MAX_VALUE);
		primaryStage.setMaxHeight(Double.MAX_VALUE);

		primaryStage.setMaximized(true);
	}

	@Override protected void loadInitialData()
	{
		log.info("""
				=== Loading initial data from backend ===
				  Verifying authentication status before data load...
				  isLoggedIn(): {}
				  Access token present: {}
				  Access token length: {}""",
				authService.isLoggedIn(),
				authService.getAccessToken() != null,
				authService.getAccessToken() != null ? authService.getAccessToken().length() : 0);

		optionalPrimaryView().ifPresentOrElse(
				view -> {
					// Cast to Gantt to access getController()
					if (view instanceof Gantt gantt)
					{
						GanttController controller = gantt.getController();
						if (controller != null)
						{
							controller.loadInitialData();
							log.info("Initial data loaded successfully");
						}
						else
						{
							log.error("Controller is null - cannot load initial data!");
						}
					}
					else
					{
						log.error("View is not an instance of Gantt - cannot access controller!");
					}
				},
				() -> log.error("View is not present - cannot access controller!")
		);
	}
}