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
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TreeItem;
import lombok.NonNull;

import java.util.Optional;

import static de.ruu.app.jeeeraaah.frontend.common.mapping.Mappings.toBean;
import static de.ruu.app.jeeeraaah.frontend.common.mapping.Mappings.toFXBean;
import static de.ruu.lib.util.BooleanFunctions.not;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;
import static javafx.scene.control.ButtonType.CANCEL;
import static javafx.scene.control.ButtonType.OK;

class ActionAddMainTask
{
	private final Context context;

	ActionAddMainTask(@NonNull Context context) { this.context = context; }

	void execute()
	{
		Dialog<TaskFXBean> dialog     = new Dialog<>();
		DialogPane         dialogPane = dialog.getDialogPane();

		dialog    .setTitle("new main task");
		dialog    .setHeaderText("create a new main task for the active task group");
		dialog    .setResultConverter(this::dialogResultConverterFXBean);
		dialogPane.getButtonTypes().addAll(CANCEL, OK);
		dialogPane.setContent(context.taskEditor().localRoot());

		// create a transient task and put it into the editor
		// NOTE: taskBeanTransient will be assigned to the context.taskgroup tasks temporarily, it has to be removed later
		TaskBean   taskBeanTransient = new TaskBean(context.taskGroup(), "new main task");
		TaskFXBean taskFXBean        = toFXBean(taskBeanTransient, new ReferenceCycleTracking());
		context.taskEditor().service().task(taskFXBean);

		Optional<TaskFXBean> optional = dialog.showAndWait();

		if (optional.isPresent())
		{
			// fetch new task bean from editor
			TaskBean taskBeanFromEditor = toBean(optional.get(), new ReferenceCycleTracking());

			try
			{
				// let task service client create a new item in the backend
				TaskBean taskBeanFromBackend = context.taskServiceClient().create(taskBeanFromEditor);

				// update the tree view
				// - create a new tree item for the new main task
				TreeItem<TaskBean> taskBeanTreeItemNew = new TreeItem<>(taskBeanFromBackend);
				// - add the new tree item with new main task to the root item of the tree view
				context.treeView().getRoot().getChildren().add(taskBeanTreeItemNew);

				// update the context
				// - remove the transient task from the context task group
				boolean removeTaskResult = context.taskGroup().removeTask(taskBeanTransient);
				if (not(removeTaskResult))
				{
					AlertDialog.showAndWait(
							"failure removing transient task",
							"transient task might still be in context task group",
							"the transient task could not be removed from the context task group",
							ERROR);
				}

				// - set the task group of the persistent task to the context task group, thereby the context task group receives
				//   the new persistent task as item of its tasks
				taskBeanFromBackend.taskGroup(context.taskGroup());
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