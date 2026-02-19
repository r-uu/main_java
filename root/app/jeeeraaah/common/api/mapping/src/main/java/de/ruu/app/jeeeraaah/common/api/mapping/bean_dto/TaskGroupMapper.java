package de.ruu.app.jeeeraaah.common.api.mapping.bean_dto;

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

/**
 * Bidirectional mapper for TaskGroup: Bean ↔ DTO
 * <p>
 * Combines mappings in both directions:
 * <ul>
 *   <li>Bean → DTO: {@link #toDTO(TaskGroupBean, ReferenceCycleTracking)}</li>
 *   <li>DTO → Bean: {@link #toBean(TaskGroupDTO, ReferenceCycleTracking)}</li>
 * </ul>
 */
@Mapper
public interface TaskGroupMapper
{
	TaskGroupMapper INSTANCE = Mappers.getMapper(TaskGroupMapper.class);

	// ========================================
	// Bean → DTO
	// ========================================

	@Mapping(target = "name", ignore = true) // set in constructor
	@Mapping(target = "tasks", ignore = true) // mapped in afterMapping
	@Mapping(target = "description", ignore = true) // mapped in afterMapping
	@NonNull
	TaskGroupDTO toDTO(@NonNull TaskGroupBean in, @NonNull @Context ReferenceCycleTracking context);

	@BeforeMapping default void beforeBeanToDTO(
			@NonNull                TaskGroupBean          in,
			@NonNull @MappingTarget TaskGroupDTO           out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		// Required arguments id, version and name are already set in constructor
	}

	@AfterMapping default void afterBeanToDTO(
			@NonNull                TaskGroupBean          in,
			@NonNull @MappingTarget TaskGroupDTO           out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		// Map mutable fields
		out.description(in.description().orElse(null));

		// Map tasks relationship with cycle detection
		if (in.tasks().isPresent())
		{
			Set<TaskBean> relatedTasks = in.tasks().get();
			for (TaskBean relatedTask : relatedTasks)
			{
				TaskDTO relatedTaskMapped = context.get(relatedTask, TaskDTO.class);
				if (isNull(relatedTaskMapped))
						// Start new mapping for related task
						TaskMapper.INSTANCE.toDTO(relatedTask, context);
			}
		}
	}

	@ObjectFactory default @NonNull TaskGroupDTO createDTO(@NonNull TaskGroupBean in)
	{
		return new TaskGroupDTO(in);
	}

	// ========================================
	// DTO → Bean
	// ========================================

	@Mapping(target = "description", ignore = true) // mapped in afterMapping
	@NonNull
	TaskGroupBean toBean(@NonNull TaskGroupDTO in, @NonNull @Context ReferenceCycleTracking context);

	@BeforeMapping default void beforeDTOToBean(
			@NonNull                TaskGroupDTO           in,
			@NonNull @MappingTarget TaskGroupBean          out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		// Required arguments are set in constructor
	}

	@AfterMapping default void afterDTOToBean(
			@NonNull                TaskGroupDTO           in,
			@NonNull @MappingTarget TaskGroupBean          out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		// Map mutable fields
		out.description(in.description().orElse(null));

		// Map tasks relationship with cycle detection
		if (in.tasks().isPresent())
		{
			Set<TaskDTO> relatedTasks = in.tasks().get();
			for (TaskDTO relatedTask : relatedTasks)
			{
				TaskBean relatedTaskMapped = context.get(relatedTask, TaskBean.class);
				if (isNull(relatedTaskMapped))
						// Start new mapping for related task, task will be added to this task group during mapping
						Mappings.toBean(relatedTask, context);
			}
		}
	}

	@ObjectFactory default @NonNull TaskGroupBean createBean(@NonNull TaskGroupDTO in)
	{
		return new TaskGroupBean((de.ruu.app.jeeeraaah.common.api.domain.TaskGroupEntity<? extends de.ruu.app.jeeeraaah.common.api.domain.TaskEntity<?, ?>>) in);
	}
}

