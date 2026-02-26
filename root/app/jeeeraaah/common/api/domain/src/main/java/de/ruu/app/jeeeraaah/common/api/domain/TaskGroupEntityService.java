package de.ruu.app.jeeeraaah.common.api.domain;

/**
 * Entity-specific service interface for task groups.
 * Extends the generic TaskGroupService with entity-specific type constraints.
 *
 * @param <TG> TaskGroupEntity implementation type
 * @param <T>  TaskEntity implementation type belonging to the TaskGroup
 */
public interface TaskGroupEntityService<TG extends TaskGroupEntity<T>, T extends TaskEntity<TG, T>>
		extends TaskGroupService<TG, T>
{
	// Currently no additional entity-specific methods.
	// This interface serves as a type-safe marker for entity-based services.
	// Future entity-specific operations (e.g., merge, refresh, eager loading) can be added here.
}