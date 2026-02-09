package de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt2;

import de.ruu.app.jeeeraaah.common.api.domain.TaskFlat;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupFlat.TaskGroupWithTasks;
import de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.TaskGroupServiceClient;
import de.ruu.lib.fx.comp.FXCController;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Objects.isNull;

/**
 * Controller for GanttTable - manages TableView with manual hierarchy.
 * <p>
 * Uses {@link TaskFlat} and {@link TaskGroupWithTasks} for better performance.
 * Supports unlimited hierarchy depth through recursive building.
 */
@Slf4j
@Dependent
public class GanttTableController
		extends FXCController.DefaultFXCController<GanttTable, GanttTableService>
		implements GanttTableService
{
	@FXML private TableView<GanttTableRow> ganttTable;

	@Inject private TaskGroupServiceClient taskGroupServiceClient;

	private final ObservableList<GanttTableRow> allRows = FXCollections.observableArrayList();
	private final ObservableList<GanttTableRow> visibleRows = FXCollections.observableArrayList();

	@Override @FXML
	protected void initialize()
	{
		// Initialize with default columns
		LocalDate defaultStart = LocalDate.of(2025, 1, 1);
		LocalDate defaultEnd = LocalDate.of(2025, 3, 31);
		createColumns(defaultStart, defaultEnd);

		// Set items
		ganttTable.setItems(visibleRows);

		// Configure table
		ganttTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		ganttTable.setFixedCellSize(28.0);
	}

	@Override
	public void populate(TaskGroupWithTasks taskGroup, @NonNull LocalDate start, @NonNull LocalDate end)
	{
		// Clear existing data
		allRows.clear();
		visibleRows.clear();
		ganttTable.getColumns().clear();

		if (isNull(taskGroup)) return;

		// Recreate columns for the new date range
		createColumns(start, end);

		// Build hierarchy
		List<GanttTableRow> rootRows = buildHierarchy(taskGroup);
		allRows.addAll(rootRows);

		// Update visible rows
		updateVisibleRows();
	}

	/**
	 * Creates all columns: Checkbox, Task Name, and Date columns.
	 */
	private void createColumns(LocalDate start, LocalDate end)
	{
		// Column 1: Checkbox for expand/collapse (FIXED, 40px)
		TableColumn<GanttTableRow, String> expandColumn = new TableColumn<>("");
		expandColumn.setPrefWidth(40);
		expandColumn.setMinWidth(40);
		expandColumn.setMaxWidth(40);
		expandColumn.setResizable(false);
		expandColumn.setReorderable(false);
		expandColumn.setSortable(false);

		expandColumn.setCellFactory(col -> new TableCell<>() {
			private final CheckBox checkBox = new CheckBox();

			{
				checkBox.setAllowIndeterminate(false);
				checkBox.setOnAction(e -> {
					GanttTableRow row = getTableRow().getItem();
					if (row != null && row.hasChildren())
					{
						row.toggleExpanded();
						updateVisibleRows();
					}
				});
			}

			@Override
			protected void updateItem(String item, boolean empty)
			{
				super.updateItem(item, empty);

				GanttTableRow row = getTableRow() != null ? getTableRow().getItem() : null;

				if (empty || row == null)
				{
					setGraphic(null);
				}
				else
				{
					if (row.hasChildren())
					{
						checkBox.setSelected(row.isExpanded());
						checkBox.setDisable(false);
						setGraphic(checkBox);
					}
					else
					{
						setGraphic(null);
					}
				}
			}
		});

		ganttTable.getColumns().add(expandColumn);

		// Column 2: Task Name with indentation (FIXED, 300px)
		TableColumn<GanttTableRow, String> nameColumn = new TableColumn<>("Task");
		nameColumn.setPrefWidth(300);
		nameColumn.setMinWidth(150);
		nameColumn.setMaxWidth(500);
		nameColumn.setResizable(true);
		nameColumn.setReorderable(false);
		nameColumn.setSortable(false);

		nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTaskName()));

		nameColumn.setCellFactory(col -> new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty)
			{
				super.updateItem(item, empty);

				if (empty || item == null)
				{
					setText(null);
					setGraphic(null);
					setStyle("");
				}
				else
				{
					GanttTableRow row = getTableRow().getItem();
					if (row != null)
					{
						// Indentation based on level
						int level = row.getLevel();
						String indent = "  ".repeat(level);
						setText(indent + item);

						// Visual style
						if (level == 0)
						{
							// Main tasks: bold
							setStyle("-fx-font-weight: bold; -fx-background-color: #f0f0f0;");
						}
						else
						{
							// Subtasks: normal
							setStyle("");
						}
					}
				}
			}
		});

		ganttTable.getColumns().add(nameColumn);

		// Date columns (3-N): One column per day (SCROLLABLE, 30px each)
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");
		LocalDate current = start;

		while (!current.isAfter(end))
		{
			final LocalDate date = current;

			TableColumn<GanttTableRow, String> dateColumn = new TableColumn<>(formatter.format(date));
			dateColumn.setPrefWidth(30);
			dateColumn.setMinWidth(30);
			dateColumn.setMaxWidth(30);
			dateColumn.setResizable(false);
			dateColumn.setReorderable(false);
			dateColumn.setSortable(false);

			dateColumn.setCellFactory(col -> new TableCell<>() {
				@Override
				protected void updateItem(String item, boolean empty)
				{
					super.updateItem(item, empty);

					if (empty)
					{
						setText(null);
						setStyle("");
					}
					else
					{
						GanttTableRow row = getTableRow().getItem();
						if (row != null && row.spansDate(date))
						{
							setText("x");
							setAlignment(Pos.CENTER);
							setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white;");
						}
						else
						{
							setText("");
							setStyle("");
						}
					}
				}
			});

			ganttTable.getColumns().add(dateColumn);
			current = current.plusDays(1);
		}
	}

	/**
	 * Builds the hierarchy of GanttTableRows from TaskGroupWithTasks.
	 * <p>
	 * Supports unlimited hierarchy depth through recursive building.
	 */
	private List<GanttTableRow> buildHierarchy(TaskGroupWithTasks taskGroup)
	{
		List<GanttTableRow> rootRows = new ArrayList<>();

		// Get main tasks (tasks without parent)
		List<TaskFlat> mainTasks = taskGroup.mainTasks();

		for (TaskFlat mainTask : mainTasks)
		{
			// Create main task row and recursively build children
			GanttTableRow mainRow = buildRowRecursively(mainTask, 0, null, taskGroup);
			rootRows.add(mainRow);
		}

		return rootRows;
	}

	/**
	 * Recursively builds a GanttTableRow with all its children.
	 *
	 * @param task the task for this row
	 * @param level the hierarchy level (0 = root, 1 = child, 2 = grandchild, etc.)
	 * @param parent the parent row (null for root tasks)
	 * @param taskGroup the task group containing all tasks
	 * @return the built row with all its children
	 */
	private GanttTableRow buildRowRecursively(
			TaskFlat task,
			int level,
			GanttTableRow parent,
			TaskGroupWithTasks taskGroup)
	{
		// Create row for this task
		GanttTableRow row = new GanttTableRow(task, level, parent);

		// Find and add children recursively
		List<TaskFlat> children = taskGroup.subTasksOf(task.id());
		for (TaskFlat child : children)
		{
			GanttTableRow childRow = buildRowRecursively(child, level + 1, row, taskGroup);
			row.addChild(childRow);
		}

		return row;
	}

	/**
	 * Updates the visible rows list based on expansion states.
	 */
	private void updateVisibleRows()
	{
		visibleRows.clear();

		for (GanttTableRow row : allRows)
		{
			addVisibleRows(row);
		}
	}

	/**
	 * Recursively adds visible rows to the visible list.
	 */
	private void addVisibleRows(GanttTableRow row)
	{
		// Add this row if visible
		if (row.isVisible())
		{
			visibleRows.add(row);

			// If expanded, add children
			if (row.isExpanded())
			{
				for (GanttTableRow child : row.getChildren())
				{
					addVisibleRows(child);
				}
			}
		}
	}
}

