package de.ruu.app.jeeeraaah.common.api.domain;

import static de.ruu.app.jeeeraaah.common.api.domain.PathsCommon.PATH_JEEERAAAH_ROOT;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsCommon.TOKEN_BY_ID;

/**
 * Constants for task REST API paths.
 * <p>
 * <b>URL Structure:</b> {@code http://host:port/jeeeraaah/task/...}
 * <ul>
 *   <li>{@code /jeeeraaah} - Application root (from {@code @ApplicationPath("jeeeraaah")})</li>
 *   <li>{@code /task} - Resource path (TOKEN_DOMAIN)</li>
 *   <li>Additional path segments for specific operations</li>
 * </ul>
 */
public interface PathsTask
{
	String TOKEN_DOMAIN = "/task";
	String PATH_DOMAIN  = PATH_JEEERAAAH_ROOT + TOKEN_DOMAIN;

	String TOKEN_ALL = "/all";
	String PATH_ALL  = PATH_DOMAIN + TOKEN_ALL;

	String TOKEN_BY_ID_WITH_RELATED = "/byIdWithRelated";
	String PATH_BY_ID_WITH_RELATED  = PATH_DOMAIN + TOKEN_BY_ID_WITH_RELATED + TOKEN_BY_ID;

	// ── Super / sub hierarchy (single entry-point) ────────────────────────────
	/** Body: {@code InterTaskRelationData} with {@code id = childId, idRelated = parentId} */
	String TOKEN_SET_SUPER_TASK    = "/setSuperTask";
	String PATH_SET_SUPER_TASK     = PATH_DOMAIN + TOKEN_SET_SUPER_TASK;
	/** Path param: {@code {id}} = childId */
	String TOKEN_REMOVE_SUPER_TASK = "/removeSuperTask";

	// ── Predecessor / successor ───────────────────────────────────────────────
	String TOKEN_ADD_PREDECESSOR    = "/addPredecessor";
	String PATH_ADD_PREDECESSOR     = PATH_DOMAIN + TOKEN_ADD_PREDECESSOR;

	String TOKEN_ADD_SUCCESSOR      = "/addSuccessor";
	String PATH_ADD_SUCCESSOR       = PATH_DOMAIN + TOKEN_ADD_SUCCESSOR;

	String TOKEN_REMOVE_PREDECESSOR = "/removePredecessor";
	String PATH_REMOVE_PREDECESSOR  = PATH_DOMAIN + TOKEN_REMOVE_PREDECESSOR;

	String TOKEN_REMOVE_SUCCESSOR   = "/removeSuccessor";
	String PATH_REMOVE_SUCCESSOR    = PATH_DOMAIN + TOKEN_REMOVE_SUCCESSOR;

	// ── Bulk operations ───────────────────────────────────────────────────────
	String TOKEN_REMOVE_NEIGHBOURS_FROM_TASK = "/removeNeighboursFromTask";
	String PATH_REMOVE_NEIGHBOURS_FROM_TASK  = PATH_DOMAIN + TOKEN_REMOVE_NEIGHBOURS_FROM_TASK;

	String PATH_BY_ID = PATH_DOMAIN + TOKEN_BY_ID;
}