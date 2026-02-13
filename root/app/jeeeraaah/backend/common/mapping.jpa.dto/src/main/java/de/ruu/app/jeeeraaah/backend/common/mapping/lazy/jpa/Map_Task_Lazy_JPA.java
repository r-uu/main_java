package de.ruu.app.jeeeraaah.backend.common.mapping.lazy.jpa;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskGroupJPA;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskJPA;
import de.ruu.app.jeeeraaah.common.api.domain.TaskLazy;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import lombok.NonNull;

/**
 * {@link TaskLazy} -> {@link TaskJPA}
 * <p>
 * Note: Using IGNORE policy because TaskLazy getters are inherited from TaskData interface,
 * and MapStruct may not recognize inherited interface methods in all cases.
 * <p>
 * Uses {@link de.ruu.lib.mapstruct.OptionalMapper} to convert between {@code Optional<T>} and {@code T}.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = de.ruu.lib.mapstruct.OptionalMapper.class)
public interface Map_Task_Lazy_JPA
{
	Map_Task_Lazy_JPA INSTANCE = Mappers.getMapper(Map_Task_Lazy_JPA.class);

	/**
	 * Maps a lazy task DTO to a JPA task entity.
	 * 
	 * @param in the lazy task to be mapped (SOURCE)
	 * @param group the task group to which the task belongs (passed to ObjectFactory only, marked as @Context)
	 * @param context reference cycle tracking context
	 * @return the mapped task JPA entity
	 */
	@NonNull TaskJPA map
			(
					@NonNull          TaskLazy               in,
					@NonNull @Context TaskGroupJPA           group,
					@NonNull @Context ReferenceCycleTracking context
			);

	/** annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called */
	@BeforeMapping default void beforeMapping
			(
					@NonNull                TaskLazy               in,
					@NonNull @MappingTarget TaskJPA                out,
					@NonNull @Context       ReferenceCycleTracking context
			)
	{
		// required arguments id, version and name are already set in constructor
	}

	/**
	 * Manually map Optional fields after automatic mapping.
	 * <p>
	 * This is necessary because:
	 * <ul>
	 *   <li>MapStruct cannot automatically convert Optional&lt;T&gt; to T</li>
	 *   <li>Fluent accessors (description() instead of getDescription()) are not auto-detected by @Mapping</li>
	 *   <li>IGNORE policy prevents automatic mapping of inherited interface methods</li>
	 * </ul>
	 */
	@AfterMapping default void afterMapping
			(
					@NonNull                TaskLazy               in,
					@NonNull @MappingTarget TaskJPA                out,
					@NonNull @Context       ReferenceCycleTracking context)
	{
		// Map optional fields manually: Optional<T> → T using orElse(null)
		// Extract values from Optional fields and set them on the JPA entity
		out.description(in.description().orElse(null));
		out.start      (in.start      ().orElse(null));
		out.end        (in.end        ().orElse(null));
	}

	/** mapstruct object factory */
	@ObjectFactory
	default @NonNull TaskJPA create
			(
					@NonNull          TaskLazy               in,
					@NonNull @Context TaskGroupJPA           group,
					@NonNull @Context ReferenceCycleTracking context
			)
	{
		return new TaskJPA(group, in.name());
	}
}