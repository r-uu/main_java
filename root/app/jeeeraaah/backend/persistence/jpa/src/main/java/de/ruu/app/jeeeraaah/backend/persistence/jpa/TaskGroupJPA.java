package de.ruu.app.jeeeraaah.backend.persistence.jpa;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.Serial;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupEntity;
import de.ruu.app.jeeeraaah.common.api.domain.lazy.TaskGroupLazy;
import de.ruu.lib.jpa.core.AbstractEntity;
import de.ruu.lib.util.Strings;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@ToString
@Getter                   // generate getter methods for all fields using lombok unless configured otherwise ({@code
@Setter                   // generate setter methods for all fields using lombok unless configured otherwise ({@code
@Accessors(fluent = true) // generate fluent accessors with lombok and java-bean-style-accessors in non-abstract classes
                          // with ide, fluent accessors will (usually / by default) be ignored by mapstruct
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true) // generate no args constructor for jsonb, jaxb, jpa, mapstruct, ...
@Entity
@Table(name = "task_group")
public class TaskGroupJPA implements TaskGroupEntity<TaskJPA>
{
	@Serial private static final long serialVersionUID = 1L;

	/**
	 * may be <pre>null</pre> if instance was not (yet) persisted.
	 * <p>not {@code final} or {@code @NonNull} because otherwise there has to be a constructor with {@code id}-parameter
	 */
	@EqualsAndHashCode.Include // documents intent of including id for equals() and hashCode() but both methods are
	                           // manually created
	@Nullable @Setter(AccessLevel.NONE) @Id @GeneratedValue
	private Long   id;

	/** may be <pre>null</pre> if {@link AbstractEntity} was not (yet) persisted. */
	@Nullable @Setter(AccessLevel.NONE) @Column(nullable = false) @Version
	private Short  version;

	/** mutable non-null */
	// no lombok-generation of setter because of additional validation in manually created method
	@NonNull  @Setter(AccessLevel.NONE) @Column(nullable = false, unique=true)
	private String name;

	/** mutable nullable */
	@Nullable
	private String description;

	/**
	 * prevent direct access to this modifiable set from outside this class
	 * <p>
	 * may explicitly be {@code null}, {@code null} indicates that there was no attempt to load related objects from db
	 * (lazy)
	 */
	@Nullable
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@Getter(AccessLevel.NONE) // provide handmade getter that returns unmodifiable
	@Setter(AccessLevel.NONE) // no setter at all
	@OneToMany
	(
			mappedBy = "taskGroup",
			// do not use cascade REMOVE in to-many relations as this may result in cascading deletes that wipe out both sides
			// of the relation entirely
			cascade  = { CascadeType.PERSIST, CascadeType.MERGE }
	)
	private Set<TaskJPA> tasks;

	///////////////
	// constructors
	///////////////

	/**
	 * hand-made required args constructor to guarantee usage of hand made accessors.
	 * @param name non-empty name
	 */
	public TaskGroupJPA(@NonNull String name) { name(name); }

	/**
	 * create a new task group entity from an existing entity
	 * <p>
	 * This constructor is used by mapstruct to create a new task group entity from an existing one.
	 * @param in the existing entity, must not be {@code null}
	 */
	public TaskGroupJPA(@NonNull TaskGroupEntity<?> in)
	{
		this(in.name());
		id      = in.id();
		version = in.version();
	}

	public TaskGroupJPA(@NonNull TaskGroupLazy in)
	{
		this(in.name());
		id      = in.id();
		version = in.version();
	}

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof TaskGroupJPA other)) return false;

		if (this.id != null && other.id != null) return id.equals(other.id);
		return false;
	}

	@Override public int hashCode() { return id != null ? id.hashCode() : System.identityHashCode(this); }

	public boolean equalsWithFieldsIgnoreIds(TaskGroupJPA other)
	{
		if (equalsIdentity(other)) return true;

		// compare fields one by one but skip id and jsonId
		if (!Objects.equals(name       , other.name       )) return false;
		if (!Objects.equals(description, other.description)) return false;

		return true;
	}

	public boolean equalsWithFieldsIgnoreId(TaskGroupJPA other)
	{
		// equalsIdentity(other) is called inside equalsWithFieldsIgnoreIds(other)
		if (!equalsWithFieldsIgnoreIds(other)) return false;

		return true;
	}

	public boolean equalsWithFields(TaskGroupJPA other)
	{
		// equalsIdentity(other) is called inside equalsWithFieldsIgnoreIds(other)
		if (!equalsWithFieldsIgnoreIds(other)) return false;

		// compare id fields
		if (!Objects.equals(id, other.id)) return false;

		return true;
	}

	public boolean equalsIdentity(TaskGroupJPA other)
	{
		if (this == other) return true;
		return false;
	}

	////////////////////////////////////////////////////////////////////////
	// fluent style accessors generated by lombok if not specified otherwise
	////////////////////////////////////////////////////////////////////////

	/**
	 * manually created fluent setter with extra parameter check (see throws documentation)
	 * @param name non-null, non-empty, non-blank
	 * @return {@code this}
	 * @throws IllegalArgumentException if {@code name} parameter is empty or blank
	 * @throws NullPointerException     if {@code name} parameter is {@code null}
	 */
	@Override @NonNull public TaskGroupJPA name(@NonNull String name)
	{
		if (Strings.isEmptyOrBlank(name)) throw new IllegalArgumentException("name must not be empty nor blank");
		this.name = name;
		return this;
	}

	@Override public Optional<String> description() { return Optional.ofNullable(description); }

	/** @return {@link #tasks} wrapped in unmodifiable */
	@Override public Optional<Set<TaskJPA>> tasks()
	{
		if (isNull(tasks)) return Optional.empty();
		return Optional.of(Collections.unmodifiableSet(tasks));
	}

	//////////////////////////////////////////////////////////////////////////////////////////////
	// java bean style accessors for those who do not work with fluent style accessors (mapstruct)
	//////////////////////////////////////////////////////////////////////////////////////////////

	@Nullable @Override public Long  getId     () { return id; }
	@Nullable @Override public Short getVersion() { return version; }

	// do _NOT_ define getter for tasks to avoid handling of tasks by mapstruct automatism
	@Nullable public Set<TaskJPA> getTasks() { return tasks; }

	// java bean style accessors removed: rely on fluent accessors and lombok-mapstruct-binding

	///////////////////////
	// additional accessors
	///////////////////////

	// --- none ---

	////////////////////////
	// relationship handling
	////////////////////////

	/**
	 * call this method from {@link TaskJPA} constructor and {@link TaskJPA#taskGroup(TaskGroupJPA)} only
	 * @param task the {@link TaskJPA} to be added as task
	 * @return {@code true} if the task was added, {@code false} if the task was already present in this task group
	 */
	protected boolean addTask(@NonNull TaskJPA task) { return nonNullTasks().add(task); }

	@Override public boolean removeTask(@NonNull TaskJPA entity)
	{
		if (nonNull(tasks)) return tasks.removeAll(List.of(entity)); // hibernate removal of single element fails
		return false;
	}

	//////////////////////
	// mapstruct callbacks
	//////////////////////

	private Set<TaskJPA> nonNullTasks()
	{
		if (isNull(tasks)) tasks = new HashSet<>();
		return tasks;
	}
}