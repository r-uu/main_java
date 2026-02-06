package de.ruu.app.jeeeraaah.frontend.ui.fx.auth;

import de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.auth.KeycloakAuthService;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import java.io.IOException;

/**
 * JavaFX Login Dialog for Keycloak authentication.
 *
 * <p>This dialog prompts the user for username and password, then authenticates
 * against Keycloak using the {@link KeycloakAuthService}.</p>
 *
 * <h2>Authentication Flow (Direct Access Grants):</h2>
 * <p>This implementation uses the OAuth 2.0 <b>Resource Owner Password Credentials Grant</b>
 * (also known as "Direct Access Grants" in Keycloak), which is specifically designed for
 * trusted clients like desktop applications where the user enters credentials directly
 * into the application.</p>
 *
 * <p><b>Flow Sequence:</b></p>
 * <ol>
 *   <li>User enters username and password in this dialog</li>
 *   <li>Application sends credentials to Keycloak token endpoint</li>
 *   <li>Keycloak validates credentials and returns access token (JWT)</li>
 *   <li>Token is stored and used for subsequent API calls</li>
 * </ol>
 *
 * <p><b>⚠️ Keycloak Configuration Requirement:</b></p>
 * <p>The Keycloak client <b>must have "Direct access grants enabled" set to ON</b>.
 * Without this setting, authentication will fail with error:</p>
 * <pre>
 * {"error":"unauthorized_client",
 *  "error_description":"Client not allowed for direct access grants"}
 * </pre>
 * <p>To enable: Keycloak Admin Console → Clients → jeeeraaah-frontend → Settings →
 * "Direct access grants enabled" → ON → Save</p>
 *
 * <p><b>Security Note:</b> This flow is suitable for desktop/mobile apps where the application
 * itself is trusted. For web applications, use Authorization Code Flow with PKCE instead.</p>
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Modal dialog blocking application until authentication succeeds or user cancels</li>
 *   <li>Username and password fields with Enter key support</li>
 *   <li>Error messages displayed inline for failed login attempts</li>
 *   <li>Automatic focus on username field on startup</li>
 *   <li>Retry on authentication failure without closing dialog</li>
 *   <li>CDI integration for injecting {@link KeycloakAuthService}</li>
 * </ul>
 *
 * <h2>Usage in Application Startup:</h2>
 * <pre>{@code
 * // In DashApp.start() method:
 * @Inject
 * private KeycloakAuthService authService;
 *
 * @Override
 * public void start(Stage primaryStage) throws ExceptionInInitializerError {
 *     // Show login dialog before loading main UI
 *     LoginDialog loginDialog = CDI.current().select(LoginDialog.class).get();
 *     boolean loginSuccessful = loginDialog.showAndWait(primaryStage);
 *
 *     if (!loginSuccessful) {
 *         // User cancelled login - exit application
 *         Platform.exit();
 *         return;
 *     }
 *
 *     // User authenticated - continue with normal app startup
 *     super.start(primaryStage);
 *     // ... rest of UI initialization
 * }
 * }</pre>
 *
 * <h2>Dialog Layout:</h2>
 * <pre>
 * ┌─────────────────────────────────────┐
 * │  Login to Jeeeraaah                 │
 * ├─────────────────────────────────────┤
 * │  Please enter your credentials      │
 * │                                     │
 * │  Username: [________________]       │
 * │  Password: [________________]       │
 * │                                     │
 * │  [error message if login failed]    │
 * │                                     │
 * │         [Cancel]  [Login]           │
 * └─────────────────────────────────────┘
 * </pre>
 *
 * <h2>Authentication Flow:</h2>
 * <ol>
 *   <li>Dialog is displayed modally (blocks main window)</li>
 *   <li>User enters username and password</li>
 *   <li>User clicks "Login" or presses Enter</li>
 *   <li>{@link KeycloakAuthService#login(String, String)} is called</li>
 *   <li>On success: Dialog closes, returns true</li>
 *   <li>On failure: Error message shown, user can retry or cancel</li>
 *   <li>On cancel: Dialog closes, returns false</li>
 * </ol>
 *
 * <h2>Error Handling:</h2>
 * <ul>
 *   <li>Invalid credentials: Shows error message from Keycloak</li>
 *   <li>Network errors: Shows connection error message</li>
 *   <li>Server unavailable: Suggests checking if Keycloak is running</li>
 * </ul>
 *
 * <h2>Security Considerations:</h2>
 * <ul>
 *   <li>Password field uses {@link PasswordField} (masked input)</li>
 *   <li>Credentials are sent directly to Keycloak via HTTPS (in production)</li>
 *   <li>No credentials are stored locally - only tokens in memory</li>
 *   <li>Tokens managed by {@link KeycloakAuthService}</li>
 * </ul>
 *
 * @see KeycloakAuthService
 */
