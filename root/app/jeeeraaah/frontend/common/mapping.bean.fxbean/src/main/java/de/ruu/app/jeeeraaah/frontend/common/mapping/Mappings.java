package de.ruu.app.jeeeraaah.frontend.common.mapping;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupFlat;
import de.ruu.app.jeeeraaah.frontend.common.mapping.bean.flatbean.Map_TaskGroup_Bean_FlatBean;
import de.ruu.app.jeeeraaah.frontend.common.mapping.bean.fxbean.Map_TaskGroup_Bean_FXBean;
import de.ruu.app.jeeeraaah.frontend.common.mapping.bean.fxbean.Map_Task_Bean_FXBean;
import de.ruu.app.jeeeraaah.frontend.common.mapping.fxbean.bean.Map_TaskGroup_FXBean_Bean;
import de.ruu.app.jeeeraaah.frontend.common.mapping.fxbean.bean.Map_Task_FXBean_Bean;
import de.ruu.app.jeeeraaah.frontend.ui.fx.model.TaskFXBean;
import de.ruu.app.jeeeraaah.frontend.ui.fx.model.TaskGroupFXBean;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import lombok.NonNull;

public interface Mappings
{
	static TaskGroupBean   toBean  (@NonNull TaskGroupFXBean in, @NonNull ReferenceCycleTracking context)
	{
		return Map_TaskGroup_FXBean_Bean  .INSTANCE.map(in, context);
	}
	static TaskBean        toBean  (@NonNull TaskFXBean      in, @NonNull ReferenceCycleTracking context)
	{
		return Map_Task_FXBean_Bean       .INSTANCE.map(in, context);
	}
	static TaskGroupFXBean toFXBean(@NonNull TaskGroupBean   in, @NonNull ReferenceCycleTracking context)
	{
		return Map_TaskGroup_Bean_FXBean  .INSTANCE.map(in, context);
	}
	static TaskFXBean      toFXBean(@NonNull TaskBean        in, @NonNull ReferenceCycleTracking context)
	{
		return Map_Task_Bean_FXBean       .INSTANCE.map(in, context);
	}
	static TaskGroupFlat toFlatBean(@NonNull TaskGroupBean   in, @NonNull ReferenceCycleTracking context)
	{
		return Map_TaskGroup_Bean_FlatBean.INSTANCE.map(in, context);
	}
}
