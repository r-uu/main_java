package de.ruu.app.jeeeraaah.common.api.domain;

import de.ruu.app.jeeeraaah.common.api.domain.exception.*;
import lombok.NonNull;

import java.util.Optional;
import java.util.Set;

/**
 * Generic, technology (JPA, JSONB, JAXB, MapStruct, ...) agnostic service interface for tasks.
 * Uses specific domain exceptions for better error handling and API clarity.
 *
 * @param <TG> TaskGroup implementation type that the tasks belong to
 * @param <T>  Task implementation type
 */
public interface TaskService<TG extends TaskGroup<T>, T extends Task<TG, T>> {

	/**
	 * Creates a new task in the backend.
	 *
	 * @param task the task to create
	 * @return the created task
	 * @throws EntityValidationException if the task data is invalid
	 * @throws EntityAlreadyExistsException if a task with the same ID already exists
	 * @throws PersistenceException if the persistence operation fails
	 */
	@NonNull T create(@NonNull T task);

	/**
	 * Reads the task with the given id from the backend.
	 *
	 * @param id the id of the task to read
	 * @return the task with the given id, or empty if not found
	 * @throws PersistenceException if the persistence operation fails
	 */
	Optional<? extends T> read(@NonNull Long id);

	/**
	 * Updates the task if it is persistent (has an id) and can be found in the backend.
	 * If it is persistent but cannot be found, it throws an EntityNotFoundException.
	 * If it is not persistent, it creates a new task in the backend.
	 *
	 * @param task the task to update or create
	 * @return the updated or newly created task
	 * @throws EntityNotFoundException if the task with the given id does not exist (for updates)
	 * @throws EntityValidationException if the task data is invalid
	 * @throws PersistenceException if the persistence operation fails
	 */
	@NonNull T update(@NonNull T task);

	/**
	 * Deletes the task with the given id from the backend.
	 *
	 * @param id the id of the task to delete
	 * @throws EntityNotFoundException if the task with the given id does not exist
	 * @throws PersistenceException if the persistence operation fails
	 */
	void delete(@NonNull Long id);

	/**
	 * Finds the task with the given id from the backend.
	 *
	 * @param id the id of the task to find
	 * @return the task with the given id, or empty if not found
	 * @throws PersistenceException if the persistence operation fails
	 */
	default Optional<? extends T> find(@NonNull Long id) {
		return read(id);
	}

	/**
	 * Finds all tasks in the backend.
	 *
	 * @return a set of all tasks
	 * @throws PersistenceException if the persistence operation fails
	 */
	Set<? extends T> findAll();

	/**
	 * Finds the task with the given id and returns it with all its related tasks.
	 *
	 * @param id the id of the task to find
	 * @return the task with the given id and all its related tasks, or empty if not found
	 * @throws PersistenceException if the persistence operation fails
	 */
	Optional<? extends T> findWithRelated(@NonNull Long id);

	/**
	 * Sets the super task (parent) of a task.
	 * <p>
	 * This is the single entry-point for managing the super/sub-task hierarchy.
	 * Setting {@code newSuperTask} to {@code null} detaches the task from its current parent (makes it a root task).
	 * <p>
	 * Cycle detection is performed server-side: if {@code newSuperTask} is already a descendant
	 * of {@code child}, a {@link de.ruu.app.jeeeraaah.common.api.domain.TaskRelationException} is thrown.
	 *
	 * @param child        the task whose parent changes
	 * @param newSuperTask the new parent task, or {@code null} to detach
	 * @throws EntityNotFoundException           if the task or new super-task does not exist
	 * @throws de.ruu.app.jeeeraaah.common.api.domain.TaskRelationException if adding would create a cycle
	 * @throws PersistenceException              if the persistence operation fails
	 */
	void setSuperTask(@NonNull T child, T newSuperTask);

	/**
	 * Detaches {@code child} from its current super task (makes it a root task).
	 * Equivalent to {@code setSuperTask(child, null)}.
	 *
	 * @param child the task to detach from its parent
	 */
	void removeSuperTask(@NonNull T child);

	/**
	 * Adds a predecessor to a task.
	 *
	 * @param task the task
	 * @param predecessor the predecessor task to add
	 * @throws EntityNotFoundException if the task or predecessor does not exist
	 * @throws CircularReferenceException if adding the predecessor would create a circular reference
	 * @throws InvalidRelationshipException if the relationship is invalid
	 * @throws PersistenceException if the persistence operation fails
	 */
	void addPredecessor(@NonNull T task, @NonNull T predecessor);

	/**
	 * Adds a successor to a task.
	 *
	 * @param task the task
	 * @param successor the successor task to add
	 * @throws EntityNotFoundException if the task or successor does not exist
	 * @throws CircularReferenceException if adding the successor would create a circular reference
	 * @throws InvalidRelationshipException if the relationship is invalid
	 * @throws PersistenceException if the persistence operation fails
	 */
	void addSuccessor(@NonNull T task, @NonNull T successor);


	/**
	 * Removes a predecessor from a task.
	 *
	 * @param task the task
	 * @param predecessor the predecessor task to remove
	 * @throws EntityNotFoundException if the task or predecessor does not exist
	 * @throws InvalidRelationshipException if the relationship does not exist
	 * @throws PersistenceException if the persistence operation fails
	 */
	void removePredecessor(@NonNull T task, @NonNull T predecessor);

	/**
	 * Removes a successor from a task.
	 *
	 * @param task the task
	 * @param successor the successor task to remove
	 * @throws EntityNotFoundException if the task or successor does not exist
	 * @throws InvalidRelationshipException if the relationship does not exist
	 * @throws PersistenceException if the persistence operation fails
	 */
	void removeSuccessor(@NonNull T task, @NonNull T successor);

	/**
	 * Removes neighbours from a task based on the provided configuration.
	 *
	 * @param removeNeighboursFromTaskConfig the configuration specifying which neighbours to remove
	 * @throws EntityNotFoundException if the task does not exist
	 * @throws InvalidRelationshipException if any of the relationships do not exist
	 * @throws PersistenceException if the persistence operation fails
	 */
	void removeNeighboursFromTask(@NonNull RemoveNeighboursFromTaskConfig removeNeighboursFromTaskConfig);
}

