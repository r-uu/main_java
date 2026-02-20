package de.ruu.app.jeeeraaah.common.api.ws.rs;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupEntity;
import de.ruu.app.jeeeraaah.common.api.domain.flat.TaskGroupFlat;
import de.ruu.lib.util.Strings;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Optional;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

/** Transfer object for task groups without any information about their related tasks. */
@JsonAutoDetect(fieldVisibility = ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)  // Optional: exclude null fields
@JsonIgnoreProperties(ignoreUnknown = true) // Optional: ignore unknown properties
@ToString
@Getter
@Accessors(fluent = true)
public class TaskGroupDTOFlat implements TaskGroupFlat
{
	private final UUID jsonId = randomUUID();

	private @NonNull  Long   id;
	private @NonNull  Short  version;
	private @NonNull  String name;
	private @Nullable String description;

	protected TaskGroupDTOFlat() { } // for jackson

	public TaskGroupDTOFlat(@NonNull TaskGroupEntity<?> entity)
	{
		this();
		id          = requireNonNull(entity.id());
		version     = requireNonNull(entity.version());
		description = entity.description().orElse("");
		name(entity.name());
	}

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof TaskGroupDTOFlat other)) return false;

		if (this.id != null && other.id != null) return id.equals(other.id);
		// otherwise compare by jsonId (survives JSON serialization)
		return jsonId.equals(other.jsonId);
	}

	@Override public int hashCode() { return id != null ? id.hashCode() : jsonId.hashCode(); }

	@Override public @NonNull TaskGroupFlat name(@NonNull String name)
	{
		if (Strings.isEmptyOrBlank(name)) throw new IllegalArgumentException("name must not be empty nor blank");
		this.name = name;
		return this;
	}

	@Override public Optional<String> description() { return Optional.ofNullable(description); }

	@Override public @NonNull TaskGroupFlat description(String description)
	{
		this.description = description;
		return this;
	}
}