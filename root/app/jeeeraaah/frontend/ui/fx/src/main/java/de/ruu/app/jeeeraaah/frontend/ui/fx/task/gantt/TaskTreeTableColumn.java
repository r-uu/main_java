package de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt;

import static de.ruu.lib.util.BooleanFunctions.not;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskTreeTableColumn extends TreeTableColumn<TaskTreeTableDataItem, String>
{
//	private final LocalDate date;

	// TODO find a way to get rid of this constructor
	public TaskTreeTableColumn(@NonNull String header)
	{
		super(header);
//		date = null;
		setCellValueFactory(cellValueFactory(LocalDate.MIN));
		setStyle("-fx-font-weight: normal;");
	}

	public TaskTreeTableColumn(@NonNull LocalDate date)
	{
		super("" + date.getDayOfMonth());
//		this.date = date;
		setCellValueFactory(cellValueFactory(date));
		setCellFactory(cellFactory());
		setStyle("-fx-font-weight: normal;");
	}

	private Callback<CellDataFeatures<TaskTreeTableDataItem, String>, ObservableValue<String>> cellValueFactory(
			LocalDate date)
	{
//		return new Callback<CellDataFeatures<TaskTreeTableDataItem, String>, ObservableValue<String>>()
		return cdfs ->
		{
			// navigate objects until cells are reached
			TreeItem<TaskTreeTableDataItem> treeItem      = cdfs.getValue();
			TaskTreeTableDataItem           tableDataItem = treeItem.getValue();
			List<TaskTreeTableCellData>     cells         = tableDataItem.cells();

			// find cell data that matches date
			Optional<TaskTreeTableCellData> optionalCellData =
					cells.stream().filter(tttcd -> tttcd.date().equals(date)).findFirst();

			if (optionalCellData.isPresent())
			{
				TaskTreeTableCellData cellData = optionalCellData.get();
				return new SimpleStringProperty(cellData.planningStatus().get().symbol());
			}
			return new SimpleStringProperty("???");
		};
	}

	private Callback<TreeTableColumn<TaskTreeTableDataItem, String>, TreeTableCell<TaskTreeTableDataItem, String>> cellFactory()
	{
//		return new Callback<TreeTableColumn<TaskTreeTableDataItem, String>, TreeTableCell<TaskTreeTableDataItem, String>>()
		return new Callback<>()
		{
			@Override public TreeTableCell<TaskTreeTableDataItem, String> call(TreeTableColumn<TaskTreeTableDataItem, String> treeTableColumn)
			{
				return new TreeTableCell<>()
				{
					@Override protected void updateItem(String item, boolean empty)
					{
						super.updateItem(item, empty);

						// Check if this row should be hidden due to collapsed parent
//						TreeItem<TaskTreeTableDataItem> treeItem     = getTreeTableRow().getTreeItem();
						TreeItem<TaskTreeTableDataItem> treeItem     = getTableRow().getTreeItem();
//						boolean                         isRowVisible = treeItem != null && isTreeItemVisible(treeItem);
						boolean                         isRowVisible = isTreeItemVisible(treeItem);

						if (not(empty) && isRowVisible)
						{
							if (item.equals(TaskTreeTableCellData.PlanningStatus.NO_ACTIVITY.symbol()))
							{
								setStyle("-fx-background-color: white");
								setTextFill(Color.BLACK);
							}
							else
							{
								setStyle("-fx-background-color: blue");
								setTextFill(Color.WHITE);
							}
							setAlignment(Pos.CENTER);
							setText(item);
						}
						else
						{
							setText(null);
							setGraphic(null);
							setStyle("");
						}
					}

					private boolean isTreeItemVisible(TreeItem<TaskTreeTableDataItem> item)
					{
						if (item == null) return false;
						TreeItem<TaskTreeTableDataItem> parent = item.getParent();
						while (parent != null && parent.getParent() != null) // don't check root
						{
							if (!parent.isExpanded()) return false;
							parent = parent.getParent();
						}
						return true;
					}
				};
			}
		};
	}
}
