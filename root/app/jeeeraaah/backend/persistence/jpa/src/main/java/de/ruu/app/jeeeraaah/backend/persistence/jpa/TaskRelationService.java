package de.ruu.app.jeeeraaah.backend.persistence.jpa;

import de.ruu.app.jeeeraaah.common.api.domain.TaskRelationException;
import lombok.NonNull;

/**
 * Service interface for managing task relationships using IDs.
 * <p>
 * Super/sub hierarchy is managed exclusively via {@link #setSuperTask} and
 * {@link #removeSuperTask}. Predecessor/successor relations use the add/remove pair.
 */
public interface TaskRelationService
{
	// ── Super / sub hierarchy ─────────────────────────────────────────────────

	/**
	 * Sets the super task (parent) of the child task.
	 * Performs ancestor-walk cycle detection before applying the change.
	 *
	 * @param idChild     ID of the task whose parent changes
	 * @param idSuperTask ID of the new parent task
	 * @throws TaskRelationException if the change would create a cycle or violates constraints
	 */
	void setSuperTask(@NonNull Long idChild, @NonNull Long idSuperTask) throws TaskRelationException;

	/**
	 * Detaches the child task from its current super task (makes it a root task).
	 *
	 * @param idChild ID of the task to detach
	 * @throws TaskRelationException if the task does not exist
	 */
	void removeSuperTask(@NonNull Long idChild) throws TaskRelationException;

	// ── Predecessor / successor ───────────────────────────────────────────────

	void addPredecessor(@NonNull Long idTask, @NonNull Long idPredecessor) throws TaskRelationException;
	void addSuccessor  (@NonNull Long idTask, @NonNull Long idSuccessor)   throws TaskRelationException;
	void removePredecessor(@NonNull Long idTask, @NonNull Long idPredecessor) throws TaskRelationException;
	void removeSuccessor  (@NonNull Long idTask, @NonNull Long idSuccessor)   throws TaskRelationException;
}
