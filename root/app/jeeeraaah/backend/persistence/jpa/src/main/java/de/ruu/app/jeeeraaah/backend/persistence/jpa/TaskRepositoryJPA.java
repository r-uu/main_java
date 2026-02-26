package de.ruu.app.jeeeraaah.backend.persistence.jpa;

import de.ruu.app.jeeeraaah.common.api.domain.RemoveNeighboursFromTaskConfig;
import de.ruu.app.jeeeraaah.common.api.domain.TaskRelationException;
import de.ruu.lib.jpa.core.AbstractRepository;
import de.ruu.lib.jpa.core.GraphType;
import jakarta.persistence.EntityGraph;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.ruu.lib.util.BooleanFunctions.not;

/**
 * Repository for TaskJPA entities.
 * <p>
 * Note: This repository extends AutoCloseable but is CDI-managed and should NOT be closed manually.
 * The @SuppressWarnings("resource") annotation suppresses false-positive warnings.
 */
@Slf4j
@SuppressWarnings("resource") // Repository is CDI-managed, not manually closed
public abstract class TaskRepositoryJPA extends AbstractRepository<TaskJPA, Long>
{
	public Optional<TaskJPA> findWithRelated(@NonNull Long id)
	{
		EntityGraph<TaskJPA> entityGraph = entityManager().createEntityGraph(TaskJPA.class);

		// Load all related entities to prevent LazyInitializationException
		entityGraph.addSubgraph(TaskJPA_.TASK_GROUP);  // Fix: Load taskGroup to avoid LazyInitializationException
		entityGraph.addSubgraph(TaskJPA_.SUB_TASKS);
		entityGraph.addSubgraph(TaskJPA_.PREDECESSORS);
		entityGraph.addSubgraph(TaskJPA_.SUCCESSORS);

		Map<String, Object> hints = new HashMap<>();
		hints.put(GraphType.FETCH.getName(), entityGraph);

		TaskJPA result = entityManager().find(TaskJPA.class, id, hints);

		return Optional.ofNullable(result);
	}

	// public @NonNull Set<TaskJPA> findGroupTasksWithRelated(@NonNull Long id)
	// {
	// CriteriaBuilder criteriaBuilder = entityManager().getCriteriaBuilder();
	// CriteriaQuery<TaskJPA> criteriaQuery =
	// criteriaBuilder.createQuery(TaskJPA.class);
	// Root<TaskJPA> root = criteriaQuery .from (TaskJPA.class);
	//
	// criteriaQuery.select(root).where(root.get(TaskJPA_.id.getName()).in(ids));
	//
	// EntityGraph<TaskJPA> entityGraph =
	// entityManager().createEntityGraph(TaskJPA.class);
	// entityGraph.addSubgraph("subTasks");
	// entityGraph.addSubgraph("predecessors");
	// entityGraph.addSubgraph("successors");
	//
	// Set<TaskJPA> taskEntities =
	// new HashSet<>(
	// entityManager()
	// .createQuery(criteriaQuery)
	// .setHint(GraphType.FETCH.getName(), entityGraph)
	// .getResultList());
	//
	// return taskEntities;
	// }
	//
	// public @NonNull Set<TaskJPA> findTasks(@NonNull Set<Long> ids)
	// {
	// CriteriaBuilder criteriaBuilder = entityManager().getCriteriaBuilder();
	// CriteriaQuery<TaskJPA> criteriaQuery =
	// criteriaBuilder.createQuery(TaskJPA.class);
	// Root<TaskJPA> root = criteriaQuery .from (TaskJPA.class);
	//
	// criteriaQuery.select(root).where(root.get("id").in(ids));
	//
	// Set<TaskJPA> taskEntities =
	// new HashSet<>(
	// entityManager()
	// .createQuery(criteriaQuery)
	// .getResultList());
	//
	// return taskEntities;
	// }

	public void addSubTask(@NonNull Long taskId, @NonNull Long subTaskId) throws TaskRelationException {
		TaskJPA persistedTask = findOrThrow(taskId);
		TaskJPA persistedSubTask = findOrThrow(subTaskId);

		if (not(persistedTask.addSubTask(persistedSubTask)))
			throw new RuntimeException("failure adding sub task with id " + subTaskId + " to task with id " + taskId);
	}

	public void addPredecessor(@NonNull Long taskId, @NonNull Long predecessorId) throws TaskRelationException {
		TaskJPA persistedTask = findOrThrow(taskId);
		TaskJPA persistedPreTask = findOrThrow(predecessorId);

		if (not(persistedTask.addPredecessor(persistedPreTask)))
			throw new TaskRelationException(
					"failure adding predecessor with id " + predecessorId + " to task with id " + taskId);
	}

	public void addSuccessor(@NonNull Long taskId, @NonNull Long successorId) throws TaskRelationException {
		TaskJPA persistedTask = findOrThrow(taskId);
		TaskJPA persistedSucTask = findOrThrow(successorId);

		if (not(persistedTask.addSuccessor(persistedSucTask)))
			throw new TaskRelationException(
					"failure adding successor with id " + successorId + " to task with id: " + taskId);
	}

	public void removeSubTask(@NonNull Long taskId, @NonNull Long subTaskId) throws TaskRelationException {
		Optional<TaskJPA> optional = findWithRelated(taskId);
		if (not(optional.isPresent()))
			throw new TaskRelationException("super task not found, id: " + taskId);
		TaskJPA persistedTask = optional.get();

		optional = findWithRelated(subTaskId);
		if (not(optional.isPresent()))
			throw new TaskRelationException("sub task not found, id: " + subTaskId);
		TaskJPA persistedSubTask = optional.get();

		if (not(persistedTask.removeSubTask(persistedSubTask)))
			throw new TaskRelationException(
					"failure removing sub task with id [" + subTaskId + "] from task with id [" + taskId + "]");
	}

