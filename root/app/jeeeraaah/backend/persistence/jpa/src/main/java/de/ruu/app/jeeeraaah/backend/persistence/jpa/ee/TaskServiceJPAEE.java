package de.ruu.app.jeeeraaah.backend.persistence.jpa.ee;

import static java.util.Objects.nonNull;

import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskLazyMapper;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskRelationValidator;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.internal.TaskGroupRepositoryJPA;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.internal.TaskRepositoryJPA;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.internal.TaskServiceJPA;
import de.ruu.app.jeeeraaah.common.api.domain.TaskRelationException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Transactional // jakarta.transaction.Transactional interceptor
@Slf4j
public class TaskServiceJPAEE extends TaskServiceJPA
{
	@Inject private TaskRepositoryJPAEE      repository;
	@Inject private TaskGroupRepositoryJPAEE taskGroupRepository;
	@Inject private TaskLazyMapper           mapper;

	/** All registered constraint validators (e.g. timecycle). Empty if no module provides one. */
	@Inject @Any private Instance<TaskRelationValidator> validators;

	@PostConstruct private void postConstruct()
	{
		log.debug("repository available         : {}", nonNull(repository         ));
		log.debug("taskGroupRepository available: {}", nonNull(taskGroupRepository));
		log.debug("mapper available             : {}", nonNull(mapper             ));
	}

	@Override protected TaskRepositoryJPA      repository         () { return repository;          }
	@Override protected TaskGroupRepositoryJPA taskGroupRepository() { return taskGroupRepository; }
	@Override protected TaskLazyMapper         taskLazyMapper     () { return mapper;              }

	// -------------------------------------------------------------------------
	// Delegate to validators BEFORE the structural operation
	// -------------------------------------------------------------------------

	@Override
	public void addPredecessor(@NonNull Long idTask, @NonNull Long idPredecessorTask)
			throws TaskRelationException
	{
		for (TaskRelationValidator v : validators)
			v.validateBeforeAddPredecessor(idTask, idPredecessorTask);
		super.addPredecessor(idTask, idPredecessorTask);
	}

	@Override
	public void addSuccessor(@NonNull Long idTask, @NonNull Long idSuccessorTask)
			throws TaskRelationException
	{
		for (TaskRelationValidator v : validators)
			v.validateBeforeAddSuccessor(idTask, idSuccessorTask);
		super.addSuccessor(idTask, idSuccessorTask);
	}
}

