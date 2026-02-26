package de.ruu.app.jeeeraaah.backend.persistence.jpa;

import static de.ruu.lib.util.BooleanFunctions.not;
import static java.util.Objects.isNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import de.ruu.app.jeeeraaah.common.api.domain.RemoveNeighboursFromTaskConfig;
import de.ruu.app.jeeeraaah.common.api.domain.lazy.TaskLazy;
import de.ruu.app.jeeeraaah.common.api.domain.TaskRelationException;
import de.ruu.app.jeeeraaah.common.api.domain.TaskEntityService;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskCreationData;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.NotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * JPA-based implementation of TaskEntityService.
 * Provides CRUD operations and queries for TaskJPA entities.
 * <p>
 * Note: The repositories are CDI-managed beans and should NOT be closed manually.
 * The @SuppressWarnings("resource") annotation suppresses false-positive warnings
 * about try-with-resources for the repository() calls.
 *
 * @see TaskEntityService
 * @see TaskJPA
 */
@Slf4j
@SuppressWarnings("resource") // Repositories are CDI-managed, not manually closed
public abstract class TaskServiceJPA implements TaskEntityService<TaskGroupJPA, TaskJPA>
{
	protected abstract TaskRepositoryJPA repository();

	protected abstract TaskGroupRepositoryJPA taskGroupRepository();

	@PostConstruct
	private void postConstruct() {
		log.debug("repository available: {}", not(isNull(repository())));
	}

	@Override
	public @NonNull TaskJPA create(@NonNull TaskJPA entity) {
		return repository().create(entity);
	}

	/**
	 * Functional interface for mapping TaskLazy to TaskJPA. Allows injection of
	 * mapper implementation without circular
	 * dependency.
	 */
	@FunctionalInterface
	public interface TaskLazyMapper { @NonNull TaskJPA map(@NonNull TaskGroupJPA taskGroup, @NonNull TaskLazy taskLazy); }

	protected abstract TaskLazyMapper taskLazyMapper();

	/**
	 * Creates a new task from TaskCreationData within a transaction to avoid lazy
	 * initialization issues. This method
	 * performs the mapping inside the transaction boundary where the TaskGroupJPA
	 * is managed by the persistence context,
	 * allowing access to lazy-loaded collections.
	 *
	 * @param data task creation data containing task group ID and task lazy data
	 * @return the persisted task entity
	 * @throws EntityNotFoundException if the task group with the given ID does not
	 *                                 exist
	 */
	public @NonNull TaskJPA createFromData(@NonNull TaskCreationData data) {
		// find persistent task group - within transaction, so lazy collections can be
		// accessed
		Optional<? extends TaskGroupJPA> optional = taskGroupRepository().find(data.getTaskGroupId());
		if (optional.isEmpty()) {
			throw new EntityNotFoundException("task group with id " + data.getTaskGroupId() + " not found");
		}
		TaskGroupJPA taskGroup = optional.get();

		// map lazy dto to jpa entity - within transaction, taskGroup.addTask() can
		// access tasks collection
		TaskJPA taskJPA = taskLazyMapper().map(taskGroup, data.getTask());

		// persist and return
		return repository().create(taskJPA);
	}

	@Override public @NonNull Optional<TaskJPA> read(@NonNull Long id)
			{ return repository().find(id); }

	@Override public @NonNull TaskJPA update(@NonNull TaskJPA entity) throws EntityNotFoundException
			{ return repository().update(entity); }

	@Override public void delete(@NonNull Long id) throws EntityNotFoundException
			{ repository().delete(id); }

	@Override public @NonNull Set<TaskJPA> findAll()
			{ return new HashSet<>(repository().findAll()); }

	@Override public Optional<TaskJPA> findWithRelated(@NonNull Long id)
			{ return repository().findWithRelated(id); }

	@Override public void addSubTask(@NonNull TaskJPA task, @NonNull TaskJPA subTask)
			throws NotFoundException { repository().addSubTask(task.id(), subTask.id()); }

	@Override public void addPredecessor(@NonNull TaskJPA task, @NonNull TaskJPA predecessor)
			{ repository().addPredecessor(task.id(), predecessor.id()); }

	@Override
	public void addSuccessor(@NonNull TaskJPA task, @NonNull TaskJPA successor) {
		repository().addSuccessor(task.id(), successor.id());
	}

	@Override
	public void removeSubTask(@NonNull TaskJPA task, @NonNull TaskJPA subTask) {
		repository().removeSubTask(task.id(), subTask.id());
	}

	@Override
	public void removePredecessor(@NonNull TaskJPA task, @NonNull TaskJPA predecessor) {
		repository().removePredecessor(task.id(), predecessor.id());
	}

	@Override
	public void removeSuccessor(@NonNull TaskJPA task, @NonNull TaskJPA successor) {
		repository().removeSuccessor(task.id(), successor.id());
	}

	@Override
	public void removeNeighboursFromTask(@NonNull RemoveNeighboursFromTaskConfig removeNeighboursFromTaskConfig) {
		repository().removeNeighboursFromTask(removeNeighboursFromTaskConfig);
	}

	public void addSubTask(@NonNull Long idTask, @NonNull Long idSubTask) throws TaskRelationException {
		repository().addSubTask(idTask, idSubTask);
	}

	public void addPredecessor(@NonNull Long idTask, @NonNull Long idSucTask) throws TaskRelationException {
		repository().addPredecessor(idTask, idSucTask);
	}

	public void addSuccessor(@NonNull Long idTask, @NonNull Long idSubTask) throws TaskRelationException {
		repository().addSuccessor(idTask, idSubTask);
	}

	public void removeSubTask(@NonNull Long idTask, @NonNull Long idSubTask) throws TaskRelationException {
		repository().removeSubTask(idTask, idSubTask);
	}

	public void removePredecessor(@NonNull Long idTask, @NonNull Long idSubTask) throws TaskRelationException {
		repository().removePredecessor(idTask, idSubTask);
	}

	public void removeSuccessor(@NonNull Long idTask, @NonNull Long idSubTask) throws TaskRelationException {
		repository().removeSuccessor(idTask, idSubTask);
	}
}
