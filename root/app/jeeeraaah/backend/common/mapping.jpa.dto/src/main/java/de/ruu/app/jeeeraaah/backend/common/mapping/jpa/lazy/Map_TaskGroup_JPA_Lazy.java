package de.ruu.app.jeeeraaah.backend.common.mapping.jpa.lazy;

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

import de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto.Map_Task_JPA_DTO;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskGroupJPA;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskJPA;
import de.ruu.app.jeeeraaah.common.api.domain.lazy.TaskGroupLazy;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTO;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTOLazy;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import lombok.NonNull;

/** {@link TaskGroupJPA} -> {@link TaskGroupDTO} */
@Mapper public interface Map_TaskGroup_JPA_Lazy
{
	Map_TaskGroup_JPA_Lazy INSTANCE = Mappers.getMapper(Map_TaskGroup_JPA_Lazy.class);

	@Mapping(target = "name", ignore = true) // set in constructor
	@Mapping(target = "description", ignore = true) // mapped in afterMapping
	@NonNull TaskGroupLazy map(@NonNull TaskGroupJPA in, @NonNull @Context ReferenceCycleTracking context);

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@BeforeMapping default void beforeMapping
			(
					@NonNull                TaskGroupJPA           in,
					@NonNull @MappingTarget TaskGroupLazy          out,
					@NonNull @Context       ReferenceCycleTracking context
			)
	{
		// required arguments id, version and name are already set in constructor
	}

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@AfterMapping default void afterMapping
			(
					@NonNull                TaskGroupJPA           in,
					@NonNull @MappingTarget TaskGroupLazy          out,
					@NonNull @Context       ReferenceCycleTracking context
			)
	{
		// rely on MapStruct (with lombok-mapstruct-binding) to map simple fields

		if (in.tasks().isPresent())
		{
			Set<TaskJPA> relatedTasks = in.tasks().get();
			for (TaskJPA relatedTask : relatedTasks)
			{
				// check if related task was already mapped
				TaskDTO relatedTaskMapped = context.get(relatedTask, TaskDTO.class);
				if (isNull(relatedTaskMapped))
						// start new mapping for related task
						Map_Task_JPA_DTO.INSTANCE.map(relatedTask, context);
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
	default void mapMutableFields(@NonNull TaskGroupJPA source, @NonNull TaskGroupLazy target)
	{
		target.description(source.description().orElse(null));
	}

	/** mapstruct object factory */
	@ObjectFactory default @NonNull TaskGroupLazy create(@NonNull TaskGroupJPA in) { return new TaskGroupDTOLazy(in); }
}
