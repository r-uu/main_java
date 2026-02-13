package de.ruu.app.jeeeraaah.common.api.mapping.lazy.bean;

import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupLazy;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTOLazy;
import lombok.NonNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import org.mapstruct.factory.Mappers;

import static java.util.Objects.requireNonNull;

/** {@link TaskGroupDTOLazy} -> {@link TaskGroupBean} */
@Mapper(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface Map_TaskGroup_Lazy_Bean
{
	Map_TaskGroup_Lazy_Bean INSTANCE = Mappers.getMapper(Map_TaskGroup_Lazy_Bean.class);

	@NonNull TaskGroupBean map(@NonNull TaskGroupLazy in);

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@BeforeMapping default void beforeMapping(@NonNull TaskGroupLazy in, @NonNull @MappingTarget TaskGroupBean out)
	{
		// required arguments are set in constructor
	}

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@AfterMapping default void afterMapping(@NonNull TaskGroupLazy in, @NonNull @MappingTarget TaskGroupBean out)
	{
		// no manual mappings in addition to those done by mapstruct
	}

	/** mapstruct object factory */
	@ObjectFactory default @NonNull TaskGroupBean create(@NonNull TaskGroupLazy in) { return new TaskGroupBean(in, in.name()); }
}