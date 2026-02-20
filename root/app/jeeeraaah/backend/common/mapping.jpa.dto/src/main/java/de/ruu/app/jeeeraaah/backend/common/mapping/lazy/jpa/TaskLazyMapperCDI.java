package de.ruu.app.jeeeraaah.backend.common.mapping.lazy.jpa;

import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskGroupJPA;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskJPA;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskServiceJPA.TaskLazyMapper;
import de.ruu.app.jeeeraaah.common.api.domain.lazy.TaskLazy;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.NonNull;

/**
 * CDI-enabled adapter for TaskLazyMapper interface. Bridges the MapStruct-generated mapper with CDI injection, avoiding
 * circular compile-time dependencies.
 */
@ApplicationScoped
public class TaskLazyMapperCDI implements TaskLazyMapper
{
	public TaskJPA map(TaskGroupJPA taskGroup, TaskLazy taskLazy)
	{
		return Map_Task_Lazy_JPA.INSTANCE.map(taskLazy, taskGroup, new de.ruu.lib.mapstruct.ReferenceCycleTracking());
	}
}