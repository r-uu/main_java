package de.ruu.app.jeeeraaah.common.api.domain.flat;

import de.ruu.app.jeeeraaah.common.api.domain.TaskEntity;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroup;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupEntity;

import de.ruu.lib.jpa.core.Entity;
import de.ruu.lib.util.Strings;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static lombok.AccessLevel.NONE;

/**
 * Flat representation of a task group entity that doesn't include related tasks but the remaining relevant fields of
 * {@link TaskGroup}.
 * <p>
 * This is useful for clients that need to display or manipulate task groups without loading related tasks.
 */
public interface TaskGroupFlat extends Entity<Long>, Comparable<TaskGroupFlat>
{
	@NonNull String  name       ();
	Optional<String> description();

	@NonNull TaskGroupFlat name       (@NonNull  String name       );
	@NonNull TaskGroupFlat description(@Nullable String description);

	@Override default int compareTo(@NonNull TaskGroupFlat other) { return this.name().compareTo(other.name()); }

	@Getter
	@Setter
	@Accessors(fluent = true)
	public static class TaskGroupFlatSimple implements TaskGroupFlat
	{
		@Setter(NONE)
		private @Nullable  Long            id;
		@Setter(NONE)
		private @Nullable  Short           version;
		// no lombok-generation of setter because of additional validation in manually created method
		private @NonNull  String           name;
		private           Optional<String> description;

		public TaskGroupFlatSimple(@NonNull String name) { name(name); }

		public TaskGroupFlatSimple(@NonNull TaskGroupEntity<? extends TaskEntity<?, ?>> in)
		{
			this(in.name());
			id          = in.id();
			version     = in.version();
			description = in.description();
		}

		/**
		 * manually created fluent setter with extra parameter check (see throws documentation)
		 * @param name non-null, non-empty, non-blank
		 * @return {@code this}
		 * @throws IllegalArgumentException if {@code name} parameter is empty or blank
		 * @throws NullPointerException     if {@code name} parameter is {@code null}
		 */
		@Override @NonNull public TaskGroupFlatSimple name(@NonNull String name)
		{
			if (Strings.isEmptyOrBlank(name)) throw new IllegalArgumentException("name must not be empty nor blank");
			this.name = name;
			return this;
		}

		@Override public @NonNull TaskGroupFlat description(@Nullable String description)
		{
			this.description = Optional.ofNullable(description);
			return this;
		}
	}

	/**
	 * Extended flat representation that includes tasks for efficient hierarchy building.
	 * <p>
	 * Tasks are stored as {@link TaskFlat} instances (lightweight) instead of full TaskBean objects
	 * to avoid loading expensive relations (predecessors, successors, etc.).
	 */
	@Getter
	@Setter
	@Accessors(fluent = true)
	class TaskGroupWithTasks extends TaskGroupFlatSimple
	{
		private Set<TaskFlat> tasks = new HashSet<>();

		public TaskGroupWithTasks(@NonNull String name)
		{
			super(name);
		}

		public TaskGroupWithTasks(@NonNull TaskGroupEntity<? extends TaskEntity<?, ?>> taskGroup)
		{
			super(taskGroup);

			// Convert tasks to flat representation
			if (taskGroup.tasks().isPresent())
			{
				this.tasks = taskGroup.tasks().get().stream()
						.map(task -> new TaskFlat.TaskFlatSimple((TaskEntity<?, ?>) task))
						.collect(Collectors.toSet());
			}
		}

		/**
		 * Get main tasks (tasks without super task).
		 */
		public List<TaskFlat> mainTasks()
		{
			return tasks.stream()
					.filter(task -> task.superTaskId().isEmpty())
					.sorted(Comparator.comparing(TaskFlat::name))
					.toList();
		}

		/**
		 * Get subtasks of a given task.
		 */
		public List<TaskFlat> subTasksOf(@NonNull Long taskId)
		{
			return tasks.stream()
					.filter(task -> task.superTaskId().isPresent() &&
					                task.superTaskId().get().equals(taskId))
					.sorted(Comparator.comparing(TaskFlat::name))
					.toList();
		}
	}
}