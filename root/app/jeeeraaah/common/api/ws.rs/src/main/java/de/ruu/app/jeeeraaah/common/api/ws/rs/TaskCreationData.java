package de.ruu.app.jeeeraaah.common.api.ws.rs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Data Transfer Object for creating a new Task.
 * Uses Lombok @NonNull for null checks and normal getters.
 * Jackson deserializes via @JsonCreator constructor.
 */
@Getter
@ToString
public class TaskCreationData
{
	@NonNull
	private final Long taskGroupId;

	@NonNull
	private final TaskDTOLazy task;

	@JsonCreator
	public TaskCreationData(
			@JsonProperty(value = "taskGroupId", required = true) @NonNull Long taskGroupId,
			@JsonProperty(value = "task", required = true) @NonNull TaskDTOLazy task)
	{
		this.taskGroupId = taskGroupId;
		this.task = task;
	}
}