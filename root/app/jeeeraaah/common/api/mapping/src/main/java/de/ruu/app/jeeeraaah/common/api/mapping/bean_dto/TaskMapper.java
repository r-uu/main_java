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
 * Bidirectional mapper for Task: Bean ↔ DTO
 * <p>
 * Combines mappings in both directions:
 * <ul>
 *   <li>Bean → DTO: {@link #toDTO(TaskBean, ReferenceCycleTracking)}</li>
 *   <li>DTO → Bean: {@link #toBean(TaskDTO, ReferenceCycleTracking)}</li>
 * </ul>
 */
@Mapper
public interface TaskMapper
{
	TaskMapper INSTANCE = Mappers.getMapper(TaskMapper.class);

	// ========================================
	// Bean → DTO
	// ========================================

	@Mapping(target = "taskGroup", ignore = true) // mapped in object factory
	@Mapping(target = "superTask", ignore = true) // mapped in afterMapping
	@Mapping(target = "subTasks", ignore = true)
	@Mapping(target = "predecessors", ignore = true)
	@Mapping(target = "successors", ignore = true)
	@Mapping(target = "description", ignore = true) // mapped in afterMapping
	@Mapping(target = "start", ignore = true)
	@Mapping(target = "end", ignore = true)
	@NonNull
	TaskDTO toDTO(@NonNull TaskBean in, @NonNull @Context ReferenceCycleTracking context);

	@BeforeMapping default void beforeBeanToDTO(
			@NonNull                TaskBean                in,
			@NonNull @MappingTarget TaskDTO                 out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		// Required arguments id, version and name are already set in constructor
	}

	@AfterMapping default void afterBeanToDTO(
			@NonNull                TaskBean                in,
			@NonNull @MappingTarget TaskDTO                 out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		// Map mutable fields
		out.description(in.description().orElse(null));
		out.start(in.start().orElse(null));
		out.end(in.end().orElse(null));

		// Map relationships with cycle detection
		if (in.superTask().isPresent())
		{
			TaskBean relatedTask = in.superTask().get();
			TaskDTO relatedTaskMapped = context.get(relatedTask, TaskDTO.class);
			if (isNull(relatedTaskMapped))
					out.superTask(Mappings.toDTO(relatedTask, context));
			else
					out.superTask(relatedTaskMapped);
		}

		if (in.subTasks().isPresent())
		{
			Set<TaskBean> relatedTasks = in.subTasks().get();
			for (TaskBean relatedTask : relatedTasks)
			{
				TaskDTO relatedTaskMapped = context.get(relatedTask, TaskDTO.class);
				if (isNull(relatedTaskMapped))
						out.addSubTask(Mappings.toDTO(relatedTask, context));
				else
						out.addSubTask(relatedTaskMapped);
			}
		}

		if (in.predecessors().isPresent())
		{
			Set<TaskBean> relatedTasks = in.predecessors().get();
			for (TaskBean relatedTask : relatedTasks)
			{
				TaskDTO relatedTaskMapped = context.get(relatedTask, TaskDTO.class);
				if (isNull(relatedTaskMapped))
						out.addPredecessor(Mappings.toDTO(relatedTask, context));
				else
						out.addPredecessor(relatedTaskMapped);
			}
		}

		if (in.successors().isPresent())
		{
			Set<TaskBean> relatedTasks = in.successors().get();
			for (TaskBean relatedTask : relatedTasks)
			{
				TaskDTO relatedTaskMapped = context.get(relatedTask, TaskDTO.class);
				if (isNull(relatedTaskMapped))
						out.addSuccessor(Mappings.toDTO(relatedTask, context));
				else
						out.addSuccessor(relatedTaskMapped);
			}
		}
	}

	@ObjectFactory @NonNull default TaskDTO createDTO(
			@NonNull          TaskBean                in,
			@NonNull @Context ReferenceCycleTracking context)
	{
		TaskGroupDTO group = context.get(in.taskGroup(), TaskGroupDTO.class);

		if (isNull(group))
		{
			group = new TaskGroupDTO(in.taskGroup());
			context.put(in.taskGroup(), group);
		}

		return new TaskDTO(group, in);
	}

	// ========================================
	// DTO → Bean
	// ========================================

	@Mapping(target = "name", ignore = true) // set in constructor
	@Mapping(target = "superTask", ignore = true) // mapped in afterMapping
	@Mapping(target = "closed", ignore = true) // mapped in afterMapping
	@Mapping(target = "taskGroup", ignore = true) // mapped in object factory
	@Mapping(target = "preconditionCheckRelationalOperations", ignore = true)
	@Mapping(target = "description", ignore = true) // mapped in afterMapping
	@Mapping(target = "start", ignore = true)
	@Mapping(target = "end", ignore = true)
	@NonNull
	TaskBean toBean(@NonNull TaskDTO in, @NonNull @Context ReferenceCycleTracking context);

	@BeforeMapping default void beforeDTOToBean(
			@NonNull                TaskDTO                in,
			@NonNull @MappingTarget TaskBean                out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		// Required arguments id, version and name are already set in constructor
	}

	@AfterMapping default void afterDTOToBean(
			@NonNull                TaskDTO                 in,
			@NonNull @MappingTarget TaskBean                out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		// Map mutable fields
		out.setClosed(in.closed());
		out.setDescription(in.description().orElse(null));
		out.setStart(in.start().orElse(null));
		out.setEnd(in.end().orElse(null));

		// Map relationships with cycle detection
		if (in.superTask().isPresent())
		{
			TaskDTO relatedTask = in.superTask().get();
			TaskBean relatedTaskMapped = context.get(relatedTask, TaskBean.class);
			if (isNull(relatedTaskMapped))
					out.superTask(Mappings.toBean(relatedTask, context));
			else
					out.superTask(relatedTaskMapped);
		}

		if (in.subTasks().isPresent())
		{
			Set<TaskDTO> relatedTasks = in.subTasks().get();
			for (TaskDTO relatedTask : relatedTasks)
			{
				TaskBean relatedTaskMapped = context.get(relatedTask, TaskBean.class);
				if (isNull(relatedTaskMapped))
						out.addSubTask(Mappings.toBean(relatedTask, context));
				else
						out.addSubTask(relatedTaskMapped);
			}
		}

		if (in.predecessors().isPresent())
		{
			Set<TaskDTO> relatedTasks = in.predecessors().get();
			for (TaskDTO relatedTask : relatedTasks)
			{
				TaskBean relatedTaskMapped = context.get(relatedTask, TaskBean.class);
				if (isNull(relatedTaskMapped))
						out.addPredecessor(Mappings.toBean(relatedTask, context));
				else
						out.addPredecessor(relatedTaskMapped);
			}
		}

		if (in.successors().isPresent())
		{
			Set<TaskDTO> relatedTasks = in.successors().get();
			for (TaskDTO relatedTask : relatedTasks)
			{
				TaskBean relatedTaskMapped = context.get(relatedTask, TaskBean.class);
				if (isNull(relatedTaskMapped))
						out.addSuccessor(Mappings.toBean(relatedTask, context));
				else
						out.addSuccessor(relatedTaskMapped);
			}
		}
	}

	@ObjectFactory default @NonNull TaskBean createBean(
			@NonNull          TaskDTO                in,
			@NonNull @Context ReferenceCycleTracking context)
	{
		TaskGroupBean group = context.get(in.taskGroup(), TaskGroupBean.class);

		if (isNull(group))
		{
			group = new TaskGroupBean((de.ruu.app.jeeeraaah.common.api.domain.TaskGroupEntity<? extends de.ruu.app.jeeeraaah.common.api.domain.TaskEntity<?, ?>>) in.taskGroup());
			context.put(in.taskGroup(), group);
		}

		return new TaskBean(group, (de.ruu.app.jeeeraaah.common.api.domain.TaskEntity<?, ?>) in);
	}
}

