package de.ruu.app.jeeeraaah.common.api.mapping.flat.bean;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupFlat;
import de.ruu.app.jeeeraaah.common.api.mapping.Mappings;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import lombok.NonNull;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.Set;

import static java.util.Objects.isNull;

/** {@link TaskGroupFlat} -> {@link TaskGroupBean} */
@Mapper
public interface Map_TaskGroup_Flat_Bean
{
	Map_TaskGroup_Flat_Bean INSTANCE = Mappers.getMapper(Map_TaskGroup_Flat_Bean.class);

	@Mapping(target = "name", ignore = true)
	@Mapping(target = "description", ignore = true)
	@NonNull TaskGroupBean map(@NonNull TaskGroupFlat in);

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@BeforeMapping default void beforeMapping(
			@NonNull                TaskGroupFlat          in,
			@NonNull @MappingTarget TaskGroupBean          out)
	{
		// required arguments are set in constructor
	}

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@AfterMapping default void afterMapping(
			@NonNull                TaskGroupFlat          in,
			@NonNull @MappingTarget TaskGroupBean          out)
	{ }

	/** mapstruct object factory */
	@ObjectFactory default @NonNull TaskGroupBean create(@NonNull TaskGroupFlat in) { return new TaskGroupBean(in); }
}