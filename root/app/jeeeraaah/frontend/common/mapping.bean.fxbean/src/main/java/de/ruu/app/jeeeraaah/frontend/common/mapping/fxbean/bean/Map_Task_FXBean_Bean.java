package de.ruu.app.jeeeraaah.frontend.common.mapping.fxbean.bean;

import static de.ruu.app.jeeeraaah.frontend.common.mapping.Mappings.toBean;
import static java.util.Objects.isNull;

import java.util.Set;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.frontend.ui.fx.model.TaskFXBean;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import lombok.NonNull;

/** {@link TaskBean} -> {@link TaskBean} */
@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR) public interface Map_Task_FXBean_Bean
{
	Map_Task_FXBean_Bean INSTANCE = Mappers.getMapper(Map_Task_FXBean_Bean.class);

	// Ignore fields that are handled in @ObjectFactory or @AfterMapping
	@Mapping(target = "taskGroup", ignore = true) // mapped in @ObjectFactory
	@Mapping(target = "name", ignore = true)      // mapped in @ObjectFactory constructor
	@Mapping(target = "superTask", ignore = true) // handled in @AfterMapping
	@Mapping(target = "preconditionCheckRelationalOperations", ignore = true) // not yet implemented
	@NonNull TaskBean map(de.ruu.app.jeeeraaah.frontend.ui.fx.model.TaskFXBean in, @NonNull @Context ReferenceCycleTracking context);

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@BeforeMapping default void beforeMapping(
			@NonNull                TaskFXBean in,
			@NonNull @MappingTarget TaskBean   out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		// required arguments id, version and name are already set in constructor
	}

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@AfterMapping default void afterMapping(
			@NonNull                TaskFXBean   in,
			@NonNull @MappingTarget TaskBean     out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		if (in.superTask().isPresent())
		{
			TaskFXBean relatedTask     = in.superTask().get();
			// check if related task was already mapped
			TaskBean relatedTaskMapped = context.get(relatedTask, TaskBean.class);
			if (isNull(relatedTaskMapped))
					// start new mapping for related task
					out.superTask(toBean(relatedTask, context));
			else
					// use already mapped related task
					out.superTask(relatedTaskMapped);
		}
		if (in.subTasks().isPresent())
		{
			Set<TaskFXBean> relatedTasks = in.subTasks().get();
			for (TaskFXBean relatedTask : relatedTasks)
			{
				// check if related task was already mapped
				TaskBean relatedTaskMapped = context.get(relatedTask, TaskBean.class);
				if (isNull(relatedTaskMapped))
						// start new mapping for related task
						out.addSubTask(toBean(relatedTask, context));
				else
						// use already mapped related task
						out.addSubTask(relatedTaskMapped);
			}
		}
		if (in.predecessors().isPresent())
		{
			Set<TaskFXBean> relatedTasks = in.predecessors().get();
			for (TaskFXBean relatedTask : relatedTasks)
			{
				// check if related task was already mapped
				TaskBean relatedTaskMapped = context.get(relatedTask, TaskBean.class);
				if (isNull(relatedTaskMapped))
						// start new mapping for related task
						out.addPredecessor(toBean(relatedTask, context));
				else
						// use already mapped related task
						out.addPredecessor(relatedTaskMapped);
			}
		}
		if (in.successors().isPresent())
		{
			Set<TaskFXBean> relatedTasks = in.successors().get();
			for (TaskFXBean relatedTask : relatedTasks)
			{
				// check if related task was already mapped
				TaskBean relatedTaskMapped = context.get(relatedTask, TaskBean.class);
				if (isNull(relatedTaskMapped))
						// start new mapping for related task
						out.addSuccessor(toBean(relatedTask, context));
				else
						// use already mapped related task
						out.addSuccessor(relatedTaskMapped);
			}
		}
	}

	/** mapstruct object factory */
	@ObjectFactory default @NonNull TaskBean create(@NonNull TaskFXBean in, @NonNull @Context ReferenceCycleTracking context)
	{
		TaskGroupBean group = context.get(in.taskGroup(), TaskGroupBean.class);

		if (isNull(group))
		{
			group = new TaskGroupBean(in.taskGroup());
			context.put(in.taskGroup(), group);
		}

		return new TaskBean(group, in);
	}
}