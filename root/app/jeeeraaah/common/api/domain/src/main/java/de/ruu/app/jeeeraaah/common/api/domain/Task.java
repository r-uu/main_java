package de.ruu.app.jeeeraaah.common.api.domain;

import lombok.NonNull;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

/**
 * Generic, technology (JPA, JSONB, JAXB, MapStruct, ...) agnostic interface for tasks. Each task belongs to a {@link
 * TaskGroup}. {@link Task}s may have a {@link #superTask()} and {@link #subTasks()} with the same {@link TaskGroup} as
 * well as {@link #predecessors()} and {@link #successors()} with the same or even other {@link TaskGroup}s.
 *
 * @param <TG> {@link TaskGroup} implementation for the task group that all {@link #superTask()}, {@link #subTasks()},
 *             {@link #predecessors()} and {@link #successors()} belong to
 */
public interface Task<TG   extends TaskGroup<? extends Task<TG, ?>>,
                      SELF extends Task<TG, SELF>>
		extends TaskData<SELF>
{
	@NonNull TG   taskGroup();
	@NonNull SELF taskGroup  (@NonNull TG taskGroup);

	/**
	 * @return superordinate (parent) task, parent task has to be in the same {@link TaskGroup} as this task instance. At
	 *         the same time this instance has to be one of the parent's children.
	 */
	Optional<SELF> superTask();
	@NonNull SELF  superTask(SELF superTask);

	/**
	 * @return subordinate (child) tasks
	 *         <p>
	 *         Optional unmodifiable set of {@link Task}s, optional supports lazy loading: empty optional means no attempt
	 *         was made to load children, present optional but empty {@code Set} means, no children could be loaded
	 *         <p>
	 *         Children have to be in the same {@link TaskGroup} as this task instance.
	 *         <p>
	 *         Read-only: sub-task membership is exclusively controlled via {@link #superTask(Object)}.
	 */
	Optional<Set<SELF>> subTasks();

	/**
	 * @return tasks that have to be finished before this task can start
	 *         <p>
	 *         Optional unmodifiable set of {@link Task}s, optional supports lazy loading, empty optional means no attempt
	 *         was made to load predecessors, present optional but empty {@code Set} means, no predecessors could be
	 *         loaded
	 *         <p>
	 *         Predecessors do <b>not</b> have to be in the same {@link TaskGroup} as this task instance.
	 */
	Optional<Set<SELF>> predecessors();
	boolean          addPredecessor (@NonNull SELF task);
	boolean       removePredecessor (@NonNull SELF task);

	/**
	 * @return tasks that can not start until this task is finished
	 *         <p>
	 *         Optional unmodifiable set of {@link Task}s, optional supports lazy loading, empty optional means no attempt
	 *         was made to load successors, present optional but empty {@code Set} means, no successors could be loaded
	 *         <p>
	 *         Successors do <b>not</b> have to be in the same {@link TaskGroup} as this task instance.
	 */
	Optional<Set<SELF>> successors();
	boolean          addSuccessor (@NonNull SELF task);
	boolean       removeSuccessor (@NonNull SELF task);

	Comparator<Task<?, ?>> COMPARATOR = (o1, o2) ->
	{
		if (o1 == null && o2 == null) return 0;
		if (o1 == null) return -1;
		if (o2 == null) return 1;
		return o1.name().compareTo(o2.name());
	};
}