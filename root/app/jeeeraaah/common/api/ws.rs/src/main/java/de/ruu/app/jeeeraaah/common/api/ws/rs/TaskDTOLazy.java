package de.ruu.app.jeeeraaah.common.api.ws.rs;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.ruu.app.jeeeraaah.common.api.domain.TaskEntity;
import de.ruu.app.jeeeraaah.common.api.domain.flat.TaskFlat;
import de.ruu.app.jeeeraaah.common.api.domain.lazy.TaskGroupLazy;
import de.ruu.app.jeeeraaah.common.api.domain.lazy.TaskLazy;
import jakarta.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

// @formatter:off
/** Transfer object for tasks with the ids of their related tasks and their task group. */
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = ANY, isGetterVisibility = ANY)
@EqualsAndHashCode @ToString @Getter // generate getter methods for all fields using lombok unless configured otherwise
@Accessors(fluent = true)            // generate fluent accessors with lombok
public class TaskDTOLazy implements TaskLazy
// @formatter:on
{
	@JsonProperty("id")
	private @NonNull Long id;

	@JsonProperty("version")
	private @NonNull Short version;

	@JsonProperty("name")
	private @NonNull String name;

	@JsonProperty("closed")
	private @NonNull Boolean closed;

	@JsonProperty("description")
	private Optional<String> description = Optional.empty();

	@JsonProperty("start")
	private Optional<LocalDate> start = Optional.empty();

	@JsonProperty("end")
	private Optional<LocalDate> end = Optional.empty();

	@JsonProperty("taskGroupId")
	private @NonNull Long taskGroupId;

	@JsonProperty("superTaskId")
	private @Nullable Long superTaskId;

	@JsonProperty("subTaskIds")
	private final @NonNull Set<Long> subTaskIds = new HashSet<>();

	@JsonProperty("predecessorIds")
	private final @NonNull Set<Long> predecessorIds = new HashSet<>();

	@JsonProperty("successorIds")
	private final @NonNull Set<Long> successorIds = new HashSet<>();

	///////////////
	// constructors
	///////////////

	/** Default constructor for Jackson deserialization. */
	private TaskDTOLazy() { closed = false; }

	public TaskDTOLazy(TaskGroupLazy group, @NonNull String name)
	{
		taskGroupId = Objects.requireNonNull(group.id());
		this.name = name;
	}

	/**
	 * Constructor creating a task from an existing entity, preserving id, version and closed.
	 * @param group the task group this task belongs to
	 * @param in the existing task entity, must not be {@code null}
	 */
	public TaskDTOLazy(TaskGroupLazy group, @NonNull TaskEntity<?, ?> in)
	{
		this(group, in.name());
		this.id = in.id();
		this.version = in.version();
		this.closed = in.closed();
	}

	@Override public @NonNull TaskLazy name(@NonNull String name)
	{
		this.name = name;
		return this;
	}

	@Override public @NonNull TaskLazy description(@Nullable String description)
	{
		this.description = Optional.ofNullable(description);
		return this;
	}

	@Override public @NonNull TaskLazy start(@Nullable LocalDate startEstimated)
	{
		start = Optional.ofNullable(startEstimated);
		return this;
	}

	@Override public @NonNull TaskLazy end(@Nullable LocalDate finishEstimated)
	{
		end = Optional.ofNullable(finishEstimated);
		return this;
	}

	@Override public @NonNull TaskLazy closed(@NonNull Boolean closed)
	{
		this.closed = closed;
		return this;
	}

	public @NonNull Set<Long> subTaskIds    () { return Collections.unmodifiableSet(subTaskIds    ); }
	public @NonNull Set<Long> predecessorIds() { return Collections.unmodifiableSet(predecessorIds); }
	public @NonNull Set<Long> successorIds  () { return Collections.unmodifiableSet(successorIds  ); }

	// Override from TaskFlat - return Optional<Long> instead of @Nullable Long
	@Override public Optional<Long> superTaskId() { return Optional.ofNullable(superTaskId); }

	// Setter for superTaskId (fluent style) - returns TaskFlat as required by interface
	@Override public @NonNull TaskFlat superTaskId(@Nullable Long superTaskId)
	{
		this.superTaskId = superTaskId;
		return this;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////
	// java bean style accessors for those who do not work with fluent style accessors (mapstruct)
	//////////////////////////////////////////////////////////////////////////////////////////////

	public @NonNull String     getName()                                    { return name; }
	public            void     setName(@NonNull String name)                { this.name = name; }
	public @NonNull Boolean    getClosed()                                  { return closed; }
	public void                setClosed(@NonNull Boolean closed)           { this.closed = closed; }
	public Optional<String>    getDescription()                             { return description; }
	public void                setDescription(@Nullable String description) { this.description = Optional.ofNullable(description); }
	public Optional<LocalDate> getStart()                                   { return start; }
	public void                setStart(@Nullable LocalDate start)          { this.start = Optional.ofNullable(start); }
	public Optional<LocalDate> getEnd()                                     { return end; }
	public void                setEnd(@Nullable LocalDate end)              { this.end = Optional.ofNullable(end); }
	/** @return primary key, may be {@code null}, {@code null} indicates that entity was not (yet) persisted. */
	@Override public @Nullable Long           getId()           { return TaskLazy.super.getId(); }
	/** @return version , may be {@code null}, {@code null} indicates that entity was not (yet) persisted. */
	@Override public @Nullable Short          getVersion()      { return TaskLazy.super.getVersion(); }
	/** @return optional primary key, {@link Optional#empty()} indicates that entity was not (yet) persisted. */
	@Override public @NonNull Optional<Long>  optionalId()      { return TaskLazy.super.optionalId(); }
	/** @return optional version, {@link Optional#empty()} indicates that entity was not (yet) persisted. */
	@Override public @NonNull Optional<Short> optionalVersion() { return TaskLazy.super.optionalVersion(); }
	@Override public EntityInfo               entityInfo()      { return TaskLazy.super.entityInfo(); }
	@Override public boolean                  isPersisted()     { return TaskLazy.super.isPersisted(); }

	//////////////////////
	// mapstruct callbacks
	//////////////////////
}