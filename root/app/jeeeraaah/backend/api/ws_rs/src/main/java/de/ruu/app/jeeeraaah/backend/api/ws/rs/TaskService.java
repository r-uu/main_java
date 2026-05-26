package de.ruu.app.jeeeraaah.backend.api.ws.rs;

import static de.ruu.app.jeeeraaah.common.api.domain.PathsCommon.TOKEN_BY_ID;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTask.TOKEN_ADD_PREDECESSOR;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTask.TOKEN_SET_SUPER_TASK;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTask.TOKEN_REMOVE_SUPER_TASK;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTask.TOKEN_ALL;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTask.TOKEN_BY_ID_WITH_RELATED;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTask.TOKEN_DOMAIN;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTask.TOKEN_REMOVE_NEIGHBOURS_FROM_TASK;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTask.TOKEN_REMOVE_PREDECESSOR;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTask.TOKEN_ADD_SUCCESSOR;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTask.TOKEN_REMOVE_SUCCESSOR;
import static de.ruu.lib.util.BooleanFunctions.not;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.CONFLICT;
import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.Optional;
import java.util.Set;

import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskDTOService;
import de.ruu.app.jeeeraaah.common.api.domain.InterTaskRelationData;
import de.ruu.app.jeeeraaah.common.api.domain.RemoveNeighboursFromTaskConfig;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskCreationData;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller providing REST endpoints for task operations.
 * <p>
 * Works exclusively with DTOs - no JPA entities are exposed.
 * All mapping between DTOs and JPA entities happens within the service layer,
 * ensuring proper JPMS encapsulation.
 *
 * @author r-uu
 */
@ApplicationScoped
@Path(TOKEN_DOMAIN)
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@OpenAPIDefinition(info = @Info(version = "a version", title = "a title"))
@Timed
@Slf4j
public class TaskService
{
	@Inject private TaskDTOService taskService;

	@POST
	@RolesAllowed("task-create")
	public Response create(@Valid TaskCreationData data)
	{
		log.debug("Received TaskCreationData:");
		log.debug("  data: {}", data);
		log.debug("  taskGroupId: {}", data != null ? data.getTaskGroupId() : "data is null");
		log.debug("  task: {}", data != null ? data.getTask() : "data is null");

		// Service handles mapping and transaction internally
		TaskDTO result = taskService.create(data);
		return Response.status(CREATED).entity(result).build();
	}

	@GET
	@Path(TOKEN_BY_ID)
	@Produces(APPLICATION_JSON)
	@RolesAllowed("task-read")
	public Response read(@PathParam("id") Long id)
	{
		Optional<TaskDTO> optional = taskService.read(id);
		if (not(optional.isPresent()))
			return Response.status(NOT_FOUND).entity("task with id " + id + " not found").build();
		
		TaskDTO result = optional.get();
		log.debug("found task for id {}\n{}", id, result);
		return Response.ok(result).build();
	}

	@PUT
	@RolesAllowed("task-update")
	public Response update(@NonNull TaskDTO dto)
	{
		TaskDTO result = taskService.update(dto);
		return Response.ok(result).build();
	}

	@DELETE
	@Path(TOKEN_BY_ID)
	@RolesAllowed("task-delete")
	public Response delete(@PathParam("id") Long id)
	{
		try { taskService.delete(id); }
		catch (Exception e)
		{
			log.error("failed to delete task with id {}", id, e);
			return Response.status(CONFLICT).build();
		}
		return Response.ok().build();
	}

	@GET
	@Path(TOKEN_ALL)
	@Produces(APPLICATION_JSON)
	@RolesAllowed("task-read")
	public Response findAll()
	{
		Set<TaskDTO> result = taskService.findAll();
		log.debug("found {} tasks", result.size());
		return Response.ok(result).build();
	}

	@GET
	@Path(TOKEN_BY_ID_WITH_RELATED + TOKEN_BY_ID)
	@Produces(APPLICATION_JSON)
	@RolesAllowed("task-read")
	public Response findByIdWithRelated(@PathParam("id") Long id)
	{
		Optional<TaskDTO> optional = taskService.findWithRelated(id);
		if (not(optional.isPresent()))
			return Response.status(NOT_FOUND).entity("task with id " + id + " not found").build();
		
		return Response.ok(optional.get()).build();
	}

	// @POST // use POST for bulk operations
	// @Path(PATH_BY_IDS_LAZY)
	// @Produces(APPLICATION_JSON)
	// public Response findByIdsWithRelatedLazy(Set<Long> ids)
	// {
	// Set<TaskLazy> result = new HashSet<>();
	// taskService.findGroupTasksWithRelated(ids).forEach(t -> result.add(toLazy(t,
	// new ReferenceCycleTracking())));
	// return Response.ok(result).build();
	// }

	@PUT
	@Path(TOKEN_SET_SUPER_TASK)
	@Consumes(APPLICATION_JSON)
	@Produces(APPLICATION_JSON)
	@RolesAllowed("task-update")
	public Response setSuperTask(@NonNull InterTaskRelationData data)
	{
		log.debug("set super task — id child: {}, id super task: {}", data.id(), data.idRelated());
		taskService.setSuperTask(data.id(), data.idRelated());
		return Response.ok().build();
	}

	@PUT
	@Path(TOKEN_REMOVE_SUPER_TASK + TOKEN_BY_ID)
	@Produces(APPLICATION_JSON)
	@RolesAllowed("task-update")
	public Response removeSuperTask(@PathParam("id") Long childId)
	{
		log.debug("remove super task — id child: {}", childId);
		taskService.removeSuperTask(childId);
		return Response.ok().build();
	}

	@PUT
	@Path(TOKEN_ADD_PREDECESSOR)
	@Consumes(APPLICATION_JSON)
	@Produces(APPLICATION_JSON)
	@RolesAllowed("task-update")
	public Response addPredecessor(@NonNull InterTaskRelationData data)
	{
		log.debug("id task: {}, id predecessor task: {}", data.id(), data.idRelated());
		// TaskRelationException is handled by TaskRelationExceptionMapper → mapped to 400
		// NotFoundException propagates to 404
		taskService.addPredecessor(data.id(), data.idRelated());
		return Response.ok().build();
	}

	@PUT
	@Path(TOKEN_ADD_SUCCESSOR)
	@Consumes(APPLICATION_JSON)
	@Produces(APPLICATION_JSON)
	@RolesAllowed("task-update")
	public Response addSuccessor(@NonNull InterTaskRelationData data)
	{
		log.debug("id task: {}, id successor task: {}", data.id(), data.idRelated());
		// TaskRelationException is handled by TaskRelationExceptionMapper → mapped to 400
		taskService.addSuccessor(data.id(), data.idRelated());
		return Response.ok().build();
	}

	@PUT
	@Path(TOKEN_REMOVE_PREDECESSOR)
	@Consumes(APPLICATION_JSON)
	@Produces(APPLICATION_JSON)
	@RolesAllowed("task-update")
	// public Response removePredecessor(@PathParam("idTask") Long idTask,
	// @PathParam("idPredecessor") Long idPredecessor)
	public Response removePredecessor(@NonNull InterTaskRelationData data)
	{
		log.debug("id task: {}, id predecessor task: {}", data.id(), data.idRelated());
		try { taskService.removePredecessor(data.id(), data.idRelated()); }
		catch (NotFoundException e) { return Response.status(NOT_FOUND).entity(e.getMessage()).build(); }
		return Response.ok().build();
	}

	@PUT
	@Path(TOKEN_REMOVE_SUCCESSOR)
	@Consumes(APPLICATION_JSON)
	@Produces(APPLICATION_JSON)
	@RolesAllowed("task-update")
	public Response removeSuccessor(@NonNull InterTaskRelationData data)
	{
		log.debug("id task: {}, id successor task: {}", data.id(), data.idRelated());
		try { taskService.removeSuccessor(data.id(), data.idRelated()); }
		catch (NotFoundException e) { return Response.status(NOT_FOUND).entity(e.getMessage()).build(); }
		return Response.ok().build();
	}

	@PUT
	@Path(TOKEN_REMOVE_NEIGHBOURS_FROM_TASK)
	@Consumes(APPLICATION_JSON)
	@Produces(APPLICATION_JSON)
	@RolesAllowed("task-update")
	public Response removeNeighboursFromTask(@NonNull RemoveNeighboursFromTaskConfig data)
	{
		log.debug("removing from neighbours from task with id: {}", data.idTask());
		try { taskService.removeNeighboursFromTask(data); }
		catch (NotFoundException e) { return Response.status(NOT_FOUND).entity(e.getMessage()).build(); }
		return Response.ok().build();
	}
}