@Slf4j
public class LoginDialog
{
	/**
	 * Keycloak authentication service for login operations.
	 * Injected via CDI when dialog instance is created.
	 */
	@Inject private KeycloakAuthService authService;

	// UI components
	private Dialog<ButtonType> dialog;
	private TextField          usernameField;
	private PasswordField      passwordField;
	private Label              errorLabel;

	/**
	 * Shows the login dialog and waits for user action.
	 *
	 * <p>This method blocks until the user either successfully authenticates or cancels the dialog.</p>
	 *
	 * <p><strong>Behavior:</strong></p>
	 * <ul>
	 *   <li>Dialog is modal - blocks interaction with parent window</li>
	 *   <li>Dialog ownership ensures it stays on top of parent window</li>
	 *   <li>Focus is automatically set to username field</li>
	 *   <li>Enter key in password field triggers login</li>
	 *   <li>Failed login shows error message without closing dialog</li>
	 * </ul>
	 *
	 * @param owner the parent window that owns this dialog (can be null for standalone)
	 * @return true if authentication succeeded, false if user cancelled
	 *
	 * @throws IllegalStateException if authService is null (CDI injection failed)
	 */
	public boolean showAndWait(Stage owner)
	{
		if (authService == null)
		{
			throw new IllegalStateException("KeycloakAuthService not injected. " +
					"Ensure LoginDialog is created via CDI: CDI.current().select(LoginDialog.class).get()");
		}

		// Check if testing mode is enabled
		Config  config      = ConfigProvider.getConfig();
		boolean testingMode = config.getOptionalValue("testing", Boolean.class).orElse(false);

		if (testingMode)
		{
			// Testing mode: auto-login with credentials from config
			String username =
					config
							.getOptionalValue("keycloak.test.user", String.class)
							// TODO return default value and save to file?
							.orElseThrow(() -> new IllegalStateException(
									"Testing mode enabled but keycloak.test.user property not found"));
			String password =
					config
							.getOptionalValue("keycloak.test.password", String.class)
						// TODO return default value and save to file?
							.orElseThrow(() -> new IllegalStateException(
									"Testing mode enabled but keycloak.test.password property not found"));

			log.info("Testing mode enabled - performing automatic login with user: {}", username);

			try
			{
				authService.login(username, password);
				return authService.isLoggedIn();
			}
			catch (IOException | InterruptedException e)
			{
				log.error("Auto-login failed in testing mode", e);
				return false;
			}
		}

		// Normal mode: show login dialog
		createDialog(owner);

		// Show dialog and handle result
		// This blocks until user closes the dialog
		dialog.showAndWait();

		// Return true only if user is now logged in
		return authService.isLoggedIn();
	}

