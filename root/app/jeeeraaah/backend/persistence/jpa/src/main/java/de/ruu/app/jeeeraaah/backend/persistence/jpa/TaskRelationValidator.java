package de.ruu.app.jeeeraaah.backend.persistence.jpa;

import de.ruu.app.jeeeraaah.common.api.domain.TaskRelationException;
import lombok.NonNull;

/**
 * SPI hook for additional task-relation validation.
 * <p>
 * Implementations are discovered via CDI ({@code Instance<TaskRelationValidator>})
 * and are called by the persistence layer before each structural change.
 * This is the extension point for the
 * {@code de.ruu.app.jeeeraaah.backend.constraints.timecycle} module (predecessor/successor
 * cycle detection) and any future constraint modules.
 * <p>
 * The persistence layer ({@code backend.persistence.jpa}) does <em>not</em> depend on
 * any constraint module — constraint modules depend on this interface unidirectionally.
 */
public interface TaskRelationValidator
{
	/**
	 * Called before {@code task} (identified by {@code taskId}) gets {@code predecessorId}
	 * added as a predecessor.
	 *
	 * @throws TaskRelationException if the operation violates a constraint
	 */
	void validateBeforeAddPredecessor(@NonNull Long taskId, @NonNull Long predecessorId)
			throws TaskRelationException;

	/**
	 * Called before {@code task} (identified by {@code taskId}) gets {@code successorId}
	 * added as a successor.
	 *
	 * @throws TaskRelationException if the operation violates a constraint
	 */
	void validateBeforeAddSuccessor(@NonNull Long taskId, @NonNull Long successorId)
			throws TaskRelationException;
}

