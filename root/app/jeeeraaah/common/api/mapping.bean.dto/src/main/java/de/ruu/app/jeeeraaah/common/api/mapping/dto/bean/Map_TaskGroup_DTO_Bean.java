package de.ruu.app.jeeeraaah.common.api.mapping.dto.bean;

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
import de.ruu.app.jeeeraaah.common.api.mapping.Mappings;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTO;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import lombok.NonNull;

/** {@link TaskGroupDTO} -> {@link TaskGroupBean} */
@Mapper
public interface Map_TaskGroup_DTO_Bean
{
	Map_TaskGroup_DTO_Bean INSTANCE = Mappers.getMapper(Map_TaskGroup_DTO_Bean.class);

	@Mapping(target = "description", ignore = true) // mapped in afterMapping
	@NonNull TaskGroupBean map(@NonNull TaskGroupDTO in, @NonNull @Context ReferenceCycleTracking context);

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@BeforeMapping default void beforeMapping(
			@NonNull                TaskGroupDTO           in,
			@NonNull @MappingTarget TaskGroupBean          out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		// required arguments are set in constructor
	}

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@AfterMapping default void afterMapping(
			@NonNull                TaskGroupDTO           in,
			@NonNull @MappingTarget TaskGroupBean          out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		// Map mutable fields
		mapMutableFields(in, out);
		
		if (in.tasks().isPresent())
		{
			Set<TaskDTO> relatedTasks = in.tasks().get();
			for (TaskDTO relatedTask : relatedTasks)
			{
				// check if related task was already mapped
				TaskBean relatedTaskMapped = context.get(relatedTask, TaskBean.class);
				if (isNull(relatedTaskMapped))
						// start new mapping for related task, task will be added to this task group during mapping
						Mappings.toBean(relatedTask, context);
			}
		}
	}

	/** mapstruct object factory */
	@ObjectFactory default @NonNull TaskGroupBean create(@NonNull TaskGroupDTO in) {
		return new TaskGroupBean((de.ruu.app.jeeeraaah.common.api.domain.TaskGroupEntity<? extends de.ruu.app.jeeeraaah.common.api.domain.TaskEntity<?, ?>>) in);
	}
	
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
	default void mapMutableFields(TaskGroupDTO source, TaskGroupBean target) {
		target.description(source.description().orElse(null));
	}
}