package de.ruu.app.jeeeraaah.common.api.mapping.lazy.bean;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.domain.TaskLazy;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTOLazy;
import lombok.NonNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import org.mapstruct.factory.Mappers;

/** {@link TaskDTOLazy} -> {@link TaskBean} */
@Mapper public interface Map_Task_Lazy_Bean
{
	Map_Task_Lazy_Bean INSTANCE = Mappers.getMapper(Map_Task_Lazy_Bean.class);

	/**
	 * @param group necessary to propagate task group id to task bean in {@link #create(TaskGroupBean, TaskLazy)},
	 *              the type of {@code group} must match the type of parameter {@code group] in {@link
	 *              #create(TaskGroupBean, TaskLazy)} exactly
	 * @param in    the lazy task to be mapped
	 * @return      the mapped task bean
	 */
	@Mapping(target = "taskGroup", source = "group")
	@Mapping(target = "name", ignore = true)
	@Mapping(target = "description", ignore = true)
	@Mapping(target = "superTask", ignore = true)
	@Mapping(target = "start", ignore = true)
	@Mapping(target = "end", ignore = true)
	@Mapping(target = "closed", ignore = true)
	@Mapping(target = "preconditionCheckRelationalOperations", ignore = true)
	@NonNull TaskBean map(@NonNull TaskGroupBean group, @NonNull TaskLazy in);

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@BeforeMapping
	default void beforeMapping(
			@NonNull                TaskLazy in,
			@NonNull @MappingTarget TaskBean out)
	{
		// required arguments are set in constructor
	}

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@AfterMapping
	default void afterMapping(
			@NonNull                TaskLazy in,
			@NonNull @MappingTarget TaskBean out)
	{
		// no manual mappings in addition to those done by mapstruct
	}

	/** mapstruct object factory */
	@ObjectFactory default @NonNull TaskBean create(@NonNull TaskGroupBean group, @NonNull TaskLazy in)
	{
		return new TaskBean(group, in.name(), in);
	}
}