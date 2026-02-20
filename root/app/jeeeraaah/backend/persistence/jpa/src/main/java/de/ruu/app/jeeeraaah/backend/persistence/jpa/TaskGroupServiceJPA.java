package de.ruu.app.jeeeraaah.backend.persistence.jpa;

import de.ruu.app.jeeeraaah.common.api.domain.flat.TaskGroupFlat;
import de.ruu.app.jeeeraaah.common.api.domain.lazy.TaskGroupLazy;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupService;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.Set;

import static de.ruu.lib.util.BooleanFunctions.not;
import static java.util.Objects.isNull;

/** TODO: rename and move this type (no JPA suffix) */
@Slf4j
public abstract class TaskGroupServiceJPA
		implements TaskGroupService<TaskGroupJPA>
{
	protected abstract TaskGroupRepositoryJPA repository();

	@PostConstruct private void postConstruct() { log.debug("repository available: {}", not(isNull(repository()))); }

	@Override public @NonNull TaskGroupJPA  create(@NonNull TaskGroupJPA entity) { return repository().create(entity); }
	@Override public Optional<TaskGroupJPA> read  (@NonNull Long        id     ) { return repository().find  (id    ); }
	@Override public @NonNull TaskGroupJPA  update(@NonNull TaskGroupJPA entity) { return repository().update(entity); }
	@Override public          void          delete(@NonNull Long id            ) {        repository().delete(id    ); }

	@Override public          Optional<TaskGroupJPA>  findWithTasks(@NonNull Long id)
			{ return repository().findWithTasks(id); }
	@Override public          Optional<TaskGroupJPA>  findWithTasksAndDirectNeighbours(@NonNull Long id)
			{ return repository().findWithTasksAndDirectNeighbours(id); }
	@Override public @NonNull Set<TaskGroupFlat>      findAllFlat  ()
			{ return repository().findAllFlat  ();   }
//	@Override public          Optional<TaskGroupLazy> findLazy(@NonNull Long id)
//			{ return repository().findLazy     (id); }

	@Override public void removeTaskFromGroup(@NonNull Long idGroup, @NonNull Long idTask)
			{ repository().deleteFromGroup(idGroup, idTask); }
}