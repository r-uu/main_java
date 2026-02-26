package de.ruu.app.jeeeraaah.common.api.domain;

import de.ruu.app.jeeeraaah.common.api.domain.flat.TaskGroupFlat;
import lombok.NonNull;

import java.io.Serial;
import java.util.Optional;
import java.util.Set;

/**
 * Generic, technology (JPA, JSONB, JAXB, MapStruct, ...) agnostic service interface for task groups.
 *
 * @param <TG> TaskGroup implementation type
 * @param <T>  Task implementation type belonging to the TaskGroup
 *
 * TODO wouldn't it be better to throw more specific exceptions like TaskGroupNotFoundException instead of Exception?
 */
public interface TaskGroupService<TG extends TaskGroup<T>, T extends Task<?, ?>>
{
	/**
	 * creates a new task group in the backend
	 * @param taskGroup the task group to create
	 * @return the created task group
	 * @throws Exception if an error occurs
	 */
	@NonNull           TG  create(@NonNull TG   taskGroup) throws Exception;

	/**
	 * reads the task group with the given id from the backend
	 * @param id the id of the task group to read
	 * @return the task group with the given id
	 * @throws TaskGroupNotFoundException if the task group with the given id does not exist
	 */
	Optional<? extends TG> read  (@NonNull Long id       ) throws Exception;

	/**
	 * updates the task group if it is persistent (has an id) and can be found in the backend<p>
	 * if it is persistent but can not be found, it throws a TaskGroupNotFoundException<p>
	 * if it is not persistent, it creates a new task group in the backend
	 * @param taskGroup the task group to update or create
	 * @return the updated or newly created task group
	 * @throws TaskGroupNotFoundException if the task group with the given id does not exist
	 */
	@NonNull           TG  update(@NonNull TG   taskGroup) throws Exception;

	/**
	 * deletes the task group with the given id from the backend
	 * @param id the id of the task group to delete
	 * @throws TaskGroupNotFoundException if the task group with the given id does not exist
	 */
	void                   delete(@NonNull Long id       ) throws Exception;

	/**
	 * finds all task groups in the backend, task groups are flat which means they do not contain information to related
	 * tasks
	 * @return a set of all task groups in the backend
	 */
	@NonNull Set<TaskGroupFlat> findAllFlat() throws Exception;

//	/**
//	 * finds the task group with the given id and returns it with sets of id references to all its directly related tasks
//	 * @param id the id of the task group to find
//	 * @return the task group with the given id and the sets of id references to all its directly related tasks
//	 * @throws TaskGroupNotFoundException if the task group with the given id does not exist
//	 */
//	Optional<TaskGroupLazy>     findLazy(@NonNull Long id) throws Exception;

	/**
	 * finds the task group with the given id and returns it with all its tasks
	 * @param id the id of the task group to find
	 * @return the task group with the given id and all its tasks
	 * @throws TaskGroupNotFoundException if the task group with the given id does not exist
	 */
	Optional<? extends TG>      findWithTasks(@NonNull Long id) throws Exception;

	/**
	 * finds the task group with the given id and returns it with all its tasks and their neighbours
	 * @param id the id of the task group to find
	 * @return the task group with the given id and all its tasks and their neighbours
	 * @throws TaskGroupNotFoundException if the task group with the given id does not exist
	 */
	Optional<? extends TG>      findWithTasksAndDirectNeighbours(@NonNull Long id) throws Exception;

	/**
	 * removes a task from a task group
	 * @param idGroup the id of the task group
	 * @param idTask the id of the task to remove
	 */
	void removeTaskFromGroup(@NonNull Long idGroup, @NonNull Long idTask) throws Exception;

	class TaskGroupNotFoundException extends RuntimeException
	{
		@Serial private static final long serialVersionUID = 1L;
		public TaskGroupNotFoundException(String text) { super(text); }
	}
}