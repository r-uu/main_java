package de.ruu.app.jeeeraaah.backend.persistence.jpa;

import de.ruu.app.jeeeraaah.common.api.domain.flat.TaskGroupFlat;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTOFlat;
import de.ruu.lib.jpa.core.AbstractRepository;
import de.ruu.lib.jpa.core.GraphType;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Subgraph;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Root;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static jakarta.persistence.criteria.JoinType.LEFT;

/**
 * Repository for TaskGroupJPA entities.
 * <p>
 * Note: This repository extends AutoCloseable but is CDI-managed and should NOT be closed manually.
 * The @SuppressWarnings("resource") annotation suppresses false-positive warnings.
 */
@Slf4j
@SuppressWarnings("resource") // Repository is CDI-managed, not manually closed
public abstract class TaskGroupRepositoryJPA extends AbstractRepository<TaskGroupJPA, Long>
{
	/**
	 * finds the task group with the given id and returns it with all its tasks
	 * @param id the id of the task group to find
	 * @return the task group with the given id and all its tasks
	 */
	public Optional<TaskGroupJPA> findWithTasks(@NonNull Long id)
	{
		EntityGraph<TaskGroupJPA> entityGraph = entityManager().createEntityGraph(TaskGroupJPA.class);
		entityGraph.addSubgraph(TaskGroupJPA_.TASKS);

		Map<String, Object> hints = new HashMap<>();
		hints.put(GraphType.FETCH.getName(), entityGraph);

		TaskGroupJPA result = entityManager().find(TaskGroupJPA.class, id, hints);

		return Optional.ofNullable(result);
	}

/*
schreibe eine Methode für die Klasse TaskGroupRepositoryJPA,
die für einen Parameter id vom Typ Long eine TaskGroupJPA liefert,
in der das Feld tasks mit allen TaskJPA Instanzen der TaskGroupJPA Instanz befüllt ist
und in denen jeweils die Felder taskGroup, superTask, subTasks, predecessors und successors befüllt sind.
Für diese Elemente sollen die Felder taskGroup, superTask, subTasks, predecessors und successors nicht befüllt werden.
*/
	/**
	 * finds the task group with the given id and returns it with all its tasks and their direct neighbours
	 * @param id the id of the task group to find
	 * @return the task group with the given id and all its tasks including their direct neighbours
	 */
	public Optional<TaskGroupJPA> findWithTasksAndDirectNeighbours(@NonNull Long id)
	{
		// --- Build an entity graph to ensure required relationships are fully loaded ---
		EntityGraph<TaskGroupJPA>     graph = entityManager().createEntityGraph(TaskGroupJPA.class);
		Subgraph<TaskJPA>         taskGraph = graph.addSubgraph(TaskGroupJPA_.TASKS);

		// Add all required task relationships to the graph
		taskGraph.addAttributeNodes(TaskJPA_.TASK_GROUP  );
		taskGraph.addAttributeNodes(TaskJPA_.SUPER_TASK  );
		taskGraph.addAttributeNodes(TaskJPA_.SUB_TASKS   );
		taskGraph.addAttributeNodes(TaskJPA_.PREDECESSORS);
		taskGraph.addAttributeNodes(TaskJPA_.SUCCESSORS  );

		// --- Create a type-safe criteria query equivalent to the JPQL ---
		CriteriaBuilder             cb = entityManager().getCriteriaBuilder();
		CriteriaQuery<TaskGroupJPA> cq = cb.createQuery(TaskGroupJPA.class);
		Root<TaskGroupJPA>          tg = cq.from(TaskGroupJPA.class);

		// Perform all required LEFT JOIN FETCH operations in a type-safe way
		Fetch<TaskGroupJPA, TaskJPA> tasks = tg.fetch(TaskGroupJPA_.tasks, LEFT);
		tasks.fetch(TaskJPA_.superTask   , LEFT);
		tasks.fetch(TaskJPA_.subTasks    , LEFT);
		tasks.fetch(TaskJPA_.predecessors, LEFT);
		tasks.fetch(TaskJPA_.successors  , LEFT);

		// SELECT DISTINCT tg
		cq.select(tg).distinct(true);

		// WHERE tg.id = :id
		cq.where(cb.equal(tg.get(TaskGroupJPA_.id), id));

		TypedQuery<TaskGroupJPA> query = entityManager().createQuery(cq).setHint("jakarta.persistence.fetchgraph", graph);

		return Optional.ofNullable(query.getSingleResult());
	}

//	public Optional<TaskGroupLazy> findLazy(@NonNull Long id)
//	{
//		Optional<TaskGroupJPA> optional = findWithTasks(id);
//
//		if (optional.isPresent())
//		{
//			TaskGroupJPA taskGroup = optional.get();
//			TaskGroupLazy result = new TaskGroupDTOLazy(taskGroup);
//			if (taskGroup.getTasks() != null)
//			{
//				for (TaskJPA t : taskGroup.getTasks()) result.taskIds().add(requireNonNull(t.getId()));
//			}
//			return Optional.of(result);
//		}
//		else return Optional.empty();
//	}
//
//	public Set<TaskGroupLazy> findAllLazy()
//	{
//		Set<TaskGroupJPA>  entities = findAll();
//		Set<TaskGroupLazy> result   = new HashSet<>();
//		for (TaskGroupJPA tgJPA : entities)
//		{
//			TaskGroupLazy tgLazy = new TaskGroupDTOLazy(tgJPA);
//			if (tgJPA.getTasks() != null)
//			{
//				for (TaskJPA t : tgJPA.getTasks()) tgLazy.taskIds().add(requireNonNull(t.getId()));
//			}
//			result.add(tgLazy);
//		}
//		return result;
//	}
//
//	public Optional<TaskGroupFlat> findFlat(@NonNull Long id)
//	{
//		Optional<TaskGroupJPA> optional = findWithTasks(id);
//
//		if (optional.isPresent())
//		{
//			TaskGroupJPA taskGroup = optional.get();
//			TaskGroupFlat result = new TaskGroupDTOFlat(taskGroup);
//			return Optional.of(result);
//		}
//		else return Optional.empty();
//	}

