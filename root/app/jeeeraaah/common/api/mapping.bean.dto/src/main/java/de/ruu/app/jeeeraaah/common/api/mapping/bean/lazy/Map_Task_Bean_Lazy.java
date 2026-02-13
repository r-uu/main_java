package de.ruu.app.jeeeraaah.common.api.mapping.bean.lazy;

import static de.ruu.app.jeeeraaah.common.api.mapping.Mappings.toLazy;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import org.mapstruct.factory.Mappers;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.domain.TaskLazy;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTOLazy;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import lombok.NonNull;

/** {@link TaskDTOLazy} -> {@link TaskBean} */
@Mapper public interface Map_Task_Bean_Lazy
{
	Map_Task_Bean_Lazy INSTANCE = Mappers.getMapper(Map_Task_Bean_Lazy.class);

	/**
	 * @param in    the task bean to be mapped
	 * @return      the mapped lazy task
	 */
	// Ignore Optional<T> fields here to avoid MapStruct warnings about type mismatch.
	// These fields are manually mapped in afterMapping() where Optional.orElse(null) extracts the value.
	@Mapping(target = "description", ignore = true)
	@Mapping(target = "start", ignore = true)
	@Mapping(target = "end", ignore = true)
	@NonNull TaskLazy map(@NonNull TaskBean in);

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@BeforeMapping
	default void beforeMapping(
			@NonNull                TaskBean in,
			@NonNull @MappingTarget TaskLazy out)
	{
		// required arguments are set in constructor
	}

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@AfterMapping
	default void afterMapping(
			@NonNull                TaskBean in,
			@NonNull @MappingTarget TaskLazy out)
	{
		// Map Optional<T> fields to T fields
		out.description(in.description().orElse(null));
		out.start(in.start().orElse(null));
		out.end(in.end().orElse(null));
	}

	/** mapstruct object factory */
	@ObjectFactory default @NonNull TaskLazy create(@NonNull TaskBean in)
	{
		return new TaskDTOLazy(toLazy(in.taskGroup(), new ReferenceCycleTracking()), in);
	}
}