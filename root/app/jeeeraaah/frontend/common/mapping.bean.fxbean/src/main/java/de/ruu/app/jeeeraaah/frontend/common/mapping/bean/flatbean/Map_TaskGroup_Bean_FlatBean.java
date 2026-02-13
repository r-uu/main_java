package de.ruu.app.jeeeraaah.frontend.common.mapping.bean.flatbean;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.domain.TaskEntity;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupEntity;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupFlat;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupFlat.TaskGroupFlatSimple;
import de.ruu.app.jeeeraaah.frontend.common.mapping.bean.fxbean.Map_Task_Bean_FXBean;
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
@Mapper public interface Map_TaskGroup_Bean_FlatBean
{
	Map_TaskGroup_Bean_FlatBean INSTANCE = Mappers.getMapper(Map_TaskGroup_Bean_FlatBean.class);

	// name and description are set in the @ObjectFactory method, not by MapStruct field mapping
	@Mapping(target = "name", ignore = true)
	@Mapping(target = "description", ignore = true)
	TaskGroupFlat map(@NonNull TaskGroupBean in, @NonNull @Context ReferenceCycleTracking context);

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@BeforeMapping default void beforeMapping(
			@NonNull                TaskGroupEntity<? extends TaskEntity<?, ?>> in,
			@NonNull @MappingTarget TaskGroupFlat                               out,
			@NonNull @Context       ReferenceCycleTracking                      context)
	{
		// required arguments id, version and name are already set in constructor
	}

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@AfterMapping default void afterMapping(
			@NonNull                TaskGroupEntity<? extends TaskEntity<?, ?>> in,
			@NonNull @MappingTarget TaskGroupFlat                               out,
			@NonNull @Context       ReferenceCycleTracking                      context)
	{
	}

	/** mapstruct object factory */
	@ObjectFactory
	default @NonNull TaskGroupFlat create(@NonNull TaskGroupEntity<? extends TaskEntity<?, ?>> in)
	{
		return new TaskGroupFlatSimple(in);
	}
}
