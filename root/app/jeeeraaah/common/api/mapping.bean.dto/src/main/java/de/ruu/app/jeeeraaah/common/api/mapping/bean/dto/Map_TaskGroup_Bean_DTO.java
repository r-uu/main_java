package de.ruu.app.jeeeraaah.common.api.mapping.bean.dto;

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

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTO;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import lombok.NonNull;

/** {@link TaskGroupBean} -> {@link TaskGroupDTO} */
@Mapper
public interface Map_TaskGroup_Bean_DTO
{
	Map_TaskGroup_Bean_DTO INSTANCE = Mappers.getMapper(Map_TaskGroup_Bean_DTO.class);

	@Mapping(target = "name", ignore = true) // set in constructor
	@Mapping(target = "tasks", ignore = true) // mapped in afterMapping
	@Mapping(target = "description", ignore = true) // mapped in afterMapping
	@NonNull TaskGroupDTO map(@NonNull TaskGroupBean in, @NonNull @Context ReferenceCycleTracking context);

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@BeforeMapping default void beforeMapping(
			@NonNull                TaskGroupBean     in,
			@NonNull @MappingTarget TaskGroupDTO     out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		// required arguments id, version and name are already set in constructor
	}

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@AfterMapping default void afterMapping(
			@NonNull                TaskGroupBean     in,
			@NonNull @MappingTarget TaskGroupDTO     out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		// Map mutable fields
		mapMutableFields(in, out);
		
		if (in.tasks().isPresent())
		{
			Set<TaskBean> relatedTasks = in.tasks().get();
			for (TaskBean relatedTask : relatedTasks)
			{
				// check if related task was already mapped
				TaskDTO relatedTaskMapped = context.get(relatedTask, TaskDTO.class);
				if (isNull(relatedTaskMapped))
						// start new mapping for related task
						Map_Task_Bean_DTO.INSTANCE.map(relatedTask, context);
			}
		}
	}

	/** mapstruct object factory */
	@ObjectFactory default @NonNull TaskGroupDTO create(@NonNull TaskGroupBean in) { return new TaskGroupDTO(in); }
	
	/**
	 * Helper method to map mutable fields that MapStruct cannot auto-map.
	 * 
	 * Note: With @ObjectFactory, MapStruct skips automatic field mapping.
	 * We must manually map:
	 * - closed: Boolean field
	 * - Optional fields: description
	 * 
	 * Fields set in constructor (via @ObjectFactory): id, version, name
	 * Relations are mapped in afterMapping main body.
	 */
	default void mapMutableFields(TaskGroupBean source, TaskGroupDTO target) {
		target.description(source.description().orElse(null));
	}
}