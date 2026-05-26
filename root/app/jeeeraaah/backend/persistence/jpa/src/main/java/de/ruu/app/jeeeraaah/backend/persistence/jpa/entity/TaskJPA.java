package de.ruu.app.jeeeraaah.backend.persistence.jpa.entity;

import static java.lang.System.identityHashCode;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import de.ruu.app.jeeeraaah.common.api.domain.Task;
import de.ruu.app.jeeeraaah.common.api.domain.TaskEntity;
import de.ruu.app.jeeeraaah.common.api.domain.TaskRelationException;
import de.ruu.lib.jpa.core.AbstractEntity;
import de.ruu.lib.util.Strings;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
@Getter // generate getter methods for all fields using lombok unless configured
				// otherwise ({@code
@Setter // generate setter methods for all fields using lombok unless configured
				// otherwise ({@code
				// @Setter(AccessLevel.NONE}))
@Accessors(fluent = true) // generate fluent accessors with lombok and java-bean-style-accessors in
													// non-abstract classes
													// with ide, fluent accessors will (usually / by default) be ignored by
													// mapstruct
// @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true) // generate
// no args constructor for jsonb, jaxb, jpa, mapstruct, ...
@Entity
@Table(name = "task")
public class TaskJPA implements TaskEntity<TaskGroupJPA, TaskJPA>
{
	/**
	 * may be <pre>null</pre> if instance was not (yet) persisted.
	 * <p>
	 * may not be modified from outside type hierarchy (from non-{@link AbstractEntity}-subclasses)
	 * <p>
	 * not {@code final} or {@code @NonNull} because otherwise there has to be a constructor with {@code id}-parameter
	 */
	@EqualsAndHashCode.Include // documents intent of including id for equals() and hashCode() but both methods are
	                           // manually created
	@Nullable
	@Setter(AccessLevel.NONE)
	@Id
	@GeneratedValue
	private Long id;

	/** may be <pre>null</pre> if {@link AbstractEntity} was not (yet) persisted. */
	@Nullable
	@Setter(AccessLevel.NONE)
	@Version
	@Column(nullable = false)
	private Short version;

	/** mutable non-null */
	// no lombok-generation of setter because of additional validation in manually
	// created method
	@Setter(AccessLevel.NONE)
	@NonNull
	private String name;
	@Nullable
	private String description;
	@Nullable
	private LocalDate start;
	@Column(name = "\"end\"") // "end" is a reserved keyword in postgresql, so force jpa to use quoted name
	@Nullable
	private LocalDate end;
	@NonNull
	private Boolean closed;

	/** mutable non-null */
	// no java-bean-style getter here, mapstruct will ignore fields without
	// bean-style-accessor so mapping can be
	// controlled in beforeMapping
	@NonNull
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@ManyToOne
	@JoinColumn(name = "idTaskGroup")
	private TaskGroupJPA taskGroup;

	/** mutable nullable */
	@Nullable
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@Getter(AccessLevel.NONE) // provide handmade getter that returns optional
	@Setter(AccessLevel.NONE) // provide handmade setter that handles bidirectional relation properly
	@ManyToOne
	@JoinColumn(name = "idSuperTask")
	private TaskJPA superTask;

	/**
	 * prevent direct access to this modifiable set from outside this class
	 * <p>
	 * may explicitly be {@code null}, {@code null} indicates that there was no
	 * attempt to load related objects from db (lazy)
	 * <p>
	 * Read-only from outside: sub-task membership is exclusively controlled via
	 * {@link #superTask(TaskJPA)}.
	 */
	@Nullable
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@Getter(AccessLevel.NONE) // provide handmade getter that returns unmodifiable
	@Setter(AccessLevel.NONE) // no setter at all, use add method instead
	@OneToMany
	(
			mappedBy = "superTask",
			// do not use cascade REMOVE in to-many relations as this may result in
			// cascading deletes that wipe out both sides
			// of the relation entirely
			cascade = { CascadeType.PERSIST, CascadeType.MERGE }
	)
	private Set<TaskJPA> subTasks;