	public Set<TaskGroupFlat> findAllFlat()
	{
		return
				findAll()
						.stream()
						.filter
						(
								tg ->
								{
									if (tg.getId() == null)
									{
										log.error("irregular persistent task group with null id: {}", tg);
										return false;
									}
									log.debug("regular task group: {}", tg);
									return true;
								}
						)
						.map(TaskGroupDTOFlat::new)
						.collect(Collectors.toSet());
	}

	public boolean deleteFromGroup(@NonNull Long idGroup, @NonNull Long idTask)
	{
		Optional<TaskGroupJPA> optionalGroup = findWithTasks(idGroup);

		if (optionalGroup.isPresent())
		{
			EntityManager entityManager = entityManager();
			TaskGroupJPA  taskGroup     = optionalGroup.get();
			TaskJPA       task          = entityManager.find(TaskJPA.class, idTask); // returns managed instance or null

			if (task != null) // Ensure task exists before removing
			{
				if (task.subTasks().isPresent())
				{
					for (TaskJPA subTask : task.subTasks().get())
					{
						task.removeSubTask(subTask);
					}
				}
				if (task.predecessors().isPresent())
				{
					for (TaskJPA predecessor : task.predecessors().get())
					{
						task.removePredecessor(predecessor);
					}
				}
				if (task.successors().isPresent())
				{
					for (TaskJPA successor : task.successors().get())
					{
						task.removeSuccessor(successor);
					}
				}

				taskGroup.removeTask(task);
				// Managed entities will be flushed by the active transaction; no persist/merge required
				return true;
			}
			else
			{
				log.warn("Task with id {} not found in group with id {}", idTask, idGroup);
				return false;
			}
		}
		else
		{
			log.warn("Task group with id {} not found for deletion of task with id {}", idGroup, idTask);
			return false;
		}
	}
}