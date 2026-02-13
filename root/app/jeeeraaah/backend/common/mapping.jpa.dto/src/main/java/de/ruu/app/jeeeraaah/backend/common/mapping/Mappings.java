package de.ruu.app.jeeeraaah.backend.common.mapping;

import de.ruu.app.jeeeraaah.backend.common.mapping.dto.jpa.Map_TaskGroup_DTO_JPA;
import de.ruu.app.jeeeraaah.backend.common.mapping.dto.jpa.Map_Task_DTO_JPA;
import de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto.Map_TaskGroup_JPA_DTO;
import de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto.Map_Task_JPA_DTO;
import de.ruu.app.jeeeraaah.backend.common.mapping.jpa.lazy.Map_TaskGroup_JPA_Lazy;
import de.ruu.app.jeeeraaah.backend.common.mapping.jpa.lazy.Map_Task_JPA_Lazy;
import de.ruu.app.jeeeraaah.backend.common.mapping.lazy.jpa.Map_TaskGroup_Lazy_JPA;
import de.ruu.app.jeeeraaah.backend.common.mapping.lazy.jpa.Map_Task_Lazy_JPA;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskGroupJPA;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskJPA;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupLazy;
import de.ruu.app.jeeeraaah.common.api.domain.TaskLazy;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTO;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;

public interface Mappings
{
	static TaskGroupDTO toDTO(TaskGroupJPA in, ReferenceCycleTracking context)
	{
		return Map_TaskGroup_JPA_DTO.INSTANCE.map(in, context);
	}

	static TaskDTO toDTO(TaskJPA in, ReferenceCycleTracking context)
	{
		return Map_Task_JPA_DTO.INSTANCE.map(in, context);
	}

	static TaskGroupJPA toJPA(TaskGroupDTO in, ReferenceCycleTracking context)
	{
		return Map_TaskGroup_DTO_JPA.INSTANCE.map(in, context);
	}

	static TaskJPA toJPA(TaskDTO in, ReferenceCycleTracking context)
	{
		return Map_Task_DTO_JPA.INSTANCE.map(in, context);
	}

	static TaskGroupLazy toLazy(TaskGroupJPA in, ReferenceCycleTracking context)
	{
		return Map_TaskGroup_JPA_Lazy.INSTANCE.map(in, context);
	}

	static TaskLazy toLazy(TaskJPA in, ReferenceCycleTracking context)
	{
		return Map_Task_JPA_Lazy.INSTANCE.map(in, context);
	}

	static TaskGroupJPA toJPA(TaskGroupLazy in)
	{
		return Map_TaskGroup_Lazy_JPA.INSTANCE.map(in);
	}

	static TaskJPA toJPA(TaskGroupJPA taskGroup, TaskLazy in)
	{
		return Map_Task_Lazy_JPA.INSTANCE.map(in, taskGroup, new ReferenceCycleTracking());
	}
}