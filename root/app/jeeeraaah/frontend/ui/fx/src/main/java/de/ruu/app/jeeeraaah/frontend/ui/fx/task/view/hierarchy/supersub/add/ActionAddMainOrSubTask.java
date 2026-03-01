package de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy.supersub.add;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.frontend.ui.fx.model.TaskFXBean;
import de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy.supersub.add.ActionAdd.Context;
import de.ruu.lib.fx.control.dialog.AlertDialog;
import de.ruu.lib.fx.control.dialog.ExceptionDialog;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import de.ruu.lib.ws_rs.NonTechnicalException;
import de.ruu.lib.ws_rs.TechnicalException;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import lombok.NonNull;

import java.util.Optional;

import static de.ruu.app.jeeeraaah.frontend.common.mapping.Mappings.toBean;
import static de.ruu.app.jeeeraaah.frontend.common.mapping.Mappings.toFXBean;
import static de.ruu.lib.util.BooleanFunctions.not;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;
import static javafx.scene.control.ButtonType.CANCEL;
import static javafx.scene.control.ButtonType.OK;

class ActionAddMainOrSubTask
{
	private final Context context;

	ActionAddMainOrSubTask(@NonNull Context context) { this.context = context; }

	void execute()
	{
		Dialog<TaskFXBean> dialog            = new Dialog<>();
		DialogPane         dialogPane        = dialog.getDialogPane();
		VBox               dialogPaneContent = new VBox();
		CheckBox           checkBox          = new CheckBox("create main task for task group");

		dialog           .setTitle("new main or sub task");
		dialog           .setHeaderText("create a new main task for the task group or a new sub task for the selected task");
		dialog           .setResultConverter(this::dialogResultConverterFXBean);
		dialogPane       .getButtonTypes().addAll(CANCEL, OK);
		dialogPane       .setContent(dialogPaneContent);
		dialogPaneContent.getChildren().add(context.taskEditor().localRoot());
		dialogPaneContent.getChildren().add(checkBox);
		checkBox         .setSelected(false);

		// create a transient task and put it into the editor
		// NOTE: each task (even transient tasks) has to be assigned to a task group
		//       taskBeanTransient will be assigned to the context.taskgroup tasks temporarily, it has to be removed later
		TaskBean   taskBeanTransient = new TaskBean(context.taskGroup(), "new main or sub task");
		TaskFXBean taskFXBean        = toFXBean(taskBeanTransient, new ReferenceCycleTracking());
		context.taskEditor().service().task(taskFXBean);

		Optional<TaskFXBean> optional = dialog.showAndWait();

		if (optional.isPresent())
		{
			// fetch new task fx bean from editor and convert it to a task bean
			TaskBean taskBeanFromEditor = toBean(optional.get(), new ReferenceCycleTracking());

			try
			{
				// let task service client create a new item in the backend
				TaskBean taskBeanFromBackend = context.taskServiceClient().create(taskBeanFromEditor);

				// update the tree view
				// - create a new tree item for the new main task
				TreeItem<TaskBean> taskBeanTreeItemNew = new TreeItem<>(taskBeanFromBackend);

				// - add the new tree item for the task as main or subtask to the tree view depending on the checkbox
				if (checkBox.isSelected())
						// create a main task in the tree
						context.treeView().getRoot().getChildren().add(taskBeanTreeItemNew);
				else
				{
					// otherwise create a subtask of the currently selected supertask
					// - fetch the supertask from the tree view
					TaskBean supertask = context.treeView().getSelectionModel().getSelectedItem().getValue();

					try
					{
						// let task service client create a new subtask relation in the backend
						context.taskServiceClient().addSubTask(supertask, taskBeanFromBackend);

						// update the context
						// - remove the temporary, transient task from the context task group (see note from above)
						boolean removeTaskResult = context.taskGroup().removeTask(taskBeanTransient);
						if (not(removeTaskResult))
						{
							AlertDialog.showAndWait
							(
									"failure removing transient task",
									"transient task might still be in context task group",
									"the transient task could not be removed from the context task group",
									ERROR
							);
						}

						// - set the task group of the backend task to the context task group, thereby the context task group
						//   receives the new persistent task as item of its tasks
						taskBeanFromBackend.taskGroup(context.taskGroup());

						// update the tree view
						// - add the new task as sub node of the supertask (the selected item in the tree view)
						context.treeView().getSelectionModel().getSelectedItem().getChildren().add(taskBeanTreeItemNew);

						// - select the new task in the tree view
						context.treeView().getSelectionModel().select(taskBeanTreeItemNew);
					}
					catch (TechnicalException | NonTechnicalException e)
					{
						ExceptionDialog.showAndWait
						(
								"failure creating new subtask relation",
								"the subtask relation between\n" + supertask + "\nand\n" +
										taskBeanFromEditor + "\ncould not be created in the backend, " +
										"however the subtask has been created in the backend",
								e.getMessage(),
								e
						);
					}
				}
			}
			catch (TechnicalException | NonTechnicalException e)
			{
				ExceptionDialog.showAndWait
				(
						"failure creating new task",
						"a task for\n" + taskBeanFromEditor + "\ncould not be created in the backend",
						e.getMessage(),
						e
				);
			}
		}
	}

	private TaskFXBean dialogResultConverterFXBean(ButtonType btn)
	{
		if (btn.getButtonData() == OK_DONE) return context.taskEditor().service().task().orElse(null);
		return null;
	}
}
