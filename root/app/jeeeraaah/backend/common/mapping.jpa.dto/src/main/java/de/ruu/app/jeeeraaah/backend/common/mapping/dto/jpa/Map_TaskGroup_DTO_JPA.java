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
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTO;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import lombok.NonNull;

/** {@link TaskGroupDTO} -> {@link TaskGroupJPA} */
@Mapper public interface Map_TaskGroup_DTO_JPA
{
	Map_TaskGroup_DTO_JPA INSTANCE = Mappers.getMapper(Map_TaskGroup_DTO_JPA.class);

	@Mapping(target = "tasks", ignore = true)
	@Mapping(target = "description" , ignore = true) // mapped in afterMapping
	@NonNull TaskGroupJPA map(@NonNull TaskGroupDTO in, @NonNull @Context ReferenceCycleTracking context);

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@BeforeMapping default void beforeMapping
			(
					@NonNull                TaskGroupDTO     in,
					@NonNull @MappingTarget TaskGroupJPA     out,
					@NonNull @Context       ReferenceCycleTracking context
			)
	{
		// required arguments are set in constructor
	}

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@AfterMapping default void afterMapping
			(
					@NonNull                TaskGroupDTO     in,
					@NonNull @MappingTarget TaskGroupJPA     out,
					@NonNull @Context       ReferenceCycleTracking context
			)
	{
		if (in.tasks().isPresent())
		{
			Set<TaskDTO> relatedTasks = in.tasks().get();
			for (TaskDTO relatedTask : relatedTasks)
			{
				// check if related task was already mapped
				TaskJPA relatedTaskMapped = context.get(relatedTask, TaskJPA.class);
				if (isNull(relatedTaskMapped))
						// start new mapping for related task, task will be added to this task group during mapping
						Map_Task_DTO_JPA.INSTANCE.map(relatedTask, context);
			}
		}
		// Map mutable fields
		mapMutableFields(in, out);
	}

	/**
	 * Helper method to map mutable fields that MapStruct cannot auto-map.
	 * 
	 * Note: With @ObjectFactory, MapStruct skips automatic field mapping.
	 * We must manually map Optional fields: description
	 * (MapStruct cannot map Optional<T> to T automatically)
	 * 
	 * Fields set in constructor (via @ObjectFactory): id, version, name, closed
	 */
	default void mapMutableFields(@NonNull TaskGroupDTO source, @NonNull TaskGroupJPA target)
	{
		target.description(source.description().orElse(null));
	}

	/** mapstruct object factory */
	@ObjectFactory default @NonNull TaskGroupJPA create(@NonNull TaskGroupDTO in) { return new TaskGroupJPA(in); }
}