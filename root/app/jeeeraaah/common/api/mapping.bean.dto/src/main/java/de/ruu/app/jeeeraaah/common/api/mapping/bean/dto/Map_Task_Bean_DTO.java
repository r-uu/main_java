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
import de.ruu.app.jeeeraaah.common.api.mapping.Mappings;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTO;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import lombok.NonNull;

/** {@link TaskBean} -> {@link TaskDTO} */
@Mapper
public interface Map_Task_Bean_DTO
{
	Map_Task_Bean_DTO INSTANCE = Mappers.getMapper(Map_Task_Bean_DTO.class);

	@Mapping(target = "taskGroup", ignore = true) // ignore task group, because it is mapped in object factory
	@Mapping(target = "superTask", ignore = true) // ignore superTask as it's not needed in DTO
	@Mapping(target = "subTasks", ignore = true) // ignore subTasks as it's not needed in DTO
	@Mapping(target = "predecessors", ignore = true) // ignore predecessors as it's not needed in DTO
	@Mapping(target = "successors", ignore = true) // ignore successors as it's not needed in DTO
	@Mapping(target = "description", ignore = true) // mapped in afterMapping
	@Mapping(target = "start", ignore = true) // mapped in afterMapping
	@Mapping(target = "end", ignore = true) // mapped in afterMapping
	@NonNull
	TaskDTO map(@NonNull TaskBean in, @NonNull @Context ReferenceCycleTracking context);

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@BeforeMapping default void beforeMapping(
			@NonNull                TaskBean                in,
			@NonNull @MappingTarget TaskDTO                out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		// required arguments id, version and name are already set in constructor
	}

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@AfterMapping default void afterMapping(
			@NonNull                TaskBean                in,
			@NonNull @MappingTarget TaskDTO                out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		// Map mutable fields
		mapMutableFields(in, out);
		
		if (in.superTask().isPresent())
		{
			TaskBean relatedTask       = in.superTask().get();
			// check if related task was already mapped
			TaskDTO relatedTaskMapped = context.get(relatedTask, TaskDTO.class);
			if (isNull(relatedTaskMapped))
					// start new mapping for related task
					out.superTask(Mappings.toDTO(relatedTask, context));
			else
					// use already mapped related task
					out.superTask(relatedTaskMapped);
		}
		if (in.subTasks().isPresent())
		{
			Set<TaskBean> relatedTasks = in.subTasks().get();
			for (TaskBean relatedTask : relatedTasks)
			{
				// check if related task was already mapped
				TaskDTO relatedTaskMapped = context.get(relatedTask, TaskDTO.class);
				if (isNull(relatedTaskMapped))
				{
						// start new mapping for related task
						out.addSubTask(Mappings.toDTO(relatedTask, context));
				}
				else
						// use already mapped related task
						out.addSubTask(relatedTaskMapped);
			}
		}
		if (in.predecessors().isPresent())
		{
			Set<TaskBean> relatedTasks = in.predecessors().get();
			for (TaskBean relatedTask : relatedTasks)
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
		if (in.successors().isPresent())
		{
			Set<TaskBean> relatedTasks = in.successors().get();
			for (TaskBean relatedTask : relatedTasks)
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

	/**
	 * mapstruct object factory
	 */
	@ObjectFactory @NonNull default TaskDTO create(
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
	
	/** 
	 * Helper method to map mutable fields from TaskBean to TaskDTO.
	 * 
	 * Note: With @ObjectFactory, MapStruct skips automatic field mapping.
	 * Fields set in constructor (via @ObjectFactory): id, version, name, closed, taskGroup
	 */
	default void mapMutableFields(TaskBean source, TaskDTO target) {
		target.description(source.description().orElse(null));
		target.start(source.start().orElse(null));
		target.end(source.end().orElse(null));
	}
}