	/**
	 * Creates and configures the login dialog UI.
	 *
	 * <p>Constructs a modal dialog with:</p>
	 * <ul>
	 *   <li>Title and header text</li>
	 *   <li>Username and password input fields</li>
	 *   <li>Error message label (initially hidden)</li>
	 *   <li>Login and Cancel buttons</li>
	 *   <li>Enter key handler for quick login</li>
	 * </ul>
	 *
	 * @param owner the parent stage that owns this dialog
	 */
	private void createDialog(Stage owner)
	{
		dialog = new Dialog<>();
		dialog.setTitle("Login to Jeeeraaah");
		dialog.setHeaderText("Please enter your Keycloak credentials");

		// Make dialog modal
		// Note: initOwner() is skipped because primaryStage has no Scene yet at this point
		// The dialog will still be modal and block the application
		dialog.initModality(Modality.APPLICATION_MODAL);

		// Create input fields
		usernameField = new TextField();
		usernameField.setPromptText("Username");
		usernameField.setPrefWidth(250);

		passwordField = new PasswordField();
		passwordField.setPromptText("Password");
		passwordField.setPrefWidth(250);

		// Error label (initially invisible)
		errorLabel = new Label();
		errorLabel.setTextFill(Color.RED);
		errorLabel.setWrapText(true);
		errorLabel.setVisible(false);
		errorLabel.setMaxWidth(250);

		// Layout using GridPane for proper alignment
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 20, 10, 20));

		grid.add(new Label("Username:"), 0, 0);
		grid.add(usernameField, 1, 0);
		grid.add(new Label("Password:"), 0, 1);
		grid.add(passwordField, 1, 1);

		// Container for grid and error message
		VBox content = new VBox(10);
		content.getChildren().addAll(grid, errorLabel);
		VBox.setVgrow(errorLabel, Priority.ALWAYS);

		dialog.getDialogPane().setContent(content);

		// Add buttons
		ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

		// Get reference to login button for disabling during authentication
		Button loginButton = (Button) dialog.getDialogPane().lookupButton(loginButtonType);

		// Handle Enter key in password field
		passwordField.setOnAction(event -> {
			if (!usernameField.getText().trim().isEmpty() && !passwordField.getText().isEmpty())
			{
				loginButton.fire();
			}
		});

		// Handle login button click
		// Note: We need to consume the event to prevent dialog from closing on login failure
		loginButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
			event.consume(); // Prevent dialog from closing
			performLogin(loginButton);
		});

		// Set focus to username field when dialog is shown
		Platform.runLater(() -> usernameField.requestFocus());
	}

	/**
	 * Performs the actual login operation against Keycloak.
	 *
	 * <p>This method:</p>
	 * <ol>
	 *   <li>Validates that username and password are not empty</li>
	 *   <li>Disables the login button during authentication</li>
	 *   <li>Calls {@link KeycloakAuthService#login(String, String)}</li>
	 *   <li>On success: Closes dialog</li>
	 *   <li>On failure: Shows error message and re-enables button</li>
	 * </ol>
	 *
	 * <p><strong>Threading:</strong><br>
	 * The actual HTTP call to Keycloak happens on the JavaFX Application Thread.
	 * For production use, consider running authentication in a background thread
	 * with a progress indicator to keep UI responsive.</p>
	 *
	 * @param loginButton the login button (to disable during authentication)
	 */
	private void performLogin(Button loginButton)
	{
		String username = usernameField.getText().trim();
		String password = passwordField.getText();

		// Validate input
		if (username.isEmpty())
		{
			showError("Please enter a username");
			return;
		}

		if (password.isEmpty())
		{
			showError("Please enter a password");
			return;
		}

		// Hide previous error message
		errorLabel.setVisible(false);

		// Disable login button during authentication
		loginButton.setDisable(true);

		try
		{
			// Attempt authentication
			log.info("Attempting login for user: {}", username);
			authService.login(username, password);

			// Login successful - close dialog
			log.info("Login successful for user: {}", username);
			dialog.close();
		}
		catch (IOException e)
		{
			// Network error or connection failed
			log.error("Login failed due to network error", e);

			String errorMessage = "Connection error: " + e.getMessage() +
					"\n\nPlease check:\n" +
					"- Is Keycloak running? (docker ps | grep keycloak)\n" +
					"- Is the Keycloak URL correct? (check microprofile-config.properties_)";
			showError(errorMessage);
		}
		catch (InterruptedException e)
		{
			// HTTP request interrupted
			log.error("Login interrupted", e);
			showError("Login was interrupted. Please try again.");
			Thread.currentThread().interrupt(); // Restore interrupt status
		}
		catch (Exception e)
		{
			// Authentication failed (invalid credentials, etc.)
			log.error("Login failed", e);

			String errorMessage = "Authentication failed: " + e.getMessage() +
					"\n\nPlease check your username and password.";
			showError(errorMessage);
		}
		finally
		{
			// Re-enable login button
			loginButton.setDisable(false);
		}
	}

	/**
	 * Displays an error message in the dialog.
	 *
	 * @param message the error message to display
	 */
	private void showError(String message)
	{
		errorLabel.setText(message);
		errorLabel.setVisible(true);
	}
}
