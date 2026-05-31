package de.ruu.app.jeeeraaah.frontend.ui.fx;

import de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.auth.KeycloakAuthService;
import de.ruu.app.jeeeraaah.frontend.ui.fx.auth.LoginDialog;
import de.ruu.app.jeeeraaah.frontend.ui.fx.task.hierarchy.HierarchiesApp;
import de.ruu.lib.docker.health.HealthCheckProfiles;
import de.ruu.lib.docker.health.HealthCheckResult;
import de.ruu.lib.docker.health.HealthCheckRunner;
import de.ruu.lib.docker.health.fix.AutoFixRunner;
import de.ruu.lib.docker.health.fix.DockerContainerStartStrategy;
import de.ruu.lib.docker.health.fix.KeycloakRealmSetupStrategy;
import de.ruu.lib.fx.comp.FXCApp;
import de.ruu.lib.util.config.mp.ConfigHealthCheck;
import jakarta.enterprise.inject.spi.CDI;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * Abstract base class for JavaFX applications that require Keycloak authentication.
 *
 * <p>This class consolidates common startup logic for all Jeeeraaah frontend applications:</p>
 * <ul>
 *   <li>Configuration health check</li>
 *   <li>Docker environment health check with auto-fix</li>
 *   <li>Keycloak authentication (with optional testing mode)</li>
 *   <li>UI initialization</li>
 * </ul>
 *
 * <h2>Startup Flow:</h2>
 * <ol>
 *   <li>Application starts and {@link #start(Stage)} is called by JavaFX</li>
 *   <li>Configuration health check validates all required properties</li>
 *   <li>Docker environment health check runs with auto-fix capabilities</li>
 *   <li>Authentication: Testing mode auto-login OR login dialog displayed</li>
 *   <li>User authenticates with Keycloak credentials</li>
 *   <li>On successful login: Main UI loads via {@link #initializeUI(Stage)}</li>
 *   <li>On cancelled login: Application exits</li>
 * </ol>
 *
 * <h2>Subclass Requirements:</h2>
 * <p>Subclasses must implement:</p>
 * <ul>
 *   <li>{@link #getApplicationName()} - Name displayed in logs</li>
 *   <li>{@link #initializeUI(Stage)} - Application-specific UI initialization</li>
 *   <li>{@link #loadInitialData()} - Optional: Load data after authentication</li>
 * </ul>
 *
 * @see HierarchiesApp
 */
@Slf4j
public abstract class BaseAuthenticatedApp extends FXCApp
{
	/**
	 * Keycloak authentication service.
	 * Obtained via CDI in the start() method (cannot use @Inject in JavaFX Application).
	 */
	protected KeycloakAuthService authService;

	/**
	 * Returns the application name for logging purposes.
	 *
	 * @return the application name (e.g., "Dashboard", "Gantt Chart")
	 */
	protected abstract String getApplicationName();

	/**
	 * Initializes the application-specific UI after authentication is complete.
	 *
	 * <p>This method is called after successful authentication and should:</p>
	 * <ul>
	 *   <li>Call super.start() to load FXML and initialize UI components</li>
	 *   <li>Configure window properties (size, maximized, etc.)</li>
	 *   <li>Perform any additional UI setup</li>
	 * </ul>
	 *
	 * @param primaryStage the primary stage for this application
	 * @throws ExceptionInInitializerError if UI initialization fails
	 */
	protected abstract void initializeUI(Stage primaryStage) throws ExceptionInInitializerError;

	/**
	 * Loads initial data from the backend after authentication is complete.
	 *
	 * <p>Default implementation does nothing. Subclasses can override to load data.</p>
	 */
	protected void loadInitialData()
	{
		// Default: no data loading
	}

	/**
	 * Starts the JavaFX application with authentication.
	 *
	 * <p><strong>Startup sequence:</strong></p>
	 * <ol>
	 *   <li>Validate configuration properties</li>
	 *   <li>Check and fix Docker environment</li>
	 *   <li>Check if user is already logged in (in case of app restart)</li>
	 *   <li>If testing mode: Auto-login with test credentials</li>
	 *   <li>If not logged in: Show login dialog</li>
	 *   <li>Wait for authentication or cancellation</li>
	 *   <li>On success: Initialize main UI via {@link #initializeUI(Stage)}</li>
	 *   <li>On cancel: Exit application</li>
	 * </ol>
	 *
	 * @param primaryStage the primary stage for this application (provided by JavaFX)
	 * @throws ExceptionInInitializerError if UI initialization fails
	 */
	@Override public void start(Stage primaryStage) throws ExceptionInInitializerError
	{
		log.info("Starting {} application", getApplicationName());

		// ═══════════════════════════════════════════════════════════════════
		// STEP 0: Configuration Health Check
		// ═══════════════════════════════════════════════════════════════════
		if (!performConfigHealthCheck())
		{
			Platform.exit();
			return;
		}

		// ═══════════════════════════════════════════════════════════════════
		// STEP 1: Docker Environment Health Check with Auto-Fix
		// ═══════════════════════════════════════════════════════════════════
		HealthCheckRunner healthCheckRunner = performDockerHealthCheck();
		if (healthCheckRunner == null)
		{
			Platform.exit();
			return;
		}

		// ═══════════════════════════════════════════════════════════════════
		// STEP 2: Authentication Setup
		// ═══════════════════════════════════════════════════════════════════
		setupAuthentication();

		// ═══════════════════════════════════════════════════════════════════
		// STEP 3: Check if testing mode is enabled and auto-login
		// ═══════════════════════════════════════════════════════════════════
		if (!performTestingModeLogin())
		{
			Platform.exit();
			return;
		}

		// ═══════════════════════════════════════════════════════════════════
		// STEP 4: Interactive login (if not already logged in)
		// ═══════════════════════════════════════════════════════════════════
		if (!performInteractiveLogin(primaryStage))
		{
			Platform.exit();
			return;
		}

		// ═══════════════════════════════════════════════════════════════════
		// STEP 5: User is authenticated - initialize main UI
		// ═══════════════════════════════════════════════════════════════════
		log.info("=== Authentication complete - starting UI initialization ===");
		verifyAuthentication();

		initializeUI(primaryStage);

		log.info("{} UI initialized successfully", getApplicationName());

		// ═══════════════════════════════════════════════════════════════════
		// STEP 6: Load initial data
		// ═══════════════════════════════════════════════════════════════════
		loadInitialData();
	}

	/**
	 * Performs configuration health check.
	 *
	 * @return true if configuration is healthy, false otherwise
	 */
	private boolean performConfigHealthCheck()
	{
		log.info("Validating configuration properties...");

		ConfigHealthCheck configCheck = new ConfigHealthCheck();
		ConfigHealthCheck.Result configResult = configCheck.validate();

		if (!configResult.isHealthy())
		{
			log.error("❌ Configuration validation failed!");
			configResult.getErrors().forEach(error -> log.error("  {}", error));
			return false;
		}

		log.info("✅ Configuration properties validated successfully");
		return true;
	}

	/**
	 * Performs Docker environment health check with auto-fix.
	 *
	 * @return HealthCheckRunner if successful, null otherwise
	 */
	private HealthCheckRunner performDockerHealthCheck()
	{
		log.info("Performing Docker environment health check...");

		HealthCheckRunner healthCheckRunner = HealthCheckProfiles.fullEnvironment();

		AutoFixRunner autoFix = new AutoFixRunner(healthCheckRunner);
		autoFix.registerStrategy(new DockerContainerStartStrategy());
		autoFix.registerStrategy(new KeycloakRealmSetupStrategy());

		if (!autoFix.runWithAutoFix())
		{
			log.error("❌ Docker environment is not ready and auto-fix failed!");
			log.error("Please fix the issues above before starting the application.");
			showHealthCheckErrorDialog(healthCheckRunner);
			return null;
		}

		log.info("✅ Docker environment health check passed");
		return healthCheckRunner;
	}

	/**
	 * Sets up authentication service via CDI.
	 */
	private void setupAuthentication()
	{
		authService = CDI.current().select(KeycloakAuthService.class).get();
		log.info("KeycloakAuthService obtained - instance ID: {}", System.identityHashCode(authService));
	}

	/**
	 * Performs automatic login in testing mode.
	 *
	 * @return true to continue, false to exit application
	 */
	private boolean performTestingModeLogin()
	{
		boolean testingMode = ConfigProvider.getConfig()
				.getOptionalValue("testing", Boolean.class)
				.orElse(false);

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
					return true;
				}
				catch (Exception e)
				{
					log.error("  ❌ Automatic login failed: {}", e.getMessage(), e);
					log.error("  In testing mode, automatic login must succeed!");
					return false;
				}
			}
			else
			{
				log.error("  Testing mode enabled but credentials missing");
				return false;
			}
		}

		return true; // Continue to interactive login or skip if already logged in
	}

	/**
	 * Performs interactive login via login dialog.
	 *
	 * @param primaryStage the primary stage (for modal dialog)
	 * @return true to continue, false to exit application
	 */
	private boolean performInteractiveLogin(Stage primaryStage)
	{
		// Keep trying until successful login or user cancels
		while (!authService.isLoggedIn())
		{
			log.info("User not authenticated - showing login dialog");

			LoginDialog loginDialog = CDI.current().select(LoginDialog.class).get();
			boolean loginSuccessful = loginDialog.showAndWait(primaryStage);

			if (!loginSuccessful)
			{
				log.info("User cancelled login - exiting application");
				return false;
			}

			// if still not logged in after "successful" dialog return, something went wrong
			if (!authService.isLoggedIn())
			{
				log.error("Login dialog returned success but user is not logged in - retrying");
			}
			else
			{
				log.info("User authenticated successfully - proceeding with UI initialization");
			}
		}

		return true;
	}

	/**
	 * Verifies that authentication is complete and valid.
	 */
	private void verifyAuthentication()
	{
		log.info("""
				  isLoggedIn(): {}
				  Access token present: {}
				  Access token length: {}""",
				authService.isLoggedIn(),
				authService.getAccessToken() != null,
				authService.getAccessToken() != null ? authService.getAccessToken().length() : "N/A");
	}

	/**
	 * Shows an error dialog with health check failures.
	 *
	 * @param healthCheckRunner the health check runner with failures
	 */
	private void showHealthCheckErrorDialog(HealthCheckRunner healthCheckRunner)
	{
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Docker Environment Not Ready");
		alert.setHeaderText("Required services are not available");

		StringBuilder content = new StringBuilder();
		content.append("The following services are not ready:\n\n");

		for (HealthCheckResult result : healthCheckRunner.getFailures())
		{
			content.append("• ").append(result.getService()).append("\n");
			content.append("  Problem: ").append(result.getProblem()).append("\n");
			content.append("  Fix: ").append(result.getAlias()).append("\n\n");
		}

		content.append("\nAuto-fix failed. Please fix manually and restart the application.");

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
	@Override
	public void stop() throws Exception
	{
		log.info("Stopping {} application", getApplicationName());

		if (authService != null && authService.isLoggedIn())
		{
			log.info("Logging out user");
			authService.logout();
		}

		super.stop();
		log.info("Application stopped");
	}
}

