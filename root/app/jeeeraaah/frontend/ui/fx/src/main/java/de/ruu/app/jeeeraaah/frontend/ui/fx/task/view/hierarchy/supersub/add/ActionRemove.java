package de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy.supersub.add;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.TaskGroupServiceClient;
import de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.TaskServiceClient;
import de.ruu.lib.fx.control.dialog.AlertDialog;
import de.ruu.lib.ws_rs.NonTechnicalException;
import de.ruu.lib.ws_rs.TechnicalException;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static de.ruu.lib.util.BooleanFunctions.not;
import static javafx.scene.control.Alert.AlertType.ERROR;

@Slf4j
public class ActionRemove
{
	@Getter @Accessors(fluent = true)
	public static class Context
	{
		private TreeView<TaskBean>     superSubTreeView;
		private TaskGroupBean          activeTaskGroup;
		private TaskGroupServiceClient taskGroupServiceClient;
		private TaskServiceClient      taskServiceClient;

		public Context
				(
						@NonNull TreeView<TaskBean> superSubTreeView,
						@NonNull TaskGroupBean activeTaskGroup,
						@NonNull TaskGroupServiceClient taskGroupServiceClient,
						@NonNull TaskServiceClient taskServiceClient
				)
		{
			this.superSubTreeView       = superSubTreeView;
			this.activeTaskGroup        = activeTaskGroup;
			this.taskGroupServiceClient = taskGroupServiceClient;
			this.taskServiceClient      = taskServiceClient;
		}
	}

	private final Context context;

	public ActionRemove(@NonNull Context context) { this.context = context; }

	public void execute()
	{
		TreeItem<TaskBean> selectedItem = context.superSubTreeView().getSelectionModel().getSelectedItem();
		TaskBean           selectedTask = selectedItem.getValue();

		if (selectedTask.predecessors().isPresent() && not(selectedTask.predecessors().get().isEmpty()))
		{
			AlertDialog.showAndWait(
					"cannot remove task",
					"the selected task cannot be removed, because it has predecessor relations",
					"remove the predecessor relations first, then you can remove this task",
					ERROR
			);
			return;
		}

		if (selectedTask.successors().isPresent() && not(selectedTask.successors().get().isEmpty()))
		{
			AlertDialog.showAndWait(
					"cannot remove task",
					"the selected task cannot be removed, because it has successor relations",
					"remove the successor relations first, then you can remove this task",
					ERROR
			);
			return;
		}

		if (selectedTask.subTasks().isPresent() && not(selectedTask.subTasks().get().isEmpty()))
		{
			AlertDialog.showAndWait(
					"cannot remove task",
					"the selected task cannot be removed, because it has sub tasks",
					"remove the sub tasks first, then you can remove this task",
					ERROR
			);
			return;
		}

		if (selectedTask.superTask().isPresent())
		{
			TaskBean superTask = selectedTask.superTask().get();

			try
			{
				// remove the super-sub relation in the backend
				context.taskServiceClient.removeSubTask(superTask, selectedTask);

				// remove the super-sub relation in the current client-side beans
				// - remove the relation of the client-side task from the client-side super task
				superTask.removeSubTask(selectedTask);
				// - remove the task from the current client-side group
				boolean removeTaskFromActiveTaskGroupResult = context.activeTaskGroup.removeTask(selectedTask);
				// remove the super-sub relation in the tree view
				TreeItem<TaskBean> parent = selectedItem.getParent();
				if (parent != null) parent.getChildren().remove(selectedItem);
			}
			catch (TechnicalException | NonTechnicalException e)
			{
				AlertDialog.showAndWait(
						"failure removing task",
						"the selected task is still in its task group",
						"the selected task could not be removed from its task group",
						ERROR);
			}
		}
		else
		{
			// selected task is a main task
			if (context.activeTaskGroup.id() != null && selectedTask.id() != null)
			{
				try
				{
					// remove the super-sub relation in the backend
					context.taskGroupServiceClient.removeTaskFromGroup(context.activeTaskGroup().id(), selectedTask.id());
					// - remove the task from the current client-side group
					if (context.activeTaskGroup().removeTask(selectedTask))
					{
						// - remove the task from the tree view (from the root, because it is a main task)
						TreeItem<TaskBean> parent = selectedItem.getParent();
						if (parent != null) parent.getChildren().remove(selectedItem);
					}
				}
				catch (TechnicalException | NonTechnicalException e)
				{
					AlertDialog.showAndWait(
							"failure removing task",
							"the selected task is still in its task group",
							"the selected task could not be removed from its task group",
							ERROR);
				}
			}
		}
	}
}