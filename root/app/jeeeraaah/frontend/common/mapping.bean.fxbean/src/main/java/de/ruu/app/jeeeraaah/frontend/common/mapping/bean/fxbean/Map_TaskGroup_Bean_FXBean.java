package de.ruu.app.jeeeraaah.frontend.common.mapping.bean.fxbean;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
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

/** {@link TaskGroupBean} -> {@link TaskGroupFXBean} */
@Mapper public interface Map_TaskGroup_Bean_FXBean
{
	Map_TaskGroup_Bean_FXBean INSTANCE = Mappers.getMapper(Map_TaskGroup_Bean_FXBean.class);

	// name and description are set in the @ObjectFactory method, not by MapStruct field mapping
	@Mapping(target = "name", ignore = true)
	@Mapping(target = "description", ignore = true)
	de.ruu.app.jeeeraaah.frontend.ui.fx.model.TaskGroupFXBean map(@NonNull TaskGroupBean in, @NonNull @Context ReferenceCycleTracking context);

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@BeforeMapping default void beforeMapping(
			@NonNull                TaskGroupBean          in,
			@NonNull @MappingTarget TaskGroupFXBean        out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		// required arguments id, version and name are already set in constructor
	}

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@AfterMapping default void afterMapping(
			@NonNull                TaskGroupBean          in,
			@NonNull @MappingTarget TaskGroupFXBean        out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		if (in.tasks().isPresent())
		{
			Set<TaskBean> relatedTasks = in.tasks().get();
			for (TaskBean relatedTask : relatedTasks)
			{
				// check if related task was already mapped
				TaskFXBean relatedTaskMapped = context.get(relatedTask, TaskFXBean.class);
				if (isNull(relatedTaskMapped))
						// start new mapping for related task
						Map_Task_Bean_FXBean.INSTANCE.map(relatedTask, context);
			}
		}
	}

	/** mapstruct object factory */
	@ObjectFactory default @NonNull TaskGroupFXBean create(@NonNull TaskGroupBean in) { return new TaskGroupFXBean(in); }
}