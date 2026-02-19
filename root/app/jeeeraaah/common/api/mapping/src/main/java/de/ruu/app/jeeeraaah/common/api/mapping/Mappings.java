package de.ruu.app.jeeeraaah.common.api.mapping;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupFlat;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupLazy;
import de.ruu.app.jeeeraaah.common.api.domain.TaskLazy;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTOLazy;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTO;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import lombok.NonNull;

/**
 * Central facade for all Bean ↔ DTO, Bean ↔ Lazy, and Bean ↔ Flat mappings.
 * <p>
 * This interface provides a unified API for mapping operations, delegating to specialized mappers:
 * <ul>
 *   <li>{@link de.ruu.app.jeeeraaah.common.api.mapping.bean_dto.TaskMapper} - Task Bean ↔ DTO</li>
 *   <li>{@link de.ruu.app.jeeeraaah.common.api.mapping.bean_dto.TaskGroupMapper} - TaskGroup Bean ↔ DTO</li>
 *   <li>{@link de.ruu.app.jeeeraaah.common.api.mapping.bean_lazy.TaskMapper} - Task Bean ↔ Lazy</li>
 *   <li>{@link de.ruu.app.jeeeraaah.common.api.mapping.bean_lazy.TaskGroupMapper} - TaskGroup Bean ↔ Lazy</li>
 *   <li>{@link de.ruu.app.jeeeraaah.common.api.mapping.bean_flat.TaskGroupMapper} - TaskGroup Bean ↔ Flat</li>
 * </ul>
 */
public interface Mappings
{
	// ========================================
	// Bean → DTO
	// ========================================

	static @NonNull TaskGroupDTO toDTO(@NonNull TaskGroupBean in, @NonNull ReferenceCycleTracking context)
	{
		return de.ruu.app.jeeeraaah.common.api.mapping.bean_dto.TaskGroupMapper.INSTANCE.toDTO(in, context);
	}

	static @NonNull TaskDTO toDTO(@NonNull TaskBean in, @NonNull ReferenceCycleTracking context)
	{
		return de.ruu.app.jeeeraaah.common.api.mapping.bean_dto.TaskMapper.INSTANCE.toDTO(in, context);
	}

	// ========================================
	// DTO → Bean
	// ========================================

	static @NonNull TaskGroupBean toBean(@NonNull TaskGroupDTO in, @NonNull ReferenceCycleTracking context)
	{
		return de.ruu.app.jeeeraaah.common.api.mapping.bean_dto.TaskGroupMapper.INSTANCE.toBean(in, context);
	}

	static @NonNull TaskBean toBean(@NonNull TaskDTO in, @NonNull ReferenceCycleTracking context)
	{
		return de.ruu.app.jeeeraaah.common.api.mapping.bean_dto.TaskMapper.INSTANCE.toBean(in, context);
	}

	// ========================================
	// Bean → Lazy
	// ========================================

	static @NonNull TaskGroupLazy toLazy(@NonNull TaskGroupBean in, @NonNull ReferenceCycleTracking context)
	{
		return de.ruu.app.jeeeraaah.common.api.mapping.bean_lazy.TaskGroupMapper.INSTANCE.toLazy(in);
	}

	static @NonNull TaskLazy toLazy(@NonNull TaskBean in, @NonNull ReferenceCycleTracking context)
	{
		return de.ruu.app.jeeeraaah.common.api.mapping.bean_lazy.TaskMapper.INSTANCE.toLazy(in);
	}

	// ========================================
	// Lazy → Bean
	// ========================================

	static @NonNull TaskGroupBean toBean(@NonNull TaskGroupLazy in, @NonNull ReferenceCycleTracking context)
	{
		return de.ruu.app.jeeeraaah.common.api.mapping.bean_lazy.TaskGroupMapper.INSTANCE.toBean(in);
	}

	static @NonNull TaskBean toBean(@NonNull TaskGroupBean group, @NonNull TaskDTOLazy in, @NonNull ReferenceCycleTracking context)
	{
		return de.ruu.app.jeeeraaah.common.api.mapping.bean_lazy.TaskMapper.INSTANCE.toBean(group, in);
	}

	// ========================================
	// Flat → Bean
	// ========================================

	static @NonNull TaskGroupBean toBean(@NonNull TaskGroupFlat group)
	{
		return de.ruu.app.jeeeraaah.common.api.mapping.bean_flat.TaskGroupMapper.INSTANCE.toBean(group);
	}
}