	/**
	 * prevent direct access to this modifiable set from outside this class, use
	 * {@link #addPredecessor(TaskJPA)} and
	 * {@link #removePredecessor(TaskJPA)} to modify the set
	 * <p>
	 * may explicitly be {@code null}, {@code null} indicates that there was no
	 * attempt to load related objects from db
	 * (lazy)
	 */
	@Nullable
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@Getter(AccessLevel.NONE) // provide handmade getter that returns unmodifiable
	@Setter(AccessLevel.NONE) // no setter at all
	@ManyToMany
	(
			// mappedBy = TaskEntityJPA_.SUCCESSORS,
			// do not use cascade REMOVE in to-many relations as this may result in
			// cascading deletes that wipe out both sides
			// of the relation entirely
			cascade = { CascadeType.PERSIST, CascadeType.MERGE }
	)
	@JoinTable
	(
			name               = "PREDECESSOR_SUCCESSOR",
			joinColumns        = { @JoinColumn(name = "idPredecessor") },
			inverseJoinColumns = { @JoinColumn(name = "idSuccessor"  ) }
	)
	private Set<TaskJPA> predecessors;

	/**
	 * prevent direct access to this modifiable set from outside this class, use
	 * {@link #addSuccessor(TaskJPA)} and
	 * {@link #removeSuccessor(TaskJPA)} to modify the set
	 * <p>
	 * may explicitly be {@code null}, {@code null} indicates that there was no
	 * attempt to load related objects from db
	 * (lazy)
	 */
	@Nullable
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@Getter(AccessLevel.NONE) // provide handmade getter that returns unmodifiable
	@Setter(AccessLevel.NONE) // no setter at all, use add method instead
	@ManyToMany
	(
			mappedBy = "predecessors",
			// do not use cascade REMOVE in to-many relations as this may result in
			// cascading deletes that wipe out both sides
			// of the relation entirely
			cascade = { CascadeType.PERSIST, CascadeType.MERGE }
	)
	private Set<TaskJPA> successors;

	///////////////
	// constructors
	///////////////

	protected TaskJPA() {
		closed(false);
	} // private does not work for hibernate

	/**
	 * provide handmade required args constructor to properly handle relationships
	 */
	public TaskJPA(@NonNull TaskGroupJPA taskGroup, @NonNull String name)
	{
		this();
		this.taskGroup = taskGroup;
		this.taskGroup.addTask(this);
		name(name);
	}

