package de.ruu.app.jeeeraaah.backend.api.ws.rs.service;

import de.ruu.app.jeeeraaah.backend.common.mapping.Mappings;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskDTOService;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.entity.TaskJPA;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.entity.TaskGroupJPA;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskCreationService;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskRelationService;
import de.ruu.app.jeeeraaah.common.api.domain.RemoveNeighboursFromTaskConfig;
import de.ruu.app.jeeeraaah.common.api.domain.TaskRelationException;
import de.ruu.app.jeeeraaah.common.api.domain.exception.EntityNotFoundException;
import de.ruu.app.jeeeraaah.common.api.domain.TaskEntityService;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskCreationData;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class TaskDTOServiceImpl implements TaskDTOService
{
	@Inject private TaskCreationService creationService;
	@Inject private TaskEntityService<TaskGroupJPA, TaskJPA> entityService;
	@Inject private TaskRelationService relationService;

	@Override
	public @NonNull TaskDTO create(@NonNull TaskCreationData data)
	{
		TaskJPA entity = creationService.createFromData(data);
		return Mappings.toDTO(entity, new ReferenceCycleTracking());
	}

	@Override
	public @NonNull Optional<TaskDTO> read(@NonNull Long id)
	{
	return entityService.read(id)
	.map(entity -> Mappings.toDTO(entity, new ReferenceCycleTracking()));
	}

	@Override
	public @NonNull TaskDTO update(@NonNull TaskDTO dto)
	{
		ReferenceCycleTracking context = new ReferenceCycleTracking();
		TaskJPA entity = entityService.update(Mappings.toJPA(dto, context));
		return Mappings.toDTO(entity, context);
	}

	@Override
	public void delete(@NonNull Long id) throws EntityNotFoundException
	{
	entityService.delete(id);
	}

	@Override
	public @NonNull Set<TaskDTO> findAll()
	{
	return entityService.findAll().stream()
	.map(entity -> Mappings.toDTO(entity, new ReferenceCycleTracking()))
	.collect(Collectors.toSet());
	}

	@Override
	public @NonNull Optional<TaskDTO> findWithRelated(@NonNull Long id)
	{
	return entityService.findWithRelated(id)
	.map(entity -> Mappings.toDTO(entity, new ReferenceCycleTracking()));
	}

	@Override
	public void setSuperTask(@NonNull Long idChild, @NonNull Long idSuperTask) throws TaskRelationException, EntityNotFoundException
	{
	relationService.setSuperTask(idChild, idSuperTask);
	}

	@Override
	public void removeSuperTask(@NonNull Long idChild) throws TaskRelationException
	{
	relationService.removeSuperTask(idChild);
	}

	@Override
	public void addPredecessor(@NonNull Long taskId, @NonNull Long predecessorId) throws TaskRelationException, EntityNotFoundException
	{
	relationService.addPredecessor(taskId, predecessorId);
	}

	@Override
	public void addSuccessor(@NonNull Long taskId, @NonNull Long successorId) throws TaskRelationException, EntityNotFoundException
	{
	relationService.addSuccessor(taskId, successorId);
	}


	@Override
	public void removePredecessor(@NonNull Long taskId, @NonNull Long predecessorId) throws EntityNotFoundException
	{
	relationService.removePredecessor(taskId, predecessorId);
	}

	@Override
	public void removeSuccessor(@NonNull Long taskId, @NonNull Long successorId) throws EntityNotFoundException
	{
	relationService.removeSuccessor(taskId, successorId);
	}

	@Override
	public void removeNeighboursFromTask(@NonNull RemoveNeighboursFromTaskConfig config) throws EntityNotFoundException
	{
		Long taskId = config.idTask();
		
		// Remove from predecessors
		if (config.removeFromPredecessors() != null)
		{
			for (Long predecessorId : config.removeFromPredecessors())
			{
				relationService.removePredecessor(taskId, predecessorId);
			}
		}
		
		// Remove from sub tasks: detach each sub task from its parent
		if (config.removeFromSubTasks() != null)
		{
			for (Long subTaskId : config.removeFromSubTasks())
			{
				relationService.removeSuperTask(subTaskId);
			}
		}
		
		// Remove from successors
		if (config.removeFromSuccessors() != null)
		{
			for (Long successorId : config.removeFromSuccessors())
			{
				relationService.removeSuccessor(taskId, successorId);
			}
		}
		
		// Note: removeFromSuperTask not yet implemented
		// Would require finding parent task and calling removeSubTask
	}
}
