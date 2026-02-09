package de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt2;

import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupFlat;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupFlat.TaskGroupWithTasks;
import de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.TaskGroupServiceClient;
import de.ruu.app.jeeeraaah.frontend.ui.fx.taskgroup.selector.TaskGroupSelector;
import de.ruu.app.jeeeraaah.frontend.ui.fx.taskgroup.selector.TaskGroupSelectorService.TaskGroupSelectorComponentReadyEvent;
import de.ruu.app.jeeeraaah.frontend.ui.fx.util.ServiceOperationExecutor;
import de.ruu.lib.cdi.se.EventDispatcher;
import de.ruu.lib.fx.FXUtil;
import de.ruu.lib.fx.comp.FXCAppStartedEvent;
import de.ruu.lib.fx.comp.FXCController;
import de.ruu.lib.fx.control.dialog.ExceptionDialog;
import de.ruu.lib.ws.rs.NonTechnicalException;
import de.ruu.lib.ws.rs.TechnicalException;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.isNull;
import static javafx.scene.layout.Priority.ALWAYS;

/**
 * Controller for Gantt2 - manages TaskGroupSelector, Filter, and GanttTable.
 * <p>
 * Converts TaskGroupBean to TaskGroupWithTasks for efficient hierarchy building.
 */
@Slf4j
@Dependent
class Gantt2Controller extends FXCController.DefaultFXCController<Gantt2, Gantt2Service> implements Gantt2Service
{
	@FXML private VBox vBxRoot;
	@FXML private VBox vBxForSelector;
	@FXML private HBox hBxForFilter;
	@FXML private DatePicker dtPckrStart;
	@FXML private DatePicker dtPckrEnd;
	@FXML private Button btnApply;

	@Inject private EventDispatcher<FXCAppStartedEvent> appStartedEventDispatcher;
	@Inject private EventDispatcher<TaskGroupSelectorComponentReadyEvent> taskGroupSelectorReadyEventDispatcher;

	@Inject private TaskGroupSelector taskGroupSelector;
	@Inject private GanttTable ganttTable;
	@Inject private TaskGroupServiceClient taskGroupServiceClient;
	@Inject private ServiceOperationExecutor executor;

	private Optional<TaskGroupWithTasks> selectedTaskGroup = Optional.empty();

	@Override @FXML protected void initialize()
	{
		appStartedEventDispatcher.add(e -> onAppStarted(e));
		taskGroupSelectorReadyEventDispatcher.add(e -> onTaskGroupSelectorReady(e));

		// Prepare selector
		vBxForSelector.setAlignment(Pos.BOTTOM_LEFT);
		vBxForSelector.getChildren().add(taskGroupSelector.localRoot());
		FXUtil.wrapInTitledBorder("group", (Region) vBxForSelector);

		// Prepare filter
		configureDatePickerGermanFormat(dtPckrStart);
		configureDatePickerGermanFormat(dtPckrEnd);

		// Set default date range to Q1 2025
		dtPckrStart.setValue(LocalDate.of(2025, 1, 1));
		dtPckrEnd.setValue(LocalDate.of(2025, 3, 31));

		btnApply.setOnAction(e -> onApply());
		FXUtil.wrapInTitledBorder("filter", (Region) hBxForFilter);

		// Add GanttTable
		vBxRoot.getChildren().add(ganttTable.localRoot());
		VBox.setVgrow(ganttTable.localRoot(), ALWAYS);
		HBox.setHgrow(ganttTable.localRoot(), ALWAYS);
		FXUtil.setAnchorsInAnchorPaneTo(ganttTable.localRoot(), 0);
	}

	@Override public void loadInitialData()
	{
		log.info("Loading initial data from backend...");
		fetchTaskGroupsFromBackendAndPopulateTaskGroupSelector();
	}

	private void fetchTaskGroupsFromBackendAndPopulateTaskGroupSelector()
	{
		try
		{
			Set<TaskGroupFlat> groups = executor.execute(
					() -> taskGroupServiceClient.findAllFlat(),
					"fetching task groups",
					"Failed to load task groups",
					"Load failed after re-login"
			);

			taskGroupSelector.service().items(groups);
		}
		catch (TechnicalException | NonTechnicalException e)
		{
			log.error("failure fetching task groups from backend", e);
			ExceptionDialog.showAndWait("failure fetching task groups from backend", e);
		}
	}

	private void onApply()
	{
		ganttTable.service().populate(
				selectedTaskGroup.orElse(null),
				dtPckrStart.getValue(),
				dtPckrEnd.getValue()
		);
	}

	private void onAppStarted(FXCAppStartedEvent event)
	{
		log.debug("app started event received");
	}

	private void onTaskGroupSelectorReady(TaskGroupSelectorComponentReadyEvent event)
	{
		log.debug("task group selector ready event received");
		ReadOnlyObjectProperty<TaskGroupFlat> selectionProperty =
				taskGroupSelector.service().selectedTaskGroupProperty();

		selectionProperty.addListener((obs, old, taskGroupFlat) ->
		{
			if (isNull(taskGroupFlat)) return;

			log.debug("task group selected: {}", taskGroupFlat.name());

			// Load full task group with tasks from backend
			try
			{
				Optional<TaskGroupBean> taskGroupOptional = executor.execute(
						() -> taskGroupServiceClient.findWithTasks(taskGroupFlat.id()),
						"loading task group with tasks",
						"Failed to load task group",
						"Load failed after re-login"
				);

				if (taskGroupOptional.isPresent())
				{
					TaskGroupBean taskGroupBean = taskGroupOptional.get();

					// Convert to TaskGroupWithTasks (lightweight, no expensive relations)
					TaskGroupWithTasks taskGroupWithTasks = new TaskGroupWithTasks(taskGroupBean);
					selectedTaskGroup = Optional.of(taskGroupWithTasks);

					int taskCount = taskGroupWithTasks.tasks().size();
					log.debug("task group loaded and converted to TaskGroupWithTasks with {} tasks", taskCount);

					// Auto-apply filter when selection changes
					onApply();
				}
				else
				{
					log.warn("Task group not found: {}", taskGroupFlat.id());
					selectedTaskGroup = Optional.empty();
				}
			}
			catch (TechnicalException | NonTechnicalException e)
			{
				log.error("Failed to load task group: {}", taskGroupFlat.name(), e);
				ExceptionDialog.showAndWait("Failed to load task group", e);
			}
		});
	}

	private void configureDatePickerGermanFormat(@NonNull DatePicker datePicker)
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

		datePicker.setConverter(new StringConverter<>()
		{
			@Override
			public String toString(LocalDate date)
			{
				return date != null ? formatter.format(date) : "";
			}

			@Override
			public LocalDate fromString(String string)
			{
				try
				{
					return string != null && !string.isEmpty() ? LocalDate.parse(string, formatter) : null;
				}
				catch (DateTimeParseException e)
				{
					log.error("invalid date format: {}", string, e);
					return null;
				}
			}
		});
	}
}






