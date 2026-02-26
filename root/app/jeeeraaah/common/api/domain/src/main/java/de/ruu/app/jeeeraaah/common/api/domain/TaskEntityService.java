package de.ruu.app.jeeeraaah.common.api.domain;

/**
 * Entity-specific service interface for tasks.
 * Extends the generic TaskService with entity-specific type constraints.
 *
 * @param <TG> TaskGroupEntity implementation type that the tasks belong to
 * @param <T>  TaskEntity implementation type
 */
public interface TaskEntityService<TG extends TaskGroupEntity<T>, T extends TaskEntity<TG, T>>
		extends TaskService<TG, T>
{
	// Currently no additional entity-specific methods.
	// This interface serves as a type-safe marker for entity-based services.
	// Future entity-specific operations (e.g., merge, refresh, eager loading) can be added here.
}