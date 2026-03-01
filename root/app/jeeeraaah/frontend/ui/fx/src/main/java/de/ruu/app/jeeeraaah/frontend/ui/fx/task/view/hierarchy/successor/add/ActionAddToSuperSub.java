package de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy.successor.add;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.frontend.ui.fx.task.selector.TaskSelector;
import de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy.successor.add.ActionAdd.Context;
import de.ruu.lib.fx.control.dialog.ExceptionDialog;
import de.ruu.lib.ws_rs.NonTechnicalException;
import de.ruu.lib.ws_rs.TechnicalException;
import jakarta.enterprise.inject.spi.CDI;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TreeItem;
import lombok.NonNull;

import java.util.Optional;

import static javafx.scene.control.ButtonType.CANCEL;
import static javafx.scene.control.ButtonType.OK;

/**
 * <code>ActionAddToSuperSub</code> is responsible for adding a task as a new successor task to the selected super/sub
 * task.
 * <p>
 * It creates a dialog for the user to choose the receiver of the successor task, and upon confirmation, it creates a
 * new assignment between the receiver and its successor in the database.
 */
public class ActionAddToSuperSub
{
	private final Context context;

	private final TaskSelector taskSelector = CDI.current().select(TaskSelector.class).get();

	public ActionAddToSuperSub(@NonNull Context context) { this.context = context; }

	public void execute()
	{
		taskSelector.localRoot(); // ensure that the task selector is initialized
		taskSelector.service().populateFor(context.treeItemSelectedSuperSubTask().getValue().taskGroup());

		Dialog<TaskBean> dialog = new Dialog<>();
		dialog.setTitle("choose target task for new successor relation");
		dialog.setResultConverter(this::dialogResultConverter);

		DialogPane pane = dialog.getDialogPane();
		pane.setContent(taskSelector.localRoot());
		pane.getButtonTypes().addAll(CANCEL, OK);

		Optional<TaskBean> optional = dialog.showAndWait();

		if (optional.isPresent())
		{
			// fetch new task bean from dialog
			TaskBean chosenSuccessorTaskBean = optional.get();

			// fetch the selected super/sub task from the tree view
			TaskBean superSubTask = context.treeItemSelectedSuperSubTask().getValue();

			try
			{
				// let task service client create a new item in the backend
				context.taskServiceClient().addSuccessor(superSubTask, chosenSuccessorTaskBean);

				TreeItem<TaskBean> newItemInSuccessorTree = new TreeItem<>(chosenSuccessorTaskBean);

				// update the context
				context.treeItemSelectedSuccessorTask().getChildren().add(newItemInSuccessorTree);

				// update the tree view
				populateTreeForSuccessorsOf(newItemInSuccessorTree);

				// add the chosen successor task to the successor tasks of the selected super/sub task
				superSubTask.addSuccessor(chosenSuccessorTaskBean);
			}
			catch (TechnicalException | NonTechnicalException e)
			{
				ExceptionDialog.showAndWait
				(
						"failure creating new successor relation",
						"the successor relation between\n" + superSubTask + "\nand\n" +
								chosenSuccessorTaskBean + "\ncould not be created in the backend",
						e.getMessage(),
						e
				);
			}
		}
	}

	private void populateTreeForSuccessorsOf(TreeItem<TaskBean> newItemInSuccessorTree)
	{
		TaskBean successorTask = newItemInSuccessorTree.getValue();

		successorTask.successors().ifPresent
				(
						successors ->
								successors.forEach
										(
												successor ->
												{
													TreeItem<TaskBean> successorOfNewItemInSucccessorTree = new TreeItem<>(successor);
													newItemInSuccessorTree.getChildren().add(successorOfNewItemInSucccessorTree);
													populateTreeForSuccessorsOf(successorOfNewItemInSucccessorTree);
												}
										)
				);
	}

	private TaskBean dialogResultConverter(ButtonType btn)
	{
		return taskSelector.service().selectionChangedProperty().get();
	}
}