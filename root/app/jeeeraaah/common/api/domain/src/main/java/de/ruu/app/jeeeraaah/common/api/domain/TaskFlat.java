package de.ruu.app.jeeeraaah.common.api.domain;

import de.ruu.lib.jpa.core.Entity;
import jakarta.annotation.Nullable;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Flat representation of a task without expensive relations (predecessors, successors, etc.).
 * <p>
 * This is optimized for performance when building hierarchies - contains only the essential fields
 * needed for Gantt chart display: id, name, dates, and parent reference.
 */
public interface TaskFlat extends Entity<Long>
{
	@NonNull String         name();
	Optional<String>        description();
	Optional<LocalDate>     start();
	Optional<LocalDate>     end();
	Optional<Long>          superTaskId(); // Just the ID, not the full object!

	@NonNull TaskFlat name       (@NonNull  String    name);
	@NonNull TaskFlat description(@Nullable String    description);
	@NonNull TaskFlat start      (@Nullable LocalDate start);
	@NonNull TaskFlat end        (@Nullable LocalDate end);
	@NonNull TaskFlat superTaskId(@Nullable Long      superTaskId);

	@Getter
	@Setter
	@Accessors(fluent = true)
	@NoArgsConstructor
	@AllArgsConstructor
	@EqualsAndHashCode
	@ToString
	class TaskFlatSimple implements TaskFlat
	{
		@Setter(AccessLevel.NONE)
		private @Nullable Long            id;
		@Setter(AccessLevel.NONE)
		private @Nullable Short           version;
		private @NonNull  String          name;
		private           Optional<String>    description = Optional.empty();
		private           Optional<LocalDate> start       = Optional.empty();
		private           Optional<LocalDate> end         = Optional.empty();
		private           Optional<Long>      superTaskId = Optional.empty();

		public TaskFlatSimple(@NonNull String name)
		{
			this.name = name;
		}

		/**
		 * Create from a TaskEntity (TaskBean, TaskJPA, etc.)
		 */
		public TaskFlatSimple(@NonNull TaskEntity<?, ?> task)
		{
			this.id          = task.id();
			this.version     = task.version();
			this.name        = task.name();
			this.description = task.description();
			this.start       = task.start();
			this.end         = task.end();
			this.superTaskId = task.superTask().map(t -> t.id());
		}

		@Override public @NonNull TaskFlat name(@NonNull String name)
		{
			this.name = name;
			return this;
		}

		@Override public @NonNull TaskFlat description(@Nullable String description)
		{
			this.description = Optional.ofNullable(description);
			return this;
		}

		@Override public @NonNull TaskFlat start(@Nullable LocalDate start)
		{
			this.start = Optional.ofNullable(start);
			return this;
		}

		@Override public @NonNull TaskFlat end(@Nullable LocalDate end)
		{
			this.end = Optional.ofNullable(end);
			return this;
		}

		@Override public @NonNull TaskFlat superTaskId(@Nullable Long superTaskId)
		{
			this.superTaskId = Optional.ofNullable(superTaskId);
			return this;
		}
	}
}

