package de.ruu.app.jeeeraaah.common.api.domain.lazy;

import de.ruu.app.jeeeraaah.common.api.domain.flat.TaskGroupFlat;
import lombok.NonNull;

import java.util.Set;

/** Lazy representation of a {@link TaskGroupFlat} entity that additionally provides the ids of related tasks. */
public interface TaskGroupLazy extends TaskGroupFlat
{
	@NonNull Set<Long> taskIds();
}