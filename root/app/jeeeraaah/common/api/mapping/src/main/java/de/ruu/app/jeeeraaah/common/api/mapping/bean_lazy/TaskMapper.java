package de.ruu.app.jeeeraaah.common.api.mapping.bean_lazy;

import static de.ruu.app.jeeeraaah.common.api.mapping.Mappings.toLazy;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import org.mapstruct.factory.Mappers;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.domain.TaskLazy;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTOLazy;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import lombok.NonNull;

/**
 * Bidirectional mapper for Task: Bean ↔ Lazy
 * <p>
 * Combines mappings in both directions:
 * <ul>
 *   <li>Bean → Lazy: {@link #toLazy(TaskBean)}</li>
 *   <li>Lazy → Bean: {@link #toBean(TaskGroupBean, TaskLazy)}</li>
 * </ul>
 * <p>
 * Lazy representations are used for performance optimization, loading minimal data without relationships.
 */
@Mapper
public interface TaskMapper
{
	TaskMapper INSTANCE = Mappers.getMapper(TaskMapper.class);

	// ========================================
	// Bean → Lazy
	// ========================================

	@Mapping(target = "description", ignore = true) // mapped in afterMapping
	@Mapping(target = "start", ignore = true)
	@Mapping(target = "end", ignore = true)
	@NonNull
	TaskLazy toLazy(@NonNull TaskBean in);

	@BeforeMapping
	default void beforeBeanToLazy(
			@NonNull                TaskBean in,
			@NonNull @MappingTarget TaskLazy out)
	{
		// Required arguments are set in constructor
	}

	@AfterMapping
	default void afterBeanToLazy(
			@NonNull                TaskBean in,
			@NonNull @MappingTarget TaskLazy out)
	{
		// Map Optional<T> fields to T fields
		out.description(in.description().orElse(null));
		out.start(in.start().orElse(null));
		out.end(in.end().orElse(null));
	}

	@ObjectFactory default @NonNull TaskLazy createLazy(@NonNull TaskBean in)
	{
		return new TaskDTOLazy(TaskGroupMapper.INSTANCE.toLazy(in.taskGroup()), in);
	}

	// ========================================
	// Lazy → Bean
	// ========================================

	/**
	 * @param group necessary to propagate task group id to task bean, must match type in create method exactly
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
	@NonNull
	TaskBean toBean(@NonNull TaskGroupBean group, @NonNull TaskLazy in);

	@BeforeMapping
	default void beforeLazyToBean(
			@NonNull                TaskLazy in,
			@NonNull @MappingTarget TaskBean out)
	{
		// Required arguments are set in constructor
	}

	@AfterMapping
	default void afterLazyToBean(
			@NonNull                TaskLazy in,
			@NonNull @MappingTarget TaskBean out)
	{
		// No manual mappings in addition to those done by mapstruct
	}

	@ObjectFactory default @NonNull TaskBean createBean(@NonNull TaskGroupBean group, @NonNull TaskLazy in)
	{
		return new TaskBean(group, in.name(), in);
	}
}