	/**
	 * create a new task entity from an existing entity (DTO, Bean, etc.)
	 * <p>
	 * This constructor is used by mapstruct to create a new task entity from an existing one.
	 * It preserves the id and version from the source entity.
	 * 
	 * @param taskGroup the task group this task belongs to
	 * @param in the existing entity, must not be {@code null}
	 */
	public TaskJPA(@NonNull TaskGroupJPA taskGroup, @NonNull TaskEntity<?, ?> in)
	{
		this(taskGroup, in.name());
		this.id      = in.id();
		this.version = in.version();
		this.closed  = in.closed();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)                      return true;
		if (!(o instanceof TaskJPA other))  return false;
		if (id != null && other.id != null) return id.equals(other.id);
		return false;
	}

	@Override
	public int hashCode() {
		return (id != null) ? id.hashCode() : identityHashCode(this);
	}

	public boolean equalsWithFieldsIgnoreId(TaskJPA other)
	{
		if (equalsIdentity(other)) return true;

		// compare fields one by one but skip id
		if (!Objects.equals(name       , other.name       )) return false;
		if (!Objects.equals(description, other.description)) return false;
		if (!Objects.equals(start      , other.start      )) return false;
		if (!Objects.equals(end        , other.end        )) return false;
		if (!Objects.equals(closed     , other.closed     )) return false;

		return true;
	}

	public boolean equalsWithFields(TaskJPA other)
	{
		// equalsIdentity(other) is called inside equalsWithFieldsIgnoreId(other)
		if (!equalsWithFieldsIgnoreId(other)) return false;
		// compare id fields
		if (!Objects.equals(id, other.id))    return false;

		return true;
	}

	public boolean equalsIdentity(TaskJPA other)
	{
		if (this == other) return true;
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
	public TaskJPA name(@NonNull String name)
	{
		if (Strings.isEmptyOrBlank(name)) throw new IllegalArgumentException("name must not be empty nor blank");
		this.name = name;
		return this;
	}

	@Override public @NonNull TaskGroupJPA taskGroup()   { return taskGroup; }
	@Override public @NonNull String       name()        { return name; }
	@Override public Optional<String>      description() {
		return Optional.ofNullable(description);
	}
	@Override public Optional<LocalDate>   start()       { return Optional.ofNullable(start); }
	@Override public Optional<LocalDate>   end()         { return Optional.ofNullable(end); }
	@Override public Optional<TaskJPA>     superTask()   { return Optional.ofNullable(superTask); }

	/** @return {@link #subTasks wrapped in unmodifiable */
	@Override
	public Optional<Set<TaskJPA>> subTasks() {
		if (isNull(subTasks))
			return Optional.empty();
		return Optional.of(Collections.unmodifiableSet(subTasks));
	}

	/** @return {@link #predecessors} wrapped in unmodifiable */
	@Override
	public Optional<Set<TaskJPA>> predecessors() {
		if (isNull(predecessors))
			return Optional.empty();
		return Optional.of(Collections.unmodifiableSet(predecessors));
	}

	/** @return {@link #successors} wrapped in unmodifiable */
	@Override
	public Optional<Set<TaskJPA>> successors() {
		if (isNull(successors))
			return Optional.empty();
		return Optional.of(Collections.unmodifiableSet(successors));
	}

	///////////////////////
	// additional accessors
	///////////////////////

	@Override
	public @NonNull TaskJPA taskGroup(@NonNull TaskGroupJPA taskGroup) {
		if (taskGroup.tasks().isPresent()) {
			// create a new HashSet with most recent hash-codes even for elements that might
			// be modified while this code is
			// running
			Set<TaskJPA> tasksInGroup = new HashSet<>(taskGroup.tasks().get());
			if (tasksInGroup.contains(this))
				return this; // do nothing
		}
		this.taskGroup.removeTask(this);
		this.taskGroup = taskGroup;
		taskGroup.addTask(this);
		return this;
	}

	////////////////////////
	// relationship handling
	////////////////////////

	/**
	 * Sets the super task (parent) of this task.
	 * <p>
	 * This is the <strong>single entry-point</strong> for managing the super/sub-task hierarchy.
	 * Pass {@code null} to detach this task from its current parent (making it a root task).
	 * <p>
	 * <strong>Cycle guard:</strong> before any mutation, this method walks <em>upward</em>
	 * from {@code newSuperTask} via superTask references. If {@code this} is encountered
	 * in that chain, the proposed link would close a cycle and a {@link TaskRelationException}
	 * is thrown. Complexity: O(depth) — no full DFS required because each task has at most
	 * one parent.
	 * <p>
	 * <strong>Bidirectional consistency:</strong> the method removes this task from the old
	 * parent's {@code subTasks} collection and adds it to the new parent's collection, so
	 * both sides of the {@code @OneToMany}/{@code @ManyToOne} relationship stay in sync.
	 *
	 * @param newSuperTask the new parent task, or {@code null} to make this task a root
	 * @return {@code this} for fluent chaining
	 * @throws TaskRelationException if {@code newSuperTask} is {@code this}
	 * @throws TaskRelationException if {@code newSuperTask} is a predecessor of this task
	 * @throws TaskRelationException if {@code newSuperTask} is a successor of this task
	 * @throws TaskRelationException if setting {@code newSuperTask} would create a cycle
	 */
	@Override
	public @NonNull TaskJPA superTask(@Nullable TaskJPA newSuperTask) throws TaskRelationException
	{
		if (newSuperTask == this)
			throw new TaskRelationException("task can not be super task of itself");

		// No-op: already the same parent (covers null == null and same-reference)
		if (this.superTask == newSuperTask)
			return this;

		if (newSuperTask != null)
		{
			if (predecessorsContains(newSuperTask))
				throw new TaskRelationException("super task can not be predecessor of same task");
			if (successorsContain(newSuperTask))
				throw new TaskRelationException("super task can not be successor of same task");

			// Cycle guard: walk UP from newSuperTask.
			// If we reach 'this', then newSuperTask is already below 'this' in the tree —
			// linking would close a cycle.
			TaskJPA cursor = newSuperTask;
			while (cursor != null)
			{
				if (cursor.equals(this))
					throw new TaskRelationException(
							"setting super task would create a cycle in the task hierarchy: "
							+ "task with id " + newSuperTask.id() + " is already a descendant of task with id " + this.id());
				cursor = cursor.superTask;
			}
		}

		// ── Safe to proceed ──────────────────────────────────────────────────────
		// Remove this from old parent's subTasks collection (direct field access for
		// Hibernate-compatible removal; avoids recursive calls).
		if (this.superTask != null && this.superTask.subTasks != null)
			this.superTask.subTasks.removeAll(List.of(this));

		// Update the owning side (the FK column idSuperTask) — this is what JPA persists.
		this.superTask = newSuperTask;

		// Add to new parent's subTasks collection for in-memory consistency.
		if (newSuperTask != null)
			newSuperTask.nonNullSubTasks().add(this);

		return this;
	}

	/**
	 * Parent-centric convenience method: adds {@code subTask} as a child of this task.
	 * <p>
	 * Equivalent to {@code subTask.superTask(this)}.  The cycle guard and all validation
	 * is performed inside {@link #superTask(TaskJPA)}.
	 *
	 * @param subTask the task to add as a child of {@code this}
	 * @return {@code true} if the relation was newly created,
	 *         {@code false} if {@code subTask} was already a child of {@code this} (no-op)
	 * @throws TaskRelationException if {@code subTask} is {@code this}
	 * @throws TaskRelationException if adding {@code subTask} would create a cycle
	 */
	public boolean addSubTask(@NonNull TaskJPA subTask) throws TaskRelationException
	{
		if (subTask.superTask == this) return false; // already a child — idempotent no-op
		subTask.superTask(this);                      // validation + mutation
		return true;
	}

	/**
	 * @param task the {@link Task} to be added as predecessor
	 * @return {@code true} if operation succeeded, {@code false} otherwise
	 * @throws TaskRelationException if {@code task} is {@code this} task
	 * @throws TaskRelationException if {@code task} is already predecessor of
	 *                               {@code this} task
	 * @throws TaskRelationException if {@code task} is a sub task of {@code this}
	 *                               task
	 * @throws IllegalStateException if bidirectional relation could not be
	 *                               established
	 */
	@Override
	public boolean addPredecessor(@NonNull TaskJPA task) throws TaskRelationException
	{
		if (task.equals(this))       throw new TaskRelationException("task can not be predecessor of itself");
		if (successorsContain(task)) throw new TaskRelationException("predecessor can not be successor of the same task");
		if (subTasksContain(task))   throw new TaskRelationException("predecessor can not be sub task of the same task");

		if (predecessorsContains(task))
		{
			log.warn("predecessors already contain task");
			return false; // no-op
		}

		// Cycle guard: walk successors of 'this' transitively (in-memory only).
		// If 'task' is reachable, then this →...→ task already exists and adding task → this would close a cycle.
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
	 * @param task the {@link Task} to be added as successor
	 * @return {@code true} if operation succeeded, {@code false} otherwise
	 * @throws TaskRelationException if {@code task} is {@code this} task
	 * @throws TaskRelationException if {@code task} is already successor of
	 *                               {@code this} task
	 * @throws TaskRelationException if {@code task} is a sub task of {@code this}
	 *                               task
	 * @throws IllegalStateException if bidirectional relation could not be
	 *                               established
	 */
	@Override
	public boolean addSuccessor(@NonNull TaskJPA task) throws TaskRelationException {
		if (task.equals(this))
			throw new TaskRelationException("task can not be successor of itself");
		if (predecessorsContains(task))
			throw new TaskRelationException("successor can not be predecessor of the same task");
		if (subTasksContain(task))
			throw new TaskRelationException("successor can not be sub task of the same task");

		if (successorsContain(task))
			return false; // no-op

		// Cycle guard: walk successors of 'task' transitively (in-memory only).
		// If 'this' is reachable, then task →...→ this already exists and adding this → task would close a cycle.
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
	public boolean removePredecessor(@NonNull TaskJPA predecessor) {
		if (nonNull(predecessor.successors))
			if (predecessor.successors.removeAll(List.of(this))) // hibernate removal of single element fails
				if (nonNull(predecessors))
					return predecessors.removeAll(List.of(predecessor)); // hibernate removal of single element fails
				else
					throw new IllegalStateException("no predecessors exist, predecessor id: " + predecessor.id());
			else
				throw new IllegalArgumentException("could not remove from successors, predecessor id: " + predecessor.id());
		else
			throw new IllegalStateException("no successors exists, predecessor id: " + predecessor.id());
	}

	@Override
	public boolean removeSuccessor(@NonNull TaskJPA successor) {
		if (nonNull(successor.predecessors))
			if (successor.predecessors.removeAll(List.of(this))) // hibernate removal of single element fails
				if (nonNull(successors))
					return successors.removeAll(List.of(successor)); // hibernate removal of single element fails
				else
					throw new IllegalStateException("no successors exist, ejpaEntity id: " + successor.id());
			else
				throw new IllegalArgumentException("could not remove from predecessors, ejpaEntity id: " + successor.id());
		else
			throw new IllegalStateException("no predecessors exists, ejpaEntity id: " + successor.id());
	}

	// NOTE: JavaBean-style accessors removed — use fluent API (e.g. `description(...)`, `description()`) instead.

	@NonNull
	private Set<TaskJPA> nonNullSubTasks() {
		if (isNull(subTasks))
			subTasks = new HashSet<>();
		return subTasks;
	}

	@NonNull
	private Set<TaskJPA> nonNullPredecessors() {
		if (isNull(predecessors))
			predecessors = new HashSet<>();
		return predecessors;
	}

	@NonNull
	private Set<TaskJPA> nonNullSuccessors() {
		if (isNull(successors))
			successors = new HashSet<>();
		return successors;
	}

	/** {@code null} safe check for containment */
	private boolean predecessorsContains(TaskJPA entity) {
		if (isNull(predecessors))
			return false;
		return predecessors.contains(entity);
	}

	/** {@code null} safe check for containment */
	private boolean successorsContain(TaskJPA entity) {
		if (isNull(successors))
			return false;
		return successors.contains(entity);
	}

	/** {@code null} safe check for containment */
	private boolean subTasksContain(TaskJPA entity) {
		if (isNull(subTasks))
			return false;
		return subTasks.contains(entity);
	}

	/**
	 * Iterative DFS: returns {@code true} if {@code target} is reachable from {@code start}
	 * by following loaded (non-null) successor references.
	 * <p>
	 * In-memory only — a {@code null} {@code successors} field means the collection has not
	 * been loaded (Hibernate lazy); those branches are not traversed.  For a full persistent
	 * cycle check use {@code PredecessorSuccessorCycleValidator}.
	 */
	private boolean isSuccessorReachable(@NonNull TaskJPA start, @NonNull TaskJPA target) {
		Set<TaskJPA>   visited = new HashSet<>();
		Deque<TaskJPA> stack   = new ArrayDeque<>();
		if (start.successors != null) stack.addAll(start.successors);
		while (!stack.isEmpty()) {
			TaskJPA current = stack.pop();
			if (current.equals(target)) return true;
			if (!visited.add(current))  continue;
			if (current.successors != null) stack.addAll(current.successors);
		}
		return false;
	}

	///////////////////////
	// additional accessors
	///////////////////////

	//////////////////////
	// mapstruct callbacks
	//////////////////////
}