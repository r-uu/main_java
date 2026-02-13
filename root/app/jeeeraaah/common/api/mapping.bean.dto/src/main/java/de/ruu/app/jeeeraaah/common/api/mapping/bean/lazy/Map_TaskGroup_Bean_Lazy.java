package de.ruu.app.jeeeraaah.common.api.mapping.bean.lazy;

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

/** {@link TaskGroupDTOLazy} -> {@link TaskGroupBean} */
@Mapper public interface Map_TaskGroup_Bean_Lazy
{
	Map_TaskGroup_Bean_Lazy INSTANCE = Mappers.getMapper(Map_TaskGroup_Bean_Lazy.class);

	// Ignore Optional<String> description to avoid MapStruct warning about type mismatch.
	// The field is manually mapped in afterMapping() where Optional.orElse(null) extracts the value.
	@Mapping(target = "name", ignore = true)
	@Mapping(target = "description", ignore = true)
	@NonNull TaskGroupLazy map(@NonNull TaskGroupBean in);

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@BeforeMapping default void beforeMapping(@NonNull TaskGroupBean in, @NonNull @MappingTarget TaskGroupLazy out)
	{
		// required arguments are set in constructor
	}

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@AfterMapping default void afterMapping(@NonNull TaskGroupBean in, @NonNull @MappingTarget TaskGroupLazy out)
	{
		// Map Optional<String> description to String description
		out.description(in.description().orElse(null));
	}

	/** mapstruct object factory */
	@ObjectFactory default @NonNull TaskGroupLazy create(@NonNull TaskGroupBean in) { return new TaskGroupDTOLazy(in); }
}