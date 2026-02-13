package de.ruu.app.jeeeraaah.frontend.common.mapping.bean.fxbean;

import static de.ruu.app.jeeeraaah.frontend.common.mapping.Mappings.toFXBean;
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
import de.ruu.app.jeeeraaah.frontend.ui.fx.model.TaskFXBean;
import de.ruu.app.jeeeraaah.frontend.ui.fx.model.TaskGroupFXBean;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import lombok.NonNull;

/** {@link TaskBean} -> {@link TaskFXBean} */
@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR) public interface Map_Task_Bean_FXBean
{
	Map_Task_Bean_FXBean INSTANCE = Mappers.getMapper(Map_Task_Bean_FXBean.class);

	// Ignore fields that are handled in @ObjectFactory or @AfterMapping
	@Mapping(target = "taskGroup", ignore = true) // mapped in @ObjectFactory
	@Mapping(target = "superTask", ignore = true) // handled in @AfterMapping
	de.ruu.app.jeeeraaah.frontend.ui.fx.model.TaskFXBean map(@NonNull TaskBean in, @NonNull @Context ReferenceCycleTracking context);

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@BeforeMapping default void beforeMapping(
			@NonNull                TaskBean               in,
			@NonNull @MappingTarget TaskFXBean out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		// required arguments id, version and name are already set in constructor
	}

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@AfterMapping default void afterMapping(
			@NonNull                TaskBean               in,
			@NonNull @MappingTarget TaskFXBean             out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		if (in.superTask().isPresent())
		{
			TaskBean   relatedTask       = in.superTask().get();
			// check if related task was already mapped
			TaskFXBean relatedTaskMapped = context.get(relatedTask, TaskFXBean.class);
			if (isNull(relatedTaskMapped))
					// start new mapping for related task
					out.superTask(toFXBean(relatedTask, context));
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
				TaskFXBean relatedTaskMapped = context.get(relatedTask, TaskFXBean.class);
				if (isNull(relatedTaskMapped))
				{
						// start new mapping for related task
						out.addSubTask(toFXBean(relatedTask, context));
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
				TaskFXBean relatedTaskMapped = context.get(relatedTask, TaskFXBean.class);
				if (isNull(relatedTaskMapped))
						// start new mapping for related task
						out.addPredecessor(toFXBean(relatedTask, context));
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
				TaskFXBean relatedTaskMapped = context.get(relatedTask, TaskFXBean.class);
				if (isNull(relatedTaskMapped))
						// start new mapping for related task
						out.addSuccessor(toFXBean(relatedTask, context));
				else
						// use already mapped related task
						out.addSuccessor(relatedTaskMapped);
			}
		}
	}

	/** mapstruct object factory */
	@ObjectFactory @NonNull default TaskFXBean create(@NonNull TaskBean in, @NonNull @Context ReferenceCycleTracking context)
	{
		TaskGroupFXBean group = context.get(in.taskGroup(), TaskGroupFXBean.class);

		if (isNull(group))
		{
			group = new TaskGroupFXBean(in.taskGroup());
			context.put(in.taskGroup(), group);
		}

		return new TaskFXBean(group, in);
	}
}