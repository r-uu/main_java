package de.ruu.app.jeeeraaah.backend.persistence.jpa;

import de.ruu.app.jeeeraaah.common.api.domain.RemoveNeighboursFromTaskConfig;
import de.ruu.app.jeeeraaah.common.api.domain.TaskRelationException;
import de.ruu.app.jeeeraaah.common.api.domain.exception.EntityNotFoundException;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskCreationData;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import lombok.NonNull;

import java.util.Optional;
import java.util.Set;

/**
 * DTO-based service interface for task operations.
 * This service works exclusively with DTOs, hiding JPA implementation details
 * from the REST layer and ensuring proper JPMS encapsulation.
 * <p>
 * All mapping between JPA entities and DTOs happens internally within the
 * persistence module's service implementation.
 */
public interface TaskDTOService
{
	/**
	 * Creates a new task from creation data.
	 * Handles mapping and transaction management internally.
	 *
	 * @param data task creation data from REST API
	 * @return the created task as DTO
	 * @throws EntityNotFoundException if the task group does not exist
	 */
	@NonNull TaskDTO create(@NonNull TaskCreationData data);

	/**
	 * Reads a task by ID.
	 *
	 * @param id the task ID
	 * @return the task DTO if found
	 */
	@NonNull Optional<TaskDTO> read(@NonNull Long id);

	/**
	 * Updates an existing task.
	 *
	 * @param dto the task DTO with updated data
	 * @return the updated task DTO
	 * @throws EntityNotFoundException if the task does not exist
	 */
	@NonNull TaskDTO update(@NonNull TaskDTO dto);

	/**
	 * Deletes a task by ID.
	 *
	 * @param id the task ID
	 * @throws EntityNotFoundException if the task does not exist
	 */
	void delete(@NonNull Long id);

	/**
	 * Finds all tasks.
	 *
	 * @return set of all task DTOs
	 */
	@NonNull Set<TaskDTO> findAll();

	/**
	 * Finds a task with all its related tasks (subtasks, predecessors, successors).
	 *
	 * @param id the task ID
	 * @return the task DTO with related tasks if found
	 */
	@NonNull Optional<TaskDTO> findWithRelated(@NonNull Long id);

	// ── Super / sub hierarchy (single entry-point) ──────────────────────────
	/** Sets the super task of {@code idChild} to {@code idSuperTask}. Performs cycle detection. */
	void setSuperTask(@NonNull Long idChild, @NonNull Long idSuperTask) throws TaskRelationException;
	/** Detaches {@code idChild} from its current super task, making it a root task. */
	void removeSuperTask(@NonNull Long idChild) throws TaskRelationException;

	// ── Predecessor / successor ──────────────────────────────────────────────
	void addPredecessor(@NonNull Long idTask, @NonNull Long idPredecessor) throws TaskRelationException;
	void addSuccessor(@NonNull Long idTask, @NonNull Long idSuccessor) throws TaskRelationException;
	void removePredecessor(@NonNull Long idTask, @NonNull Long idPredecessor) throws TaskRelationException;
	void removeSuccessor(@NonNull Long idTask, @NonNull Long idSuccessor) throws TaskRelationException;
	void removeNeighboursFromTask(@NonNull RemoveNeighboursFromTaskConfig config);
}
