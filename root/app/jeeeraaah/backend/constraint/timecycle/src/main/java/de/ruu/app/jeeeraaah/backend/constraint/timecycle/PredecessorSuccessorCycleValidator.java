package de.ruu.app.jeeeraaah.backend.constraint.timecycle;

import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskRelationValidator;
import de.ruu.app.jeeeraaah.common.api.domain.TaskRelationException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.nonNull;

/**
 * CDI-managed validator that prevents predecessor/successor cycles.
 * <p>
 * Discovered by the persistence layer via {@code Instance<TaskRelationValidator>}
 * injection in {@code TaskServiceJPAEE}. No existing module needs to depend on
 * this class — the coupling is entirely unidirectional via CDI.
 * <p>
 * <strong>JPA strategy — one query, then in-memory DFS:</strong><br>
 * All predecessor edges are loaded in a single JPQL query at the start of each
 * validation call. The DFS then runs entirely in memory on the resulting adjacency
 * map. This avoids the N+1 problem (one DB round-trip per visited node) at the
 * cost of transferring the full edge set once.
 * <p>
 * For very large graphs (thousands of tasks), a PostgreSQL recursive CTE
 * ({@code WITH RECURSIVE}) would be more efficient because it only traverses the
 * subgraph reachable from the start node. At typical task-management scale
 * (tens to a few hundred tasks) the single-query approach is simpler and sufficient.
 * <p>
 * Complexity: 1 DB round-trip + O(V + E) in-memory DFS,
 * where V = all tasks with predecessor edges, E = all predecessor edges in the system.
 */
@ApplicationScoped
@Slf4j
public class PredecessorSuccessorCycleValidator implements TaskRelationValidator
{
	@PersistenceContext(unitName = "jeeeraaah_test")
	private EntityManager entityManager;

	@PostConstruct
	private void postConstruct()
	{
		log.debug("PredecessorSuccessorCycleValidator ready, entityManager available: {}", nonNull(entityManager));
	}
// -------------------------------------------------------------------------
// TaskRelationValidator interface
// -------------------------------------------------------------------------

	/**
	 * Validates that adding {@code predecessorId} as a predecessor of {@code taskId}
	 * does not create a cycle.
	 * <p>
	 * A cycle would form if {@code taskId} is already a (transitive) predecessor of
	 * {@code predecessorId}, because the new edge would close the loop:
	 * {@code predecessorId -> taskId -> ... -> predecessorId}.
	 */
	@Override
	public void validateBeforeAddPredecessor(@NonNull Long taskId, @NonNull Long predecessorId)
			throws TaskRelationException
	{
		log.debug("validateBeforeAddPredecessor: taskId={}, predecessorId={}", taskId, predecessorId);
		Map<Long, List<Long>> graph = loadAllPredecessorEdges();
		if (isReachable(graph, predecessorId, taskId))
			throw new TaskRelationException(
					"adding predecessor with id " + predecessorId + " to task with id " + taskId
							+ " would create a cycle in the predecessor/successor relationship");
	}

	/**
	 * Validates that adding {@code successorId} as a successor of {@code taskId}
	 * does not create a cycle.
	 * <p>
	 * addSuccessor(taskId, successorId) is equivalent to addPredecessor(successorId, taskId):
	 * the same graph is checked, arguments are swapped.
	 */
	@Override
	public void validateBeforeAddSuccessor(@NonNull Long taskId, @NonNull Long successorId)
			throws TaskRelationException
	{
		log.debug("validateBeforeAddSuccessor: taskId={}, successorId={}", taskId, successorId);
		Map<Long, List<Long>> graph = loadAllPredecessorEdges();
// addSuccessor(taskId, successorId)  is equivalent to  addPredecessor(successorId, taskId)
		if (isReachable(graph, taskId, successorId))
			throw new TaskRelationException(
					"adding successor with id " + successorId + " to task with id " + taskId
							+ " would create a cycle in the predecessor/successor relationship");
	}
// -------------------------------------------------------------------------
// Graph loading -- single query, then in-memory
// -------------------------------------------------------------------------

	/**
	 * Loads the complete predecessor-edge set in one JPQL query.
	 * <p>
	 * Returns a map: taskId -> [directPredecessorId, ...]
	 * <p>
	 * Uses JPQL (not native SQL) so Hibernate handles column-name mapping
	 * and quoting consistently with the entity definition.
	 */
	private Map<Long, List<Long>> loadAllPredecessorEdges()
	{
		List<Object[]> rows = entityManager
				.createQuery(
// t.id  = the task (successor side)
// p.id  = the predecessor of t
						"SELECT t.id, p.id FROM TaskJPA t JOIN t.predecessors p",
						Object[].class)
				.getResultList();
		Map<Long, List<Long>> predecessorsOf = new HashMap<>(rows.size() * 2);
		for (Object[] row : rows)
		{
			Long taskId = (Long) row[0];
			Long predId = (Long) row[1];
			predecessorsOf.computeIfAbsent(taskId, k -> new ArrayList<>()).add(predId);
		}
		log.debug("loaded predecessor graph: {} edges", rows.size());
		return predecessorsOf;
	}
// -------------------------------------------------------------------------
// DFS -- runs entirely in memory on the pre-loaded adjacency map
// -------------------------------------------------------------------------

	/**
	 * Iterative DFS: returns true if targetId is reachable from startId
	 * by following predecessor edges in graph.
	 * <p>
	 * The visited set deduplicates nodes -- this also handles graphs that already
	 * contain a cycle (defensive against inconsistent DB state).
	 */
	private boolean isReachable(Map<Long, List<Long>> graph, long startId, long targetId)
	{
		Set<Long> visited = new HashSet<>();
		Deque<Long> stack = new ArrayDeque<>();
		stack.push(startId);
		while (!stack.isEmpty())
		{
			long current = stack.pop();
			if (current == targetId) return true;
			if (!visited.add(current)) continue;
			List<Long> preds = graph.getOrDefault(current, List.of());
			for (Long predId : preds)
				stack.push(predId);
		}
		return false;
	}
}
