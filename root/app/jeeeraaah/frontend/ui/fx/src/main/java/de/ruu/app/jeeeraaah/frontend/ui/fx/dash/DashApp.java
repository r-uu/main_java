package de.ruu.app.jeeeraaah.frontend.ui.fx.dash;

import de.ruu.app.jeeeraaah.frontend.ui.fx.BaseAuthenticatedApp;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * Main JavaFX application for the Jeeeraaah Dashboard.
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
public class DashApp extends BaseAuthenticatedApp
{
	@Override
	protected String getApplicationName()
	{
		return "Dashboard";
	}

	@Override
	protected void initializeUI(Stage primaryStage) throws ExceptionInInitializerError
	{
		// Configure window properties BEFORE loading FXML
		primaryStage.setResizable(true);

		// Load FXML and initialize UI components (triggers DashController.initialize())
		log.info("Loading FXML and initializing UI components...");
		initializeStageAndScene(primaryStage);
		log.info("=== UI initialization complete ===");

		// Apply window customizations AFTER UI is loaded
		primaryStage.setMaximized(true);
	}

	@Override
	protected void loadInitialData()
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
					// Cast to Dash to access getController()
					if (view instanceof Dash dash)
					{
						DashController controller = dash.getController();
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
						log.error("View is not an instance of Dash - cannot access controller!");
					}
				},
				() -> log.error("View is not present - cannot access controller!")
		);
	}
}