	public void removePredecessor(@NonNull Long taskId, @NonNull Long predecessorId) throws TaskRelationException {
		Optional<TaskJPA> optional = findWithRelated(taskId);
		if (not(optional.isPresent()))
			throw new TaskRelationException("successor not found, id: " + taskId);
		TaskJPA persistedTask = optional.get();

		optional = findWithRelated(predecessorId);
		if (not(optional.isPresent()))
			throw new TaskRelationException("predecessor not found, id:" + predecessorId);
		TaskJPA persistedPredecessor = optional.get();

		if (not(persistedTask.removePredecessor(persistedPredecessor)))
			throw new TaskRelationException(
					"failure removing predecessor with id [" + predecessorId + "] from task with id [" + taskId + "]");

		// managed entities; no merge() required
	}

	public void removeSuccessor(@NonNull Long taskId, @NonNull Long successorId) throws TaskRelationException {
		Optional<TaskJPA> optional = findWithRelated(taskId);
		if (not(optional.isPresent()))
			throw new TaskRelationException("predecessor not found: id: " + taskId);
		TaskJPA persistedTask = optional.get();

		optional = findWithRelated(successorId);
		if (not(optional.isPresent()))
			throw new TaskRelationException("successor not found, id: " + successorId);
		TaskJPA persistedSuccessor = optional.get();

		if (not(persistedTask.removeSuccessor(persistedSuccessor)))
			throw new TaskRelationException(
					"failure removing successor with id [" + successorId + "] from task with id [" + taskId + "]");

		// managed entities; no merge() required
	}

	public void removeNeighboursFromTask(@NonNull RemoveNeighboursFromTaskConfig config) throws TaskRelationException
	{
		Optional<TaskJPA> optionalTaskWithRelations = findWithRelated(config.idTask());

		if (not(optionalTaskWithRelations.isPresent()))
				throw new TaskRelationException("task not found, id: " + config.idTask());

		TaskJPA persistedTaskWithRelations = optionalTaskWithRelations.get();
		if (config.removeFromSuperTask())
		{
			Optional<TaskJPA> optionalPersistedSuperTask = persistedTaskWithRelations.superTask();
			if (not(optionalPersistedSuperTask.isPresent()))
			{
				log.debug
				(
				   "super task relation was configured to be removed but no super task exists for task with id: {}," +
						   " continue with removing further neighbour tasks",
				   config.idTask()
				);
			}
			else
			{
				TaskJPA persistedSuperTask = optionalPersistedSuperTask.get();
				if (not(persistedSuperTask.removeSubTask(persistedTaskWithRelations)))
						throw new TaskRelationException(
								"failure removing sub task with id [" + config.idTask()
										+ "] from super task with id [" + persistedSuperTask.getId() + "]");
				entityManager().merge(persistedSuperTask);
			}
		}

		for (Long idSubTask : config.removeFromSubTasks())
		{
			Optional<TaskJPA> optionalPersistedSubTask = findWithRelated(idSubTask);
			if (not(optionalPersistedSubTask.isPresent()))
			{
				log.debug
				(
				   "sub task relation was configured to be removed but no sub task exists for task with id: {}," +
						   " continue with removing further neighbour tasks",
				   config.idTask()
				);
			}
			else
			{
				TaskJPA persistedSubTask = optionalPersistedSubTask.get();
				if (not(persistedTaskWithRelations.removeSubTask(persistedSubTask)))
						throw new TaskRelationException(
								"failure removing sub task with id [" + idSubTask
										+ "] from task with id [" + config.idTask() + "]");
				entityManager().merge(persistedSubTask);
			}
		}

		for (Long idPredecessor : config.removeFromPredecessors())
		{
			Optional<TaskJPA> optionalPersistedPredecessor = findWithRelated(idPredecessor);
			if (not(optionalPersistedPredecessor.isPresent()))
			{
				log.debug
				(
				   "predecessor relation was configured to be removed but no predecessor task exists for task with id: {}," +
						   " continue with removing further neighbour tasks",
				   config.idTask()
				);
			}
			else
			{
				TaskJPA persistedPredecessor = optionalPersistedPredecessor.get();
				if (not(persistedTaskWithRelations.removePredecessor(persistedPredecessor)))
						throw new TaskRelationException(
								"failure removing predecessor with id [" + idPredecessor
										+ "] from task with id [" + config.idTask() + "]");
				entityManager().merge(persistedPredecessor);
			}
		}

		for (Long idSuccessor : config.removeFromSuccessors())
		{
			Optional<TaskJPA> optionalPersistedSuccessor = findWithRelated(idSuccessor);
			if (not(optionalPersistedSuccessor.isPresent()))
			{
				log.debug
				(
				   "successor relation was configured to be removed but no successor task exists for task with id: {}," +
						   " finished removing further neighbour tasks",
				   config.idTask()
				);
			}
			else
			{
				TaskJPA persistedSuccessor = optionalPersistedSuccessor.get();
				if (not(persistedTaskWithRelations.removeSuccessor(persistedSuccessor)))
						throw new TaskRelationException(
								"failure removing successor with id [" + idSuccessor
										+ "] from task with id [" + config.idTask() + "]");
				entityManager().merge(persistedSuccessor);
			}
		}
	}

	private TaskJPA findOrThrow(@NonNull Long id) {
		Optional<TaskJPA> optional = findWithRelated(id);
		if (not(optional.isPresent()))
			throw new TaskRelationException("task not found, id: " + id);
		return optional.get();
	}
}