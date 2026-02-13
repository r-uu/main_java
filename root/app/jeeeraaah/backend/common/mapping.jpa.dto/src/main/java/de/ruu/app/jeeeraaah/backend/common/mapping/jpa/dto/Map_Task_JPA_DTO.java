package de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto;

import static java.util.Objects.isNull;

import java.util.Set;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import org.mapstruct.factory.Mappers;

import de.ruu.app.jeeeraaah.backend.common.mapping.Mappings;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskJPA;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskJPA_;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTO;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceUtil;
import lombok.NonNull;

/** {@link TaskJPA} -> {@link TaskDTO}
 *
 * NOTE (EN): This mapper expects the project to use the lombok-mapstruct-binding so that
 * MapStruct recognizes Lombok's fluent accessors. Without the binding MapStruct might fall back
 * to bean-style accessors and some fields might not be mapped automatically.
 */
@Mapper public interface Map_Task_JPA_DTO
{
	Map_Task_JPA_DTO INSTANCE = Mappers.getMapper(Map_Task_JPA_DTO.class);

	@Mapping(target = "name", ignore = true) // set in constructor
	@Mapping(target = "closed", ignore = true) // mapped in afterMapping
	@Mapping(target = "taskGroup"   , ignore = true) // ignore task group, because it is mapped in object factory
	@Mapping(target = "superTask"   , ignore = true)
	@Mapping(target = "subTasks"    , ignore = true)
	@Mapping(target = "predecessors", ignore = true)
	@Mapping(target = "successors"  , ignore = true)
	@Mapping(target = "description" , ignore = true) // mapped in afterMapping
	@Mapping(target = "start"       , ignore = true) // mapped in afterMapping
	@Mapping(target = "end"         , ignore = true) // mapped in afterMapping
	@NonNull
	TaskDTO map(@NonNull TaskJPA in, @NonNull @Context ReferenceCycleTracking context);

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@BeforeMapping default void beforeMapping
			(
					@NonNull                TaskJPA                in,
					@NonNull @MappingTarget TaskDTO                out,
					@NonNull @Context       ReferenceCycleTracking context
			)
	{
		// required arguments id, version and name are already set in constructor
	}

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@AfterMapping default void afterMapping
			(
					@NonNull                TaskJPA                in,
					@NonNull @MappingTarget TaskDTO                out,
					@NonNull @Context       ReferenceCycleTracking context
			)
	{
		PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
		if (in.superTask().isPresent())
		{
			TaskJPA relatedTask = in.superTask().get();
			if (persistenceUtil.isLoaded(in, TaskJPA_.superTask.getName()))
			{
				// check if related task was already mapped
				TaskDTO relatedTaskMapped = context.get(relatedTask, TaskDTO.class);
				if (isNull(relatedTaskMapped))
						// start new mapping for related task
						out.superTask(Mappings.toDTO(relatedTask, context));
				else
						// use already mapped related task
						out.superTask(relatedTaskMapped);
			}
		}
		if (in.subTasks().isPresent())
		{
			Set<TaskJPA> relatedTasks = in.subTasks().get();
			if (persistenceUtil.isLoaded(in, TaskJPA_.subTasks.getName()))
			{
				for (TaskJPA relatedTask : relatedTasks)
				{
					// check if related task was already mapped
					TaskDTO relatedTaskMapped = context.get(relatedTask, TaskDTO.class);
					if (isNull(relatedTaskMapped))
							// start new mapping for related task
							out.addSubTask(Mappings.toDTO(relatedTask, context));
					else
							// use already mapped related task
							out.addSubTask(relatedTaskMapped);
				}
			}
		}
		if (in.predecessors().isPresent())
		{
			Set<TaskJPA> relatedTasks = in.predecessors().get();
			if (persistenceUtil.isLoaded(in, TaskJPA_.predecessors.getName()))
			{
				for (TaskJPA relatedTask : relatedTasks)
				{
					// check if related task was already mapped
					TaskDTO relatedTaskMapped = context.get(relatedTask, TaskDTO.class);
					if (isNull(relatedTaskMapped))
							// start new mapping for related task
							out.addPredecessor(Mappings.toDTO(relatedTask, context));
					else
							// use already mapped related task
							out.addPredecessor(relatedTaskMapped);
				}
			}
		}
		if (in.successors().isPresent())
		{
			Set<TaskJPA> relatedTasks = in.successors().get();
			if (persistenceUtil.isLoaded(in, TaskJPA_.successors.getName()))
			{
				for (TaskJPA relatedTask : relatedTasks)
				{
					// check if related task was already mapped
					TaskDTO relatedTaskMapped = context.get(relatedTask, TaskDTO.class);
					if (isNull(relatedTaskMapped))
							// start new mapping for related task
							out.addSuccessor(Mappings.toDTO(relatedTask, context));
					else
							// use already mapped related task
							out.addSuccessor(relatedTaskMapped);
				}
			}
		}
		// Map mutable fields
		mapMutableFields(in, out);
	}

	/**
	 * Helper method to map mutable fields that MapStruct cannot auto-map.
	 * 
	 * Note: With @ObjectFactory, MapStruct skips automatic field mapping.
	 * We must manually map:
	 * - closed: Boolean field (MapStruct should handle this, but doesn't with @ObjectFactory)
	 * - Optional fields: description, start, end (MapStruct cannot map Optional<T> to T)
	 * 
	 * Fields set in constructor (via @ObjectFactory): id, version, name
	 */
	default void mapMutableFields(@NonNull TaskJPA source, @NonNull TaskDTO target)
	{
		target.closed(source.closed());
		target.description(source.description().orElse(null));
		target.start(source.start().orElse(null));
		target.end(source.end().orElse(null));
	}

	/** mapstruct object factory */
	@ObjectFactory @NonNull default TaskDTO create(@NonNull TaskJPA in, @NonNull @Context ReferenceCycleTracking context)
	{
		TaskGroupDTO group = context.get(in.taskGroup(), TaskGroupDTO.class);

		if (isNull(group))
		{
			group = new TaskGroupDTO(in.taskGroup());
			context.put(in.taskGroup(), group);
		}

		TaskDTO result = new TaskDTO(group, in);

		return result;
	}
}