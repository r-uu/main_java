package de.ruu.app.jeeeraaah.common.api.mapping.bean_lazy;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import org.mapstruct.factory.Mappers;

import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupLazy;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTOLazy;
import lombok.NonNull;

/**
 * Bidirectional mapper for TaskGroup: Bean ↔ Lazy
 * <p>
 * Combines mappings in both directions:
 * <ul>
 *   <li>Bean → Lazy: {@link #toLazy(TaskGroupBean)}</li>
 *   <li>Lazy → Bean: {@link #toBean(TaskGroupLazy)}</li>
 * </ul>
 * <p>
 * Lazy representations are used for performance optimization, loading minimal data without relationships.
 */
@Mapper(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface TaskGroupMapper
{
	TaskGroupMapper INSTANCE = Mappers.getMapper(TaskGroupMapper.class);

	// ========================================
	// Bean → Lazy
	// ========================================

	@Mapping(target = "name", ignore = true)
	@Mapping(target = "description", ignore = true)
	@NonNull
	TaskGroupLazy toLazy(@NonNull TaskGroupBean in);

	@BeforeMapping default void beforeBeanToLazy(
			@NonNull                TaskGroupBean in,
			@NonNull @MappingTarget TaskGroupLazy out)
	{
		// Required arguments are set in constructor
	}

	@AfterMapping default void afterBeanToLazy(
			@NonNull                TaskGroupBean in,
			@NonNull @MappingTarget TaskGroupLazy out)
	{
		// Map Optional<String> description to String description
		out.description(in.description().orElse(null));
	}

	@ObjectFactory default @NonNull TaskGroupLazy createLazy(@NonNull TaskGroupBean in)
	{
		return new TaskGroupDTOLazy(in);
	}

	// ========================================
	// Lazy → Bean
	// ========================================

	@NonNull
	TaskGroupBean toBean(@NonNull TaskGroupLazy in);

	@BeforeMapping default void beforeLazyToBean(
			@NonNull                TaskGroupLazy in,
			@NonNull @MappingTarget TaskGroupBean out)
	{
		// Required arguments are set in constructor
	}

	@AfterMapping default void afterLazyToBean(
			@NonNull                TaskGroupLazy in,
			@NonNull @MappingTarget TaskGroupBean out)
	{
		// No manual mappings in addition to those done by mapstruct
	}

	@ObjectFactory default @NonNull TaskGroupBean createBean(@NonNull TaskGroupLazy in)
	{
		return new TaskGroupBean(in, in.name());
	}
}

