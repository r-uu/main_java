package de.ruu.app.jeeeraaah.common.api.ws.rs;

import static de.ruu.lib.util.BooleanFunctions.not;
import static java.util.Objects.isNull;
import static java.util.UUID.randomUUID;

import java.io.Serial;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import de.ruu.app.jeeeraaah.common.api.domain.Task;
import de.ruu.app.jeeeraaah.common.api.domain.TaskEntity;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupEntity;
import de.ruu.lib.jpa.core.AbstractEntity;
import de.ruu.lib.util.BooleanFunctions;
import de.ruu.lib.util.Strings;
import jakarta.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

// @formatter:off
/** data transfer object for fully serialised object graphs (including circular references) */
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "jsonId", scope = TaskGroupDTO.class)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@ToString
@Getter                   // generate getter methods for all fields using lombok unless configured otherwise
@Setter                   // generate setter methods for all fields using lombok unless configured otherwise
@Accessors(fluent = true) // generate fluent accessors with lombok and java-bean-style-accessors in non-abstract classes
                          // with ide, fluent accessors will (usually / by default) be ignored by mapstruct
public class TaskGroupDTO implements TaskGroupEntity<TaskDTO>
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
	 * not {@code final} or {@code @NonNull} because otherwise there has to be a constructor with {@code id}-parameter
	 */
	@EqualsAndHashCode.Include // documents intent of including id for equals() and hashCode() but both methods are
	                           // manually created
	@Setter(lombok.AccessLevel.NONE)
	// @formatter:on
	private @Nullable Long id;

	// @formatter:off
	/** may be <pre>null</pre> if {@link AbstractEntity} was not (yet) persisted. */
	@Setter(lombok.AccessLevel.NONE)
	// @formatter:on
	private @Nullable Short version;

	/** mutable non-null */
	// no lombok-generation of setter because of additional validation in manually created method
	// @formatter:off
	@Setter(lombok.AccessLevel.NONE)
	// @formatter:on
	private @NonNull String name;

	/** mutable nullable */
	private @Nullable String description;

	/**
	 * prevent direct access to this modifiable set from outside this class, use {@link #addTask(TaskDTO)} and
	 * {@link #removeTask(TaskDTO)} to modify the set
	 * <p>
	 * may explicitly be {@code null}, {@code null} indicates that there was no attempt to load related objects from db
	 * (lazy)
	 */
	// @formatter:off
	@JsonProperty("tasks") // Force Jackson to serialize/deserialize this field directly
	@Nullable
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@Getter(lombok.AccessLevel.NONE) // provide handmade getter that returns unmodifiable
	@Setter(lombok.AccessLevel.NONE) // no setter at all
	// @formatter:on
	private Set<TaskDTO> tasks; ///////////////

	// constructors
	///////////////

	protected TaskGroupDTO() {
	} // for jackson

	/**
	 * hand-made required args constructor to guarantee usage of hand made accessors.
	 * 
	 * @param name non-empty name
	 */
	public TaskGroupDTO(@NonNull String name) {
		name(name);
	}

	/**
	 * create a new task group dto from an existing task group entity
	 * <p>
	 * This constructor is used by mapstruct to create a new task group entity from an existing one.
	 * 
	 * @param in the existing task group entity, must not be {@code null}
	 */
	public TaskGroupDTO(@NonNull TaskGroupEntity<? extends TaskEntity<?, ?>> in) {
		this(in.name());
		id = in.id();
		version = in.version();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof TaskGroupDTO other))
			return false;

		if (this.id != null && other.id != null)
			return id.equals(other.id);
		// otherwise compare by jsonId (survives JSON serialization)
		return jsonId.equals(other.jsonId);
	}

	@Override
	public int hashCode()
	{
		return id != null ? id.hashCode() : jsonId.hashCode();
	}

	public boolean equalsWithFieldsIgnoreIds(TaskGroupDTO other)
	{
		if (equalsIdentity(other))
			return true;

		// compare fields one by one but skip id and jsonId
		if (!Objects.equals(name, other.name))
			return false;
		if (!Objects.equals(description, other.description))
			return false;

		return true;
	}

	public boolean equalsWithFieldsIgnoreJsonId(TaskGroupDTO other)
	{
		// equalsIdentity(other) is called inside equalsWithFieldsIgnoreIds(other)
		if (!equalsWithFieldsIgnoreIds(other))
			return false;

		if (!Objects.equals(id, other.id))
			return false;

		return true;
	}

	public boolean equalsWithFieldsIgnoreId(TaskGroupDTO other)
	{
		// equalsIdentity(other) is called inside equalsWithFieldsIgnoreIds(other)
		if (!equalsWithFieldsIgnoreIds(other))
			return false;

		if (!Objects.equals(jsonId, other.jsonId))
			return false;

		return true;
	}

	public boolean equalsWithFields(TaskGroupDTO other)
	{
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

	public boolean equalsIdentity(TaskGroupDTO other)
	{
		if (this == other)
			return true;
		return false;
	}

	////////////////////////////////////////////////////////////////////////
	// fluent style accessors generated by lombok if not specified otherwise
	////////////////////////////////////////////////////////////////////////

	/**
	 * manually created fluent setter with extra parameter check (see throws documentation)
	 * 
	 * @param name non-null, non-empty, non-blank
	 * @return {@code this}
	 * @throws IllegalArgumentException if {@code name} parameter is empty or blank
	 * @throws NullPointerException if {@code name} parameter is {@code null}
	 */
	@Override @NonNull
	public TaskGroupDTO name(@NonNull String name)
	{
		if (Strings.isEmptyOrBlank(name))
			throw new IllegalArgumentException("name must not be empty nor blank");
		this.name = name;
		return this;
	}

	@Override
	public Optional<String> description()
	{
		return Optional.ofNullable(description);
	}

	/** @return optional unmodifiable */
	@Override
	public Optional<Set<TaskDTO>> tasks()
	{
		if (isNull(tasks))
			return Optional.empty();
		return Optional.of(Collections.unmodifiableSet(tasks));
	}

	//////////////////////////////////////////////////////////////////////////////////////////////
	// java bean style accessors for those who do not work with fluent style accessors (mapstruct)
	//////////////////////////////////////////////////////////////////////////////////////////////

	// Jackson needs getTasks() for serialization, but @JsonGetter prevents MapStruct from using it
	@JsonGetter("tasks") @Nullable
	public Set<TaskDTO> getTasks()
	{
		return tasks;
	}

	@JsonSetter("tasks")
	public void setTasks(@Nullable Set<TaskDTO> tasks)
	{
		this.tasks = tasks;
	}

	@NonNull
	public String getName()
	{
		return name();
	}

	public void setName(@NonNull String name)
	{
		name(name);
	}

	@Nullable
	public String getDescription()
	{
		return description().orElse(null);
	}

	public void setDescription(@Nullable String description)
	{
		description(description);
	}

	///////////////////////
	// additional accessors
	///////////////////////

	public Optional<Set<TaskDTO>> mainTaskDTOs()
	{
		Set<TaskDTO> result = new HashSet<>();

		Optional<Set<TaskDTO>> optionalTasks = tasks();
		if (optionalTasks.isPresent())
		{
			Set<TaskDTO> tasks = optionalTasks.get();
			for (TaskDTO task : tasks)
			{
				if (BooleanFunctions.not(task.superTask().isPresent()))
					result.add(task);
			}
		}

		if (BooleanFunctions.not(result.isEmpty()))
			return Optional.of(result);
		return Optional.empty();
	}

	////////////////////////
	// relationship handling
	////////////////////////

	/**
	 * call this method from {@link TaskDTO} constructor and {@link TaskDTO#taskGroup(TaskGroupDTO)} only
	 * 
	 * @param task the {@link Task} to be added as task
	 * @return {@code true} if the task was added, {@code false} if the task was already present in this task group
	 */
	protected boolean addTask(@NonNull TaskDTO task)
	{
		return nonNullTasks().add(task);
	}

	@Override
	public boolean removeTask(@NonNull TaskDTO task)
	{
		if (not(isNull(tasks)))
			return tasks.remove(task);
		return false;
	}

	//////////////////////
	// mapstruct callbacks
	//////////////////////

	private @NonNull Set<TaskDTO> nonNullTasks()
	{
		if (isNull(tasks))
			tasks = new HashSet<>();
		return tasks;
	}
}