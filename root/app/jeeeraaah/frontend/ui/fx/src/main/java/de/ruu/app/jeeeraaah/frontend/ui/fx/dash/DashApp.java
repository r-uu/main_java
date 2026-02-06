package de.ruu.app.jeeeraaah.frontend.ui.fx.dash;

import org.eclipse.microprofile.config.ConfigProvider;

import de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.auth.KeycloakAuthService;
import de.ruu.app.jeeeraaah.frontend.ui.fx.auth.LoginDialog;
import de.ruu.lib.fx.comp.FXCApp;
import jakarta.enterprise.inject.spi.CDI;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * Main JavaFX application for the Jeeeraaah Dashboard.
 *
 * <p>This application requires Keycloak authentication before the main UI is displayed.</p>
 *
 * <h2>Startup Flow:</h2>
 * <ol>
 *   <li>Application starts and {@link #start(Stage)} is called by JavaFX</li>
 *   <li>Login dialog is displayed (modal, blocks UI)</li>
 *   <li>User authenticates with Keycloak credentials</li>
 *   <li>On successful login: Main dashboard UI is loaded</li>
 *   <li>On cancelled login: Application exits</li>
 * </ol>
 *
 * <h2>Authentication Integration:</h2>
 * <p>The application uses {@link LoginDialog} to authenticate users via {@link KeycloakAuthService}.
 * After successful authentication, all REST API calls automatically include the JWT token
 * via the AuthorizationHeaderFilter registered in service clients.</p>
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
 * <h2>Configuration:</h2>
 * <p>Authentication settings are read from microprofile-config.properties_:</p>
 * <ul>
 *   <li>keycloak.auth.server.url - Keycloak server base URL</li>
 *   <li>keycloak.realm - Realm name</li>
 *   <li>keycloak.client-id - Client ID for frontend</li>
 * </ul>
 *
 * <h2>Error Handling:</h2>
 * <ul>
 *   <li>Login cancelled by user → Application exits gracefully</li>
 *   <li>Login failed → User can retry without restarting application</li>
 *   <li>Keycloak unavailable → Error message with troubleshooting hints</li>
 * </ul>
 *
 * @see LoginDialog
 * @see KeycloakAuthService
 */
@Slf4j
public class DashApp extends FXCApp
{
	/**
	 * Keycloak authentication service.
	 * Obtained via CDI in the start() method (cannot use @Inject in JavaFX Application).
	 */
	private KeycloakAuthService authService;

	/**
	 * Starts the JavaFX application with authentication.
	 *
	 * <p><strong>Startup sequence:</strong></p>
	 * <ol>
	 *   <li>Check if user is already logged in (in case of app restart)</li>
	 *   <li>If not logged in: Show login dialog</li>
	 *   <li>Wait for authentication or cancellation</li>
	 *   <li>On success: Initialize main UI via super.start()</li>
	 *   <li>On cancel: Exit application</li>
	 * </ol>
	 *
	 * <p><strong>Why authentication happens in start():</strong><br>
	 * The login dialog needs a JavaFX Stage as owner for proper modality.
	 * The primaryStage is only available in the start() method, not earlier.</p>
	 *
	 * @param primaryStage the primary stage for this application (provided by JavaFX)
	 * @throws ExceptionInInitializerError if UI initialization fails
	 */
	@Override public void start(Stage primaryStage) throws ExceptionInInitializerError
	{
		log.info("Starting Jeeeraaah Dashboard application");

		// ═══════════════════════════════════════════════════════════════════
		// STEP 0: Configuration Health Check
		// ═══════════════════════════════════════════════════════════════════
		log.info("Validating configuration properties...");

		de.ruu.lib.util.config.mp.ConfigHealthCheck configCheck = new de.ruu.lib.util.config.mp.ConfigHealthCheck();
		de.ruu.lib.util.config.mp.ConfigHealthCheck.Result configResult = configCheck.validate();

		if (!configResult.isHealthy())
		{
			log.error("❌ Configuration validation failed!");
			configResult.getErrors().forEach(error -> log.error("  {}", error));
			Platform.exit();
			return;
		}
		else
		{
			log.info("✅ Configuration properties validated successfully");
		}

		// ═══════════════════════════════════════════════════════════════════
		// STEP 1: Docker Environment Health Check with Auto-Fix
		// ═══════════════════════════════════════════════════════════════════
		log.info("Performing Docker environment health check...");

		// Use the new modular health check API
		de.ruu.lib.docker.health.HealthCheckRunner healthCheckRunner =
			de.ruu.lib.docker.health.HealthCheckProfiles.fullEnvironment();

		// Run health checks with auto-fix
		de.ruu.lib.docker.health.fix.AutoFixRunner autoFix = new de.ruu.lib.docker.health.fix.AutoFixRunner(healthCheckRunner);
		autoFix.registerStrategy(new de.ruu.lib.docker.health.fix.DockerContainerStartStrategy());
		autoFix.registerStrategy(new de.ruu.lib.docker.health.fix.KeycloakRealmSetupStrategy());

		if (!autoFix.runWithAutoFix())
		{
			log.error("❌ Docker environment is not ready and auto-fix failed!");
			log.error("Please fix the issues above before starting the application.");
			showHealthCheckErrorDialog(healthCheckRunner);
			Platform.exit();
			return;
		}
		else
		{
			log.info("✅ Docker environment health check passed");
		}

		// ═══════════════════════════════════════════════════════════════════
		// STEP 1: Authentication Setup
		// ═══════════════════════════════════════════════════════════════════

		// Obtain authService via CDI (cannot use @Inject in JavaFX Application)
		authService = CDI.current().select(KeycloakAuthService.class).get();
		log.info("KeycloakAuthService obtained - instance ID: {}", System.identityHashCode(authService));

		// STEP 1: Check if testing mode is enabled and auto-login with test credentials
		boolean testingMode = ConfigProvider.getConfig()
				.getOptionalValue("testing", Boolean.class)
				.orElse(false);
		
		log.info("=== TESTING MODE STATUS ===");
		log.info("  testing property value: {}", testingMode);
		log.info("  isLoggedIn(): {}", authService.isLoggedIn());
		log.info("  Will attempt auto-login: {}", testingMode && !authService.isLoggedIn());

		// Debug: Show all config sources
		log.info("=== CONFIG SOURCES ===");
		ConfigProvider.getConfig().getConfigSources().forEach(source -> {
			log.info("  Source: {} (ordinal: {})", source.getName(), source.getOrdinal());
		});

		if (testingMode && !authService.isLoggedIn())
		{
			log.info("=== Testing mode enabled - attempting automatic login ===");
			
			String testUsername = ConfigProvider.getConfig()
					.getOptionalValue("keycloak.test.user", String.class)
					.orElse(null);
			String testPassword = ConfigProvider.getConfig()
					.getOptionalValue("keycloak.test.password", String.class)
					.orElse(null);
			
			if (testUsername != null && testPassword != null)
			{
				log.info("  Test credentials found: username={}", testUsername);
				try
				{
					authService.login(testUsername, testPassword);
					log.info("  ✅ Automatic login successful");
					log.info("  Access token (first 50 chars): {}...", 
							authService.getAccessToken().substring(0, Math.min(50, authService.getAccessToken().length())));
				}
				catch (Exception e)
				{
					log.error("  ❌ Automatic login failed: {}", e.getMessage(), e);
					log.info("  In testing mode, automatic login must succeed!");
					log.info("  Please ensure:");
					log.info("    - Keycloak server is running");
					log.info("    - Credentials in microprofile-config.properties are correct");
					log.info("    - Direct Access Grants are enabled for the client");
					Platform.exit();
					return;
				}
			}
			else
			{
				log.error("  Testing mode enabled but credentials missing in microprofile-config.properties");
				log.error("  Expected properties: keycloak.test.user, keycloak.test.password");
				Platform.exit();
				return;
			}
		}
		
		// Keep trying until successful login or user cancels (skip in testing mode if already logged in)
		while (!authService.isLoggedIn())
		{
			log.info("User not authenticated - showing login dialog");

			// STEP 2: Show login dialog
			// Create LoginDialog via CDI to ensure proper dependency injection
			LoginDialog loginDialog = CDI.current().select(LoginDialog.class).get();
			boolean loginSuccessful = loginDialog.showAndWait(primaryStage);

			// STEP 3: Handle login result
			if (!loginSuccessful)
			{
				// User cancelled login - exit application
				log.info("User cancelled login - exiting application");
				Platform.exit();
				return;
			}

			// If still not logged in after "successful" dialog return, something went wrong
			if (!authService.isLoggedIn())
			{
				log.error("Login dialog returned success but user is not logged in - retrying");
				// Loop will show dialog again
			}
			else
			{
				log.info("User authenticated successfully - proceeding with UI initialization");
			}
		}

		// STEP 4: User is authenticated - initialize main UI
		log.info("=== Authentication complete - starting UI initialization ===");
		log.info("  isLoggedIn(): {}", authService.isLoggedIn());
		log.info("  Access token present: {}", authService.getAccessToken() != null);

		if (!authService.isLoggedIn())
		{
			log.error("CRITICAL: User is not logged in after authentication phase!");
			log.error("This should never happen. Exiting application.");
			Platform.exit();
			return;
		}

		if (authService.getAccessToken() == null)
		{
			log.error("CRITICAL: Access token is null after authentication!");
			log.error("This should never happen. Exiting application.");
			Platform.exit();
			return;
		}

		log.info("Authentication verified successfully. Access token length: {}",
				authService.getAccessToken().length());

		// Configure window properties BEFORE calling super.start()
		primaryStage.setResizable(true);

		// Call parent implementation to load FXML and initialize UI components
		log.info("Calling super.start() - this will load FXML and trigger DashController.initialize()");
		super.start(primaryStage);
		log.info("=== UI initialization complete ===");

		// STEP 5: Load data from backend now that authentication is complete
		log.info("=== Loading initial data from backend ===");
		log.info("  Verifying authentication status before data load...");
		log.info("  isLoggedIn(): {}", authService.isLoggedIn());
		log.info("  Access token present: {}", authService.getAccessToken() != null);
		log.info("  Access token length: {}",
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

		// STEP 6: Apply window customizations AFTER UI is loaded
		primaryStage.setMaximized(true);

		// Optional: Set global font size for the entire application
		// primaryStage.getScene().getRoot().setStyle("-fx-font-size: 10px;");

		log.info("Dashboard UI initialized successfully");
	}

	/**
	 * Shows an error dialog with health check failures
	 */
	private void showHealthCheckErrorDialog(de.ruu.lib.docker.health.HealthCheckRunner healthCheckRunner)
	{
		javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
			javafx.scene.control.Alert.AlertType.ERROR
		);
		alert.setTitle("Docker Environment Not Ready");
		alert.setHeaderText("Required services are not available");

		StringBuilder content = new StringBuilder();
		content.append("The following services are not ready:\n\n");

		for (de.ruu.lib.docker.health.HealthCheckResult result : healthCheckRunner.getFailures())
		{
			content.append("• ").append(result.getService()).append("\n");
			content.append("  Problem: ").append(result.getProblem()).append("\n");
			content.append("  Fix: ").append(result.getAlias()).append("\n\n");
		}

		content.append("\nAuto-fix failed. Please fix manually and restart the application.");
		content.append("\nCheck the console output for detailed commands.");

		alert.setContentText(content.toString());
		alert.showAndWait();
	}

	/**
	 * Called when application stops.
	 *
	 * <p>Performs cleanup:</p>
	 * <ul>
	 *   <li>Logs out from Keycloak (clears tokens)</li>
	 *   <li>Calls parent cleanup</li>
	 * </ul>
	 */
	@Override public void stop() throws Exception
	{
		log.info("Stopping Jeeeraaah Dashboard application");

		// Logout and clear tokens (only if authService was initialized)
		if (authService != null && authService.isLoggedIn())
		{
			log.info("Logging out user");
			authService.logout();
		}

		// Call parent cleanup
		super.stop();

		log.info("Application stopped");
	}
}
