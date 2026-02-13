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
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import lombok.NonNull;

/** {@link TaskDTO} -> {@link TaskBean} */
@Mapper
public interface Map_Task_DTO_Bean
{
	Map_Task_DTO_Bean INSTANCE = Mappers.getMapper(Map_Task_DTO_Bean.class);

	@Mapping(target = "name", ignore = true) // set in constructor
	@Mapping(target = "superTask", ignore = true) // mapped in afterMapping
	@Mapping(target = "closed", ignore = true) // mapped in afterMapping
	@Mapping(target = "taskGroup", ignore = true) // mapped in object factory
	@Mapping(target = "preconditionCheckRelationalOperations", ignore = true) // not needed in Bean
	@Mapping(target = "description", ignore = true) // Optional field, mapped in afterMapping via helper
	@Mapping(target = "start", ignore = true) // Optional field, mapped in afterMapping via helper
	@Mapping(target = "end", ignore = true) // Optional field, mapped in afterMapping via helper
	// Relations are handled in afterMapping, but we don't ignore them because they don't have setters anyway
	@NonNull TaskBean map(@NonNull TaskDTO in, @NonNull @Context ReferenceCycleTracking context);

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@BeforeMapping default void beforeMapping(
			@NonNull                TaskDTO in,
			@NonNull @MappingTarget TaskBean out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		// required arguments id, version and name are already set in constructor
	}

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@AfterMapping default void afterMapping(
			@NonNull                TaskDTO     in,
			@NonNull @MappingTarget TaskBean     out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		// Map mutable fields
		mapMutableFields(in, out);
		
		// Map complex relationship fields (circular references)
		if (in.superTask().isPresent())
		{
			TaskDTO relatedTask       = in.superTask().get();
			// check if related task was already mapped
			TaskBean relatedTaskMapped = context.get(relatedTask, TaskBean.class);
			if (isNull(relatedTaskMapped))
					// start new mapping for related task
					out.superTask(Mappings.toBean(relatedTask, context));
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
				TaskBean relatedTaskMapped = context.get(relatedTask, TaskBean.class);
				if (isNull(relatedTaskMapped))
						// start new mapping for related task
						out.addSubTask(Mappings.toBean(relatedTask, context));
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
				TaskBean relatedTaskMapped = context.get(relatedTask, TaskBean.class);
				if (isNull(relatedTaskMapped))
						// start new mapping for related task
						out.addPredecessor(Mappings.toBean(relatedTask, context));
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
				TaskBean relatedTaskMapped = context.get(relatedTask, TaskBean.class);
				if (isNull(relatedTaskMapped))
						// start new mapping for related task
						out.addSuccessor(Mappings.toBean(relatedTask, context));
				else
						// use already mapped related task
						out.addSuccessor(relatedTaskMapped);
			}
		}

		// simple fields are handled by MapStruct via lombok-mapstruct-binding; no explicit copy here
	}

	/** mapstruct object factory */
	@ObjectFactory default @NonNull TaskBean create(
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
	
	/** 
	 * Helper method to map simple mutable fields.
	 * Note: With @ObjectFactory, MapStruct does NOT auto-map any fields.
	 * We must map all mutable fields manually.
	 * Read-only fields (id, version, name) are set in constructor via @ObjectFactory.
	 * Relations are mapped in afterMapping main body.
	 */
	default void mapMutableFields(TaskDTO source, TaskBean target) {
		// Simple non-Optional field
		target.setClosed(source.closed());
		
		// Optional fields
		target.setDescription(source.description().orElse(null));
		target.setStart(source.start().orElse(null));
		target.setEnd(source.end().orElse(null));
	}
}