package de.ruu.app.jeeeraaah.backend.api.ws.rs;

import static de.ruu.app.jeeeraaah.common.api.domain.PathsCommon.TOKEN_BY_ID;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTaskGroup.TOKEN_ALL_FLAT;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTaskGroup.TOKEN_DOMAIN;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTaskGroup.TOKEN_WITH_TASKS;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTaskGroup.TOKEN_WITH_TASKS_AND_DIRECT_NEIGHBOURS;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.ok;
import static jakarta.ws.rs.core.Response.status;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.Optional;

import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskGroupDTOService;
import de.ruu.app.jeeeraaah.common.api.domain.exception.EntityNotFoundException;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTO;
import de.ruu.lib.ws_rs.ErrorResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller providing REST endpoints for task group operations.
 * <p>
 * Works exclusively with DTOs - no JPA entities are exposed.
 * All mapping between DTOs and JPA entities happens within the service layer,
 * ensuring proper JPMS encapsulation.
 *
 * @author r-uu
 */
@Path(TOKEN_DOMAIN) @ApplicationScoped @OpenAPIDefinition(info = @Info(version = "a version", title = "a title")) @Produces(APPLICATION_JSON) @Consumes(APPLICATION_JSON) @Timed @Slf4j
public class TaskGroupService
{
	private static final String MSG_TASK_NOT_FOUND = "task with id %d not found";
	private static final String MSG_TASK_GROUP_NOT_FOUND = "task group with id %d not found";
	private static final String MSG_TASK_GROUP_READ_FAILED = "failed to read task group with id %d: %s";
	private static final String MSG_TASK_GROUP_READ_FAILED_XCPTN = "failed to create task group with id %d: %s";
	private static final String MSG_TASK_GROUP_CREATE_FAILED = "failed to create task group: %s";
	private static final String MSG_TASK_GROUP_UPDATE_FAILED = "failed to update task group with id %d: %s";
	private static final String MSG_TASK_GROUP_DELETE_FAILED = "failed to delete task group with id %d: %s";
	private static final String MSG_TASK_REMOVAL_FAILED = "failed to remove task with id %d from group %d: %s";

	@Inject
	private TaskGroupDTOService service;

	@Operation
	(
		summary = "create a new task group",
		description = "creates a new task group with the provided details"
	)
	@APIResponse
	(
		responseCode = "201",
		description = "task group created successfully",
		content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = TaskGroupDTO.class))
	)
	@APIResponse
	(
		responseCode = "400",
		description = "invalid input provided",
		content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class))
	)
	@APIResponse
	(
		responseCode = "500",
		description = "internal server error",
		content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class))
	)
	@RolesAllowed("taskgroup-create")
	@POST
	public Response create(@NonNull TaskGroupDTO dto)
	{
		try
		{
			TaskGroupDTO result = service.create(dto);
			return status(CREATED).entity(result).build();
		}
		catch (Exception e)
		{
			log.error("Error creating task group", e);
			return status(BAD_REQUEST).entity(String.format(MSG_TASK_GROUP_CREATE_FAILED, e.getMessage())).build();
		}
	}

	@Operation(summary = "get task group by ID", description = "returns a task group by its ID") @APIResponse(responseCode = "200", description = "task group found", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = TaskGroupDTO.class))) @APIResponse(responseCode = "404", description = "task group not found")
	@Path(TOKEN_BY_ID)
	@RolesAllowed("taskgroup-read")
	@GET
	public Response read(
			@Parameter(description = "ID of the task group", required = true) @PathParam("id") @NonNull Long id)
	{
		try
		{
			return service.read(id).map(taskGroup -> ok(taskGroup).build())
					.orElseGet(() -> status(NOT_FOUND).entity(String.format(MSG_TASK_GROUP_NOT_FOUND, id)).build());
		}
		catch (Exception e)
		{
			log.error(String.format(MSG_TASK_GROUP_READ_FAILED, id, e));
			return status(INTERNAL_SERVER_ERROR).entity(String.format(MSG_TASK_GROUP_READ_FAILED_XCPTN, id, e.getMessage()))
					.build();
		}
	}

	@Operation(summary = "update a persistent task group or create a new task group", description = "updates a persistent task group with the provided details or creates a new task group") @APIResponse(responseCode = "200", description = "persistent task group updated or non-persistent task group created successfully", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = TaskGroupDTO.class))) @APIResponse(responseCode = "400", description = "invalid input provided")
	@RolesAllowed("taskgroup-update")
	@PUT
	public Response update(@NonNull TaskGroupDTO dto)
	{
		try
		{
			TaskGroupDTO result = service.update(dto);
			return ok(result).build();
		}
		catch (EntityNotFoundException e)
		{
			return status(NOT_FOUND).entity(String.format(MSG_TASK_GROUP_NOT_FOUND, dto.getId())).build();
		}
		catch (Exception e)
		{
			log.error(String.format("error updating task group with id %d", dto.getId()), e);
			return status(BAD_REQUEST).entity(String.format(MSG_TASK_GROUP_UPDATE_FAILED, dto.getId(), e.getMessage()))
					.build();
		}
	}

	@Operation(summary = "delete a task group", description = "deletes a task group by its ID") @APIResponse(responseCode = "200", description = "task group deleted successfully") @APIResponse(responseCode = "404", description = "task group not found")
	@Path(TOKEN_BY_ID)
	@RolesAllowed("taskgroup-delete")
	@DELETE
	public Response delete(
			@Parameter(description = "ID of the task group to delete", required = true) @PathParam("id") @NonNull Long id)
	{
		log.debug("-".repeat(20) + "attempting to delete a task group with id {}" + "-".repeat(20), id);
		try
		{
			service.delete(id);
			return ok().build();
		}
		catch (EntityNotFoundException e)
		{
			return status(NOT_FOUND).entity(String.format(MSG_TASK_GROUP_NOT_FOUND, id)).build();
		}
		catch (Exception e)
		{
			log.error(String.format(MSG_TASK_GROUP_DELETE_FAILED, id, e.getMessage()), e);
			return status(INTERNAL_SERVER_ERROR).entity(String.format(MSG_TASK_GROUP_DELETE_FAILED, id, e.getMessage()))
					.build();
		}
	}

	/**
	 * Finds all task groups in the backend, task groups are flat which means they do not contain information to related
	 * tasks.
	 * <p>
	 * This is optimized for performance when only basic task group information is needed.
	 *
	 * @return a {@link Response} containing a set of flattened task group information
	 */
	@Operation(summary = "get all task groups (flat)", description = "retrieves a flat list of all task groups with minimal information for efficient loading") @APIResponse(responseCode = "200", description = "list of task groups retrieved successfully", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = TaskGroupDTO.class)))
	@Path(TOKEN_ALL_FLAT)
	@RolesAllowed("taskgroup-read")
	@GET
	public Response findAllFlat()
	{
		try
		{
			return ok(service.findAllFlat()).build();
		}
		catch (Exception e)
		{
			// TODO: use internalServerError
			log.error("failed to retrieve list of flat task groups: {}", e.getMessage(), e);
			return status(INTERNAL_SERVER_ERROR).entity("Failed to retrieve task groups list: " + e.getMessage()).build();
		}
	}

	@Path(TOKEN_WITH_TASKS + TOKEN_BY_ID)
	@RolesAllowed("taskgroup-read")
	@GET
	public Response findWithTasks(
			@Parameter(description = "id of the task group to retrieve", required = true) @PathParam("id") @NonNull Long id)
	{
		log.debug("attempting to retrieve task group with id {}", id);
		try
		{
			Optional<TaskGroupDTO> optional = service.findWithTasks(id);

			if (optional.isPresent())
			{
				return ok(optional.get()).build();
			}
			else
			{
				return status(NOT_FOUND).entity(String.format(MSG_TASK_GROUP_NOT_FOUND, id)).build();
			}
		}
		catch (Exception e)
		{
			log.error(String.format(MSG_TASK_GROUP_READ_FAILED, id, e.getMessage()), e);
			return status(INTERNAL_SERVER_ERROR).entity(String.format(MSG_TASK_GROUP_READ_FAILED_XCPTN, id, e.getMessage()))
					.build();
		}
	}

	@Path(TOKEN_WITH_TASKS_AND_DIRECT_NEIGHBOURS + TOKEN_BY_ID)
	@RolesAllowed("taskgroup-read")
	@GET
	public Response findWithTasksAndDirectNeighbours(
			@Parameter(description = "id of the task group to retrieve with tasks and their direct neighbours", required = true) @PathParam("id") @NonNull Long id)
	{
		log.debug("attempting to retrieve task group with tasks and direct neighbours, id {}", id);
		try
		{
			Optional<TaskGroupDTO> optional = service.findWithTasksAndDirectNeighbours(id);

			if (optional.isPresent())
			{
				return ok(optional.get()).build();
			}
			else
			{
				return status(NOT_FOUND).entity(String.format(MSG_TASK_GROUP_NOT_FOUND, id)).build();
			}
		}
		catch (Exception e)
		{
			log.error(String.format(MSG_TASK_GROUP_READ_FAILED, id, e.getMessage()), e);
			return status(INTERNAL_SERVER_ERROR).entity(String.format(MSG_TASK_GROUP_READ_FAILED_XCPTN, id, e.getMessage()))
					.build();
		}
	}
}