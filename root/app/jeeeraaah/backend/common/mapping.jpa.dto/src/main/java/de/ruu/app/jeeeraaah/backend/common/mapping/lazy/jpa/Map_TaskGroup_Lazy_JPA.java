package de.ruu.app.jeeeraaah.backend.common.mapping.lazy.jpa;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import org.mapstruct.factory.Mappers;

import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskGroupJPA;
import de.ruu.app.jeeeraaah.common.api.domain.lazy.TaskGroupLazy;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTO;
import lombok.NonNull;

/** {@link TaskGroupJPA} -> {@link TaskGroupDTO} */
@Mapper
public interface Map_TaskGroup_Lazy_JPA
{
	Map_TaskGroup_Lazy_JPA INSTANCE = Mappers.getMapper(Map_TaskGroup_Lazy_JPA.class);

	@Mapping(target = "name", ignore = true) // set in constructor
	@Mapping(target = "description", ignore = true) @Mapping(target = "tasks", ignore = true) @NonNull
	TaskGroupJPA map(@NonNull TaskGroupLazy in);

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@BeforeMapping
	default void beforeMapping(@NonNull TaskGroupLazy in, @NonNull @MappingTarget TaskGroupJPA out)
	{
		// required arguments id, version and name are already set in constructor
	}

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@AfterMapping
	default void afterMapping(@NonNull TaskGroupLazy in, @NonNull @MappingTarget TaskGroupJPA out)
	{
		// required arguments id, version and name are already set in constructor
	}

	/** mapstruct object factory */
	@ObjectFactory
	default @NonNull TaskGroupJPA create(@NonNull TaskGroupLazy in)
	{
		return new TaskGroupJPA(in);
	}
}