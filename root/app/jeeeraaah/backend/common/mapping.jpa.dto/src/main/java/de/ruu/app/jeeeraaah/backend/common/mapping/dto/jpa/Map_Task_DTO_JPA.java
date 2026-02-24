package de.ruu.app.jeeeraaah.backend.common.mapping.dto.jpa;

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

import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskGroupJPA;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskJPA;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import lombok.NonNull;

/** {@link TaskDTO} -> {@link TaskJPA}
 *
 * NOTE (EN): This mapper expects the project to use the lombok-mapstruct-binding so that
 * MapStruct recognizes Lombok's fluent accessors. Without the binding MapStruct might fall back
 * to bean-style accessors and some fields might not be mapped automatically.
 */
@Mapper public interface Map_Task_DTO_JPA
{
	Map_Task_DTO_JPA INSTANCE = Mappers.getMapper(Map_Task_DTO_JPA.class);

	@Mapping(target = "name"       , ignore = true) // set in constructor
	@Mapping(target = "superTask"  , ignore = true) // mapped in afterMapping
	@Mapping(target = "closed"     , ignore = true) // mapped in afterMapping
	@Mapping(target = "taskGroup"  , ignore = true) // ignore task group, because it is mapped in object factory
	@Mapping(target = "description", ignore = true) // mapped in afterMapping
	@Mapping(target = "start"      , ignore = true) // mapped in afterMapping
	@Mapping(target = "end"        , ignore = true) // mapped in afterMapping
	@NonNull TaskJPA map(@NonNull TaskDTO in, @NonNull @Context ReferenceCycleTracking context);

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@BeforeMapping default void beforeMapping
			(
					@NonNull                TaskDTO in,
					@NonNull @MappingTarget TaskJPA out,
					@NonNull @Context       ReferenceCycleTracking context
			)
	{
		// required arguments id, version and name are already set in constructor
	}

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@AfterMapping default void afterMapping
			(
					@NonNull                TaskDTO in,
					@NonNull @MappingTarget TaskJPA out,
					@NonNull @Context       ReferenceCycleTracking context
			)
	{
		if (in.superTask().isPresent())
		{
			TaskDTO relatedTask       = in.superTask().get();
			// check if related task was already mapped
			TaskJPA relatedTaskMapped = context.get(relatedTask, TaskJPA.class);
			if (isNull(relatedTaskMapped))
					// start new mapping for related task
					out.superTask(INSTANCE.map(relatedTask, context));
			else
					// use already mapped related task
					out.superTask(relatedTaskMapped);
		}
		if (in.subTasks().isPresent())
		{
			Set<TaskDTO> relatedTasks = in.subTasks().get();
			for (TaskDTO relatedTask : relatedTasks)
			{
				// check if related task was already mapped
				TaskJPA relatedTaskMapped = context.get(relatedTask, TaskJPA.class);
				if (isNull(relatedTaskMapped))
						// start new mapping for related task
						out.addSubTask(INSTANCE.map(relatedTask, context));
				else
						// use already mapped related task
						out.addSubTask(relatedTaskMapped);
			}
		}
		if (in.predecessors().isPresent())
		{
			Set<TaskDTO> relatedTasks = in.predecessors().get();
			for (TaskDTO relatedTask : relatedTasks)
			{
				// check if related task was already mapped
				TaskJPA relatedTaskMapped = context.get(relatedTask, TaskJPA.class);
				if (isNull(relatedTaskMapped))
						// start new mapping for related task
						out.addPredecessor(INSTANCE.map(relatedTask, context));
				else
						// use already mapped related task
						out.addPredecessor(relatedTaskMapped);
			}
		}
		if (in.successors().isPresent())
		{
			Set<TaskDTO> relatedTasks = in.successors().get();
			for (TaskDTO relatedTask : relatedTasks)
			{
				// check if related task was already mapped
				TaskJPA relatedTaskMapped = context.get(relatedTask, TaskJPA.class);
				if (isNull(relatedTaskMapped))
						// start new mapping for related task
						out.addSuccessor(INSTANCE.map(relatedTask, context));
				else
						// use already mapped related task
						out.addSuccessor(relatedTaskMapped);
			}
		}
		// Map mutable fields
		mapMutableFields(in, out);
	}

	/**
	 * Helper method to map mutable fields that MapStruct cannot auto-map.
	 * 
	 * Note: With @ObjectFactory, MapStruct skips automatic field mapping.
	 * We must manually map Optional fields: description, start, end
	 * (MapStruct cannot map Optional<T> to T automatically)
	 * 
	 * Fields set in constructor (via @ObjectFactory): id, version, name, closed, taskGroup
	 */
	default void mapMutableFields(@NonNull TaskDTO source, @NonNull TaskJPA target)
	{
		target.description(source.description().orElse(null));
		target.start(source.start().orElse(null));
		target.end(source.end().orElse(null));
	}

	/**
	 * mapstruct object factory
	 * <p>
	 * Uses the TaskJPA constructor that accepts TaskEntity to preserve id and version from the DTO.
	 */
	@ObjectFactory default @NonNull TaskJPA create(@NonNull TaskDTO in, @NonNull @Context ReferenceCycleTracking context)
	{
		TaskGroupJPA group = context.get(in.taskGroup(), TaskGroupJPA.class);

		if (isNull(group))
		{
			group = new TaskGroupJPA(in.taskGroup());
			context.put(in.taskGroup(), group);
		}

		// Use constructor that preserves id, version, closed from DTO
		TaskJPA result = new TaskJPA(group, in);

		return result;
	}
}