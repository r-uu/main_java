package de.ruu.app.jeeeraaah.common.api.mapping;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupFlat;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupLazy;
import de.ruu.app.jeeeraaah.common.api.domain.TaskLazy;
import de.ruu.app.jeeeraaah.common.api.mapping.bean.dto.Map_TaskGroup_Bean_DTO;
import de.ruu.app.jeeeraaah.common.api.mapping.bean.dto.Map_Task_Bean_DTO;
import de.ruu.app.jeeeraaah.common.api.mapping.bean.lazy.Map_TaskGroup_Bean_Lazy;
import de.ruu.app.jeeeraaah.common.api.mapping.bean.lazy.Map_Task_Bean_Lazy;
import de.ruu.app.jeeeraaah.common.api.mapping.dto.bean.Map_TaskGroup_DTO_Bean;
import de.ruu.app.jeeeraaah.common.api.mapping.dto.bean.Map_Task_DTO_Bean;
import de.ruu.app.jeeeraaah.common.api.mapping.flat.bean.Map_TaskGroup_Flat_Bean;
import de.ruu.app.jeeeraaah.common.api.mapping.lazy.bean.Map_TaskGroup_Lazy_Bean;
import de.ruu.app.jeeeraaah.common.api.mapping.lazy.bean.Map_Task_Lazy_Bean;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTOLazy;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTO;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import lombok.NonNull;

public interface Mappings
{
	static @NonNull TaskGroupDTO  toDTO(@NonNull TaskGroupBean in, @NonNull ReferenceCycleTracking context)
	{
		return Map_TaskGroup_Bean_DTO.INSTANCE.map(in, context);
	}

	static @NonNull TaskDTO       toDTO(@NonNull TaskBean in, @NonNull ReferenceCycleTracking context)
	{
		return Map_Task_Bean_DTO.INSTANCE.map(in, context);
	}

	static @NonNull TaskGroupBean toBean(@NonNull TaskGroupDTO in, @NonNull ReferenceCycleTracking context)
	{
		return Map_TaskGroup_DTO_Bean.INSTANCE.map(in, context);
	}

	static @NonNull TaskBean      toBean(@NonNull TaskDTO in, @NonNull ReferenceCycleTracking context)
	{
		return Map_Task_DTO_Bean.INSTANCE.map(in, context);
	}

	static @NonNull TaskGroupLazy toLazy(@NonNull TaskGroupBean in, @NonNull ReferenceCycleTracking context)
	{
		return Map_TaskGroup_Bean_Lazy.INSTANCE.map(in);
	}

	static @NonNull TaskLazy      toLazy(@NonNull TaskBean in, @NonNull ReferenceCycleTracking context)
	{
		return Map_Task_Bean_Lazy.INSTANCE.map(in);
	}

	static @NonNull TaskGroupBean toBean(@NonNull TaskGroupLazy in, @NonNull ReferenceCycleTracking context)
	{
		return Map_TaskGroup_Lazy_Bean.INSTANCE.map(in);
	}

	static @NonNull TaskBean      toBean(@NonNull TaskGroupBean group, @NonNull TaskDTOLazy in, @NonNull ReferenceCycleTracking context)
	{
		return Map_Task_Lazy_Bean.INSTANCE.map(group, in);
	}

	static @NonNull TaskGroupBean toBean(@NonNull TaskGroupFlat group)
	{
		return Map_TaskGroup_Flat_Bean.INSTANCE.map(group);
	}
}