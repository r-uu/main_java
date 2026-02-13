package de.ruu.app.jeeeraaah.frontend.common.mapping.fxbean.bean;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.frontend.common.mapping.Mappings;
import de.ruu.app.jeeeraaah.frontend.ui.fx.model.TaskFXBean;
import de.ruu.app.jeeeraaah.frontend.ui.fx.model.TaskGroupFXBean;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import lombok.NonNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import org.mapstruct.factory.Mappers;

import java.util.Set;

import static java.util.Objects.isNull;

/** {@link TaskGroupBean} -> {@link TaskGroupBean} */
@Mapper public interface Map_TaskGroup_FXBean_Bean
{
	Map_TaskGroup_FXBean_Bean INSTANCE = Mappers.getMapper(Map_TaskGroup_FXBean_Bean.class);

	@Mapping(target = "name", ignore = true)
	@Mapping(target = "description", ignore = true)
	@NonNull TaskGroupBean map(TaskGroupFXBean in, @NonNull @Context ReferenceCycleTracking context);

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@BeforeMapping default void beforeMapping(
			@NonNull TaskGroupFXBean in,
			@NonNull @MappingTarget TaskGroupBean          out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		// required arguments are set in constructor
	}

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@AfterMapping default void afterMapping(
			@NonNull                TaskGroupFXBean        in,
			@NonNull @MappingTarget TaskGroupBean          out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		if (in.tasks().isPresent())
		{
			Set<TaskFXBean> relatedTasks = in.tasks().get();
			for (TaskFXBean relatedTask : relatedTasks)
			{
				// check if related task was already mapped
				TaskBean relatedTaskMapped = context.get(relatedTask, TaskBean.class);
				if (isNull(relatedTaskMapped))
						// start new mapping for related task, task will be added to this task group during mapping
						// adding task to task group is done in Map_Task_FXBean_Bean (TODO: verify this)
						Mappings.toBean(relatedTask, context);
			}
		}
	}

	/** mapstruct object factory */
	@ObjectFactory default @NonNull TaskGroupBean create(@NonNull TaskGroupFXBean in) { return new TaskGroupBean(in); }
}
