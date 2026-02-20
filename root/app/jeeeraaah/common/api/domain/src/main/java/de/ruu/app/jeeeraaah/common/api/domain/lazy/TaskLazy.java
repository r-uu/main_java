package de.ruu.app.jeeeraaah.common.api.domain.lazy;

import de.ruu.app.jeeeraaah.common.api.domain.TaskData;
import de.ruu.app.jeeeraaah.common.api.domain.flat.TaskFlat;
import de.ruu.lib.jpa.core.Entity;
import jakarta.annotation.Nullable;
import lombok.NonNull;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public interface TaskLazy extends TaskFlat
{
	@NonNull TaskLazy name(@NonNull String name);

	@NonNull TaskLazy description(@Nullable String    description);
	@NonNull TaskLazy start      (@Nullable LocalDate startEstimated);
	@NonNull TaskLazy end        (@Nullable LocalDate finishEstimated);
	@NonNull TaskLazy closed     (@NonNull  Boolean   closed);

	@NonNull  Long taskGroupId();
	// superTaskId() is inherited from TaskFlat as Optional<Long>

	@NonNull Set<Long> subTaskIds     = new HashSet<>();
	@NonNull Set<Long> predecessorIds = new HashSet<>();
	@NonNull Set<Long> successorIds   = new HashSet<>();
}
