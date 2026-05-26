package de.ruu.app.jeeeraaah.common.api.ws.rs;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;

import java.io.Serial;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import de.ruu.app.jeeeraaah.common.api.domain.Task;
import de.ruu.app.jeeeraaah.common.api.domain.TaskEntity;
import de.ruu.app.jeeeraaah.common.api.domain.TaskRelationException;
import de.ruu.lib.jpa.core.AbstractEntity;
import de.ruu.lib.jpa.core.Entity;
import de.ruu.lib.util.Strings;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

// @formatter:off
/** data transfer object for fully serialised object graphs (including circular references) */
@JsonIdentityInfo( generator = ObjectIdGenerators.PropertyGenerator.class, property  = "jsonId", scope = TaskDTO.class)
@JsonAutoDetect(fieldVisibility = ANY)
@ToString
@Getter                   // generate getter methods for all fields using lombok unless configured otherwise
@Setter                   // generate setter methods for all fields using lombok unless configured otherwise
@Accessors(fluent = true) // generate fluent accessors with lombok and java-bean-style-accessors in non-abstract classes
                          // with ide, fluent accessors will (usually / by default) be ignored by mapstruct
public class TaskDTO implements TaskEntity<TaskGroupDTO, TaskDTO>
// @formatter:on
{
	@Serial
	private static final long serialVersionUID = 1L;

	@EqualsAndHashCode.Exclude
	private final UUID jsonId = randomUUID();

	// @formatter:off
	/**
	 * may be <pre>null</pre> if instance was not (yet) persisted.
	 * <p>
	 * may not be modified from outside type hierarchy (from non-{@link AbstractEntity}-subclasses)
	 * <p>
	 * not {@code final} or {@code @NonNull} because otherwise there has to be a constructor with {@code id}-parameter
	 */
	@EqualsAndHashCode.Include // documents intent of including id for equals() and hashCode() but both methods are
	                           // manually created
	@Setter(AccessLevel.NONE)
	// @formatter:on
	private @Nullable Long id;

	// @formatter:off
	/** may be <pre>null</pre> if {@link AbstractEntity} was not (yet) persisted. */
	@Setter(lombok.AccessLevel.NONE)
	// @formatter:on
	private @Nullable Short version;

	/** mutable non-null */
	// no lombok-generation of setter because of additional validation in manually
	// created method
	@Setter(AccessLevel.NONE)
	@NonNull  private String name;
	@Nullable private String description;
	@Nullable private LocalDate start;
	@Nullable private LocalDate end;
	@NonNull  private Boolean closed;

	/** mutable non-null */
	@NonNull
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@Setter(AccessLevel.NONE)
	private TaskGroupDTO taskGroup;

	/** mutable nullable */
	@Nullable
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@Getter(AccessLevel.NONE) // provide handmade getter that returns optional
	@Setter(AccessLevel.NONE) // provide handmade setter that handles bidirectional relation properly
	private TaskDTO superTask;

	/**
	 * prevent direct access to this modifiable set from outside this class
	 * <p>
	 * may explicitly be {@code null}, {@code null} indicates that there was no
	 * attempt to load related objects from db (lazy)
	 * <p>
	 * Read-only: sub-task membership is exclusively controlled via {@link #superTask(TaskDTO)}.
	 */
	@Nullable
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@Getter(AccessLevel.NONE) // provide handmade getter that returns unmodifiable
	@Setter(AccessLevel.NONE) // no setter at all
	private Set<TaskDTO> subTasks;

	/**
	 * prevent direct access to this modifiable set from outside this class, use
	 * {@link #addPredecessor(TaskDTO)} and
	 * {@link #removePredecessor(TaskDTO)} to modify the set
	 * <p>
	 * may explicitly be {@code null}, {@code null} indicates that there was no
	 * attempt to load related objects from db
	 * (lazy)
	 */
	@Nullable
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@Getter(AccessLevel.NONE) // provide handmade getter that
														// returns unmodifiable
	@Setter(AccessLevel.NONE) // no setter at all, use add method instead
	private Set<TaskDTO> predecessors;

	/**
	 * prevent direct access to this modifiable set from outside this class, use
	 * {@link #addSuccessor(TaskDTO)} and
	 * {@link #removeSuccessor(TaskDTO)} to modify the set
	 * <p>
	 * may explicitly be {@code null}, {@code null} indicates that there was no
	 * attempt to load related objects from db
	 * (lazy)
	 */
	@Nullable
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@Getter(AccessLevel.NONE) // provide handmade getter that
														// returns unmodifiable
	@Setter(AccessLevel.NONE) // no setter at all, use add method instead
	private Set<TaskDTO> successors;

	///////////////
	// constructors
	///////////////

	private TaskDTO() {
		closed(false);
	} // private - mapstruct must use ObjectFactory

	/** handmade required args constructor to properly handle relationships and required fields */
	public TaskDTO(@NonNull TaskGroupDTO taskGroup, @NonNull String name) {
		this();
		this.taskGroup = taskGroup;
		this.taskGroup.addTask(this);
		name(name);
	}

	/**
	 * create a new task dto from an existing task entity and a task group dto
	 * <p>
	 * This constructor is used by mapstruct to create a new task entity from an
	 * existing one.
	 * 
	 * @param taskGroup the task group dto for the new task dto, must not be
	 *                  {@code null}
	 * @param in        the existing task entity, must not be {@code null}
	 */
	public TaskDTO(@NonNull TaskGroupDTO taskGroup, @NonNull TaskEntity<?, ?> in) {
		this(taskGroup, in.name());
		this.id = in.id();
		this.version = in.version();
		this.closed = in.closed();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof TaskDTO other))
			return false;
		if (id != null && other.id != null)
			return id.equals(other.id);
		// otherwise compare by jsonId (survives JSON serialization)
		return jsonId.equals(other.jsonId);
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : jsonId.hashCode();
	}

	public boolean equalsWithFieldsIgnoreIds(TaskDTO other) {
		if (equalsIdentity(other))
			return true;

		// compare fields one by one but skip id and jsonId
		if (!Objects.equals(name, other.name))
			return false;
		if (!Objects.equals(description, other.description))
			return false;
		if (!Objects.equals(start, other.start))
			return false;
		if (!Objects.equals(end, other.end))
			return false;
		if (!Objects.equals(closed, other.closed))
			return false;

		return true;
	}

	public boolean equalsWithFieldsIgnoreJsonId(TaskDTO other) {
		// equalsIdentity(other) is called inside equalsWithFieldsIgnoreIds(other)
		if (!equalsWithFieldsIgnoreIds(other))
			return false;

		// compare id fields
		if (!Objects.equals(id, other.id))
			return false;

		return true;
	}

	public boolean equalsWithFieldsIgnoreId(TaskDTO other) {
		// equalsIdentity(other) is called inside equalsWithFieldsIgnoreIds(other)
		if (!equalsWithFieldsIgnoreIds(other))
			return false;

		// compare id fields
		if (!Objects.equals(jsonId, other.jsonId))
			return false;

		return true;
	}

	public boolean equalsWithFields(TaskDTO other) {
		// equalsIdentity(other) is called inside equalsWithFieldsIgnoreIds(other)
		if (!equalsWithFieldsIgnoreIds(other))
			return false;

		// compare id fields
		if (!Objects.equals(id, other.id))
			return false;
		if (!Objects.equals(jsonId, other.jsonId))
			return false;

		return true;
	}

	public boolean equalsIdentity(TaskDTO other) {
		if (this == other)
			return true;
		return false;
	}

	////////////////////////////////////////////////////////////////////////
	// fluent style accessors generated by lombok if not specified otherwise
	////////////////////////////////////////////////////////////////////////

	/**
	 * manually created fluent setter with extra parameter check (see throws
	 * documentation)
	 *
	 * @param name non-null, non-empty, non-blank
	 * @return {@code this}
	 * @throws IllegalArgumentException if {@code name} parameter is empty or blank
	 * @throws NullPointerException     if {@code name} parameter is {@code null}
	 */
	@NonNull
	public TaskDTO name(@NonNull String name) {
		if (Strings.isEmptyOrBlank(name))
			throw new IllegalArgumentException("name must not be empty nor blank");
		this.name = name;
		return this;
	}

	@Override
	@NonNull
	public TaskGroupDTO taskGroup() {
		return taskGroup;
	}

	@Override
	@NonNull
	public String name() {
		return name;
	}

	@Override
	public Optional<String> description() {
		return Optional.ofNullable(description);
	}

	@Override
	public Optional<LocalDate> start() {
		return Optional.ofNullable(start);
	}

	@Override
	public Optional<LocalDate> end() {
		return Optional.ofNullable(end);
	}

	@Override
	public Optional<TaskDTO> superTask() {
		return Optional.ofNullable(superTask);
	}

	/** @return {@link #subTasks wrapped in unmodifiable */
	@Override
	public Optional<Set<TaskDTO>> subTasks() {
		if (isNull(subTasks))
			return Optional.empty();
		return Optional.of(Collections.unmodifiableSet(subTasks));
	}

	// java bean style accessor for mapstruct compatibility
	@Nullable
	public Set<TaskDTO> getSubTasks() { return subTasks; }

	/** @return {@link #predecessors} wrapped in unmodifiable */
	@Override
	public Optional<Set<TaskDTO>> predecessors() {
		if (isNull(predecessors))
			return Optional.empty();
		return Optional.of(Collections.unmodifiableSet(predecessors));
	}

	// java bean style accessor for mapstruct compatibility
	@Nullable
	public Set<TaskDTO> getPredecessors() { return predecessors; }

	/** @return {@link #successors} wrapped in unmodifiable */
	@Override
	public Optional<Set<TaskDTO>> successors() {
		if (isNull(successors))
			return Optional.empty();
		return Optional.of(Collections.unmodifiableSet(successors));
	}

	// java bean style accessor for mapstruct compatibility
	@Nullable
	public Set<TaskDTO> getSuccessors() { return successors; }

	///////////////////////
	// additional accessors
	///////////////////////

	@Override
	public @NonNull TaskDTO taskGroup(@NonNull TaskGroupDTO taskGroup) {
		// before setting task group of this task check, if this task is not already
		// contained in task groups tasks
		if (taskGroup.tasks().isPresent()) {
			// create a new HashSet with most recent hash-codes even for elements that might
			// be modified while this code is
			// running
			HashSet<TaskDTO> tasksInGroup = new HashSet<>(taskGroup.tasks().get());
			if (tasksInGroup.contains(this))
				return this; // do nothing
		}

		// remove this from present task group tasks
		this.taskGroup.removeTask(this);
		// assign new taskgroup
		this.taskGroup = taskGroup;
		// add this to new taskgroups tasks
		taskGroup.addTask(this);

		return this;
	}

	////////////////////////
	// relationship handling
	////////////////////////

	/** @throws TaskRelationException if {@code newSuperTask} is {@code this} */
	@Override
	public @NonNull TaskDTO superTask(@Nullable TaskDTO newSuperTask) throws TaskRelationException
	{
		if (newSuperTask == this)
			throw new TaskRelationException("task can not be super task of itself");

		// No-op: already the same parent
		if (this.superTask == newSuperTask)
			return this;

		if (newSuperTask != null)
		{
			if (predecessorsContains(newSuperTask))
				throw new TaskRelationException("super task can not be predecessor of same task");
			if (successorsContain(newSuperTask))
				throw new TaskRelationException("super task can not be successor of same task");

			// Cycle guard: walk UP from newSuperTask; if we reach 'this', it would close a cycle.
			TaskDTO cursor = newSuperTask;
			while (cursor != null)
			{
				if (cursor.equals(this))
					throw new TaskRelationException(
							"setting super task would create a cycle in the task hierarchy: "
							+ "task with id " + newSuperTask.id() + " is already a descendant of task with id " + this.id());
				cursor = cursor.superTask;
			}
		}

		// Remove this from old parent's subTasks collection.
		if (this.superTask != null && this.superTask.subTasks != null)
			this.superTask.subTasks.remove(this);

		// Update owning side.
		this.superTask = newSuperTask;

		// Add to new parent's subTasks collection.
		if (newSuperTask != null)
			newSuperTask.nonNullSubTasks().add(this);

		return this;
	}

	/**
	 * @param task the {@link Task} to be added as predecessor
	 * @return {@code this}
	 * @throws TaskRelationException if {@code task} is identical to {@code this}
	 *                               task
	 * @throws TaskRelationException if {@code task} is already predecessor of
	 *                               {@code this} task
	 * @throws TaskRelationException if {@code task} is a child of {@code this} task
	 */
	@Override
	public boolean addPredecessor(@NonNull TaskDTO task) throws TaskRelationException {
		if (task.equals(this))
			throw new TaskRelationException("task can not be predecessor of itself");
		if (successorsContain(task))
			throw new TaskRelationException("predecessor can not be successor of the same task");
		if (subTasksContain(task))
			throw new TaskRelationException("predecessor can not be sub task of the same task");

		if (predecessorsContains(task))
			return false; // no-op

		// Cycle guard: walk successors of 'this' transitively (in-memory).
		// If 'task' is reachable, then this →...→ task already and adding task → this would close a cycle.
		if (isSuccessorReachable(this, task))
			throw new TaskRelationException(
					"adding predecessor would create a cycle in the predecessor/successor relationship");

		// update bidirectional relation
		if (task.nonNullSuccessors().add(this)) {
			nonNullPredecessors().add(task);
			return true;
		} else {
			// this might already be among successors of task, if so return true
			if (task.nonNullSuccessors().contains(this))
				return true;
		}

		throw new IllegalStateException("could not add this to successors of task");
	}

	/**
	 * @param task the {@link Task} to be added as predecessor
	 * @return {@code true} if operation succeeded, {@code false} otherwise
	 * @throws TaskRelationException if {@code task} is identical to {@code this}
	 *                               task
	 * @throws TaskRelationException if {@code task} is already predecessor of
	 *                               {@code this} task
	 * @throws TaskRelationException if {@code task} is a child of {@code this} task
	 */
	@Override
	public boolean addSuccessor(@NonNull TaskDTO task) throws TaskRelationException {
		if (task.equals(this))
			throw new TaskRelationException("task can not be successor of itself");
		if (predecessorsContains(task))
			throw new TaskRelationException("successor can not be predecessor of the same task");
		if (subTasksContain(task))
			throw new TaskRelationException("predecessor can not be sub task of the same task");

		if (successorsContain(task))
			return false; // no-op

		// Cycle guard: walk successors of 'task' transitively (in-memory).
		// If 'this' is reachable, then task →...→ this already and adding this → task would close a cycle.
		if (isSuccessorReachable(task, this))
			throw new TaskRelationException(
					"adding successor would create a cycle in the predecessor/successor relationship");

		// update bidirectional relation
		if (task.nonNullPredecessors().add(this)) {
			nonNullSuccessors().add(task);
			return true;
		} else {
			// this might already be among predecessors of task, if so return true
			if (task.nonNullPredecessors().contains(this))
				return true;
		}

		throw new IllegalStateException("could not add this to predecessors of task");
	}


	@Override
	public boolean removePredecessor(@NonNull TaskDTO dto) {
		if (nonNull(dto.successors))
			if (dto.successors.removeAll(List.of(this))) // hibernate removal of single element fails
				if (nonNull(predecessors))
					return predecessors.removeAll(List.of(dto)); // hibernate removal of single element fails
				else
					throw new IllegalStateException("no predecessors exist, dto id: " + dto.id());
			else
				throw new IllegalArgumentException("could not remove from successors, dto id: " + dto.id());
		else
			throw new IllegalStateException("no successors exists, dto id: " + dto.id());
	}

	@Override
	public boolean removeSuccessor(@NonNull TaskDTO dto) {
		if (nonNull(dto.predecessors))
			if (dto.predecessors.removeAll(List.of(this))) // hibernate removal of single element fails
				if (nonNull(successors))
					return successors.removeAll(List.of(dto)); // hibernate removal of single element fails
				else
					throw new IllegalStateException("no successors exist, dto id: " + dto.id());
			else
				throw new IllegalArgumentException("could not remove from predecessors, dto id: " + dto.id());
		else
			throw new IllegalStateException("no predecessors exists, dto id: " + dto.id());
	}

	///////////////////////
	// additional accessors
	///////////////////////

	//////////////////////
	// NOTE: JavaBean-style accessors removed — use fluent API (e.g. `description(...)`, `description()`) instead.
	// Lombok + lombok-mapstruct-binding is expected to provide fluent accessors where applicable.

	protected void mapEntityFields(Entity<Long> input) {
		id = input.id();
		version = input.version();
	}

	//////////////////////
	// mapstruct callbacks
	//////////////////////

	@NonNull
	private Set<TaskDTO> nonNullSubTasks() {
		if (isNull(subTasks))
			subTasks = new HashSet<>();
		return subTasks;
	}

	@NonNull
	private Set<TaskDTO> nonNullPredecessors() {
		if (isNull(predecessors))
			predecessors = new HashSet<>();
		return predecessors;
	}

	@NonNull
	private Set<TaskDTO> nonNullSuccessors() {
		if (isNull(successors))
			successors = new HashSet<>();
		return successors;
	}

	/** {@code null} safe check for containment */
	private boolean predecessorsContains(TaskDTO entity) {
		if (isNull(predecessors))
			return false;
		return predecessors.contains(entity);
	}

	/** {@code null} safe check for containment */
	private boolean successorsContain(TaskDTO entity) {
		if (isNull(successors))
			return false;
		return successors.contains(entity);
	}

	/** {@code null} safe check for containment */
	private boolean subTasksContain(TaskDTO entity) {
		if (isNull(subTasks))
			return false;
		return subTasks.contains(entity);
	}

	/**
	 * Iterative DFS: returns {@code true} if {@code target} is reachable from {@code start}
	 * by following successor references.
	 */
	private boolean isSuccessorReachable(@NonNull TaskDTO start, @NonNull TaskDTO target) {
		Set<TaskDTO>   visited = new HashSet<>();
		Deque<TaskDTO> stack   = new ArrayDeque<>();
		if (start.successors != null) stack.addAll(start.successors);
		while (!stack.isEmpty()) {
			TaskDTO current = stack.pop();
			if (current.equals(target)) return true;
			if (!visited.add(current))  continue;
			if (current.successors != null) stack.addAll(current.successors);
		}
		return false;
	}
}