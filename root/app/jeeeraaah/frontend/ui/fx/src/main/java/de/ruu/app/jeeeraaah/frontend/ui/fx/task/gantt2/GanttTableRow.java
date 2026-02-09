package de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt2;

import de.ruu.app.jeeeraaah.common.api.domain.TaskFlat;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a row in the Gantt TableView.
 *
 * <p>Each row contains:
 * <ul>
 *   <li>A TaskFlat (lightweight task data - no expensive relations)</li>
 *   <li>Hierarchy level (for indentation)</li>
 *   <li>Expandable state (can this row be expanded?)</li>
 *   <li>Expanded state (is this row currently expanded?)</li>
 *   <li>Visibility state (is this row currently visible?)</li>
 *   <li>Parent row reference (for hierarchy navigation)</li>
 * </ul>
 */
@Getter
public class GanttTableRow
{
	private final TaskFlat task;
	private final int level;
	private final GanttTableRow parent;
	private final List<GanttTableRow> children = new ArrayList<>();

	private final BooleanProperty expanded = new SimpleBooleanProperty(false);
	private final BooleanProperty visible = new SimpleBooleanProperty(true);

	/**
	 * Creates a root-level row (level 0, no parent).
	 */
	public GanttTableRow(TaskFlat task)
	{
		this(task, 0, null);
	}

	/**
	 * Creates a child row with specified level and parent.
	 */
	public GanttTableRow(TaskFlat task, int level, GanttTableRow parent)
	{
		this.task = task;
		this.level = level;
		this.parent = parent;

		// Main tasks with subtasks start expanded
		this.expanded.set(false);
	}

	/**
	 * Adds a child row to this row.
	 */
	public void addChild(GanttTableRow child)
	{
		children.add(child);
	}

	/**
	 * Returns true if this row has children (is expandable).
	 */
	public boolean hasChildren()
	{
		return !children.isEmpty();
	}

	/**
	 * Returns true if this row is expanded.
	 */
	public boolean isExpanded()
	{
		return expanded.get();
	}

	/**
	 * Sets the expanded state and updates children visibility.
	 */
	public void setExpanded(boolean value)
	{
		if (!hasChildren()) return; // Only expandable rows can be expanded

		expanded.set(value);
		updateChildrenVisibility();
	}

	/**
	 * Toggles the expanded state.
	 */
	public void toggleExpanded()
	{
		setExpanded(!isExpanded());
	}

	/**
	 * Returns the expanded property (for binding).
	 */
	public BooleanProperty expandedProperty()
	{
		return expanded;
	}

	/**
	 * Returns true if this row is visible.
	 */
	public boolean isVisible()
	{
		return visible.get();
	}

	/**
	 * Sets the visibility of this row.
	 */
	public void setVisible(boolean value)
	{
		visible.set(value);
	}

	/**
	 * Returns the visible property (for binding).
	 */
	public BooleanProperty visibleProperty()
	{
		return visible;
	}

	/**
	 * Updates visibility of all children based on this row's expanded state.
	 */
	private void updateChildrenVisibility()
	{
		for (GanttTableRow child : children)
		{
			child.setVisible(isExpanded());

			// If child is collapsed, hide its children too
			if (!child.isExpanded())
			{
				child.hideAllDescendants();
			}
		}
	}

	/**
	 * Hides all descendants recursively.
	 */
	private void hideAllDescendants()
	{
		for (GanttTableRow child : children)
		{
			child.setVisible(false);
			child.hideAllDescendants();
		}
	}

	/**
	 * Returns the task name for display.
	 */
	public String getTaskName()
	{
		return task.name();
	}

	/**
	 * Returns the task start date.
	 */
	public LocalDate getStartDate()
	{
		return task.start().orElse(null);
	}

	/**
	 * Returns the task end date.
	 */
	public LocalDate getEndDate()
	{
		return task.end().orElse(null);
	}

	/**
	 * Returns true if the task spans the given date.
	 */
	public boolean spansDate(LocalDate date)
	{
		LocalDate start = getStartDate();
		LocalDate end = getEndDate();

		if (start == null || end == null) return false;

		return !date.isBefore(start) && !date.isAfter(end);
	}

	@Override
	public String toString()
	{
		return "GanttTableRow{" +
				"task=" + task.name() +
				", level=" + level +
				", hasChildren=" + hasChildren() +
				", expanded=" + isExpanded() +
				", visible=" + isVisible() +
				'}';
	}
}

