package de.ruu.app.jeeeraaah.common.api.mapping.bean_flat;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import org.mapstruct.factory.Mappers;

import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupFlat;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupFlat.TaskGroupFlatSimple;
import lombok.NonNull;

/**
 * Bidirectional mapper for TaskGroup: Bean ↔ Flat
 * <p>
 * Combines mappings in both directions:
 * <ul>
 *   <li>Bean → Flat: {@link #toFlat(TaskGroupBean)}</li>
 *   <li>Flat → Bean: {@link #toBean(TaskGroupFlat)}</li>
 * </ul>
 * <p>
 * Flat representations omit relationships and contain only core data.
 */
@Mapper
public interface TaskGroupMapper
{
	TaskGroupMapper INSTANCE = Mappers.getMapper(TaskGroupMapper.class);

	// ========================================
	// Bean → Flat
	// ========================================

	@NonNull
	TaskGroupFlat toFlat(@NonNull TaskGroupBean in);

	@ObjectFactory
	default @NonNull TaskGroupFlat createFlat(@NonNull TaskGroupBean in)
	{
		// Use TaskGroupFlatSimple constructor that accepts TaskGroupEntity
		return new TaskGroupFlatSimple(in);
	}

	// ========================================
	// Flat → Bean
	// ========================================

	@Mapping(target = "name", ignore = true)
	@Mapping(target = "description", ignore = true)
	@NonNull
	TaskGroupBean toBean(@NonNull TaskGroupFlat in);

	@BeforeMapping default void beforeFlatToBean(
			@NonNull                TaskGroupFlat          in,
			@NonNull @MappingTarget TaskGroupBean          out)
	{
		// Required arguments are set in constructor
	}

	@AfterMapping default void afterFlatToBean(
			@NonNull                TaskGroupFlat          in,
			@NonNull @MappingTarget TaskGroupBean          out)
	{
		// No additional mappings needed
	}

	@ObjectFactory default @NonNull TaskGroupBean createBean(@NonNull TaskGroupFlat in)
	{
		return new TaskGroupBean(in);
	}
}

