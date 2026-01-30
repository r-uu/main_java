package de.ruu.app.jeeeraaah.common.api.ws.rs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

@NoArgsConstructor(force = true)
@Getter
@Accessors(fluent = true)
@ToString
public class TaskCreationData
{
	@NonNull private final Long taskGroupId;
	@NonNull private final TaskDTOLazy task;

	@JsonCreator
	public TaskCreationData(
			@JsonProperty("taskGroupId") @NonNull Long taskGroupId,
			@JsonProperty("task") @NonNull TaskDTOLazy task)
	{
		this.taskGroupId = taskGroupId;
		this.task = task;
	}
}