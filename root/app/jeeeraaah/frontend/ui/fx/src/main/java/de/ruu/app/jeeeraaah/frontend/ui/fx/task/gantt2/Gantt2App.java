package de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt2;

import de.ruu.app.jeeeraaah.frontend.ui.fx.BaseAuthenticatedApp;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * Gantt2 Chart JavaFX application - TableView-based with frozen first two columns.
 *
 * <p>This application uses a different approach than the original GanttApp:</p>
 * <ul>
 *   <li>Single TableView instead of TreeTableView</li>
 *   <li>Manual hierarchy management with expand/collapse checkboxes</li>
 *   <li>First two columns are FIXED (Checkbox + Task Name)</li>
 *   <li>Date columns are horizontally scrollable</li>
 * </ul>
 *
 * <h2>Advantages over TreeTableView approach:</h2>
 * <ul>
 *   <li>True frozen columns (no complex synchronization needed)</li>
 *   <li>Simpler implementation (~200 lines vs 500+)</li>
 *   <li>Better performance with many columns</li>
 *   <li>Full control over hierarchy display</li>
 * </ul>
 *
 * @see BaseAuthenticatedApp
 */
@Slf4j
public class Gantt2App extends BaseAuthenticatedApp
{
	@Override protected String getApplicationName() { return "Gantt Chart 2.0"; }

	@Override protected void initializeUI(Stage primaryStage) throws ExceptionInInitializerError
	{
		primaryStage.setResizable(true);
		initializeStageAndScene(primaryStage);

		// Ensure window can be maximized
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
					// Gantt2 extends DefaultFXCView which has controller() method
					if (view instanceof Gantt2 gantt2)
					{
						// Access controller via the view's controller() method
						Gantt2Service service = gantt2.service();
						if (service != null)
						{
							service.loadInitialData();
							log.info("Initial data loaded successfully");
						}
						else
						{
							log.error("Service is null - cannot load initial data!");
						}
					}
					else
					{
						log.error("View is not an instance of Gantt2 - cannot access controller!");
					}
				},
				() -> log.error("View is not present - cannot access controller!")
		);
	}
}


