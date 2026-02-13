/**
 * Common API Bean-DTO Mapping module containing bidirectional Bean ↔ DTO mappers.
 * <p>
 * This module provides bidirectional mappings between:
 * <ul>
 *   <li>Bean ↔ DTO - for REST API communication</li>
 *   <li>Bean ↔ Lazy - for lazy-loading scenarios</li>
 *   <li>Flat → Bean - for simplified data structures</li>
 * </ul>
 * <p>
 * All mappers use MapStruct for type-safe, compile-time code generation and include
 * proper cycle detection for circular references.
 *
 * @since 0.0.1
 */
module de.ruu.app.jeeeraaah.common.api.mapping.bean.dto
{
	exports de.ruu.app.jeeeraaah.common.api.mapping;
	exports de.ruu.app.jeeeraaah.common.api.mapping.bean.dto;
	exports de.ruu.app.jeeeraaah.common.api.mapping.dto.bean;
	exports de.ruu.app.jeeeraaah.common.api.mapping.lazy.bean;
	exports de.ruu.app.jeeeraaah.common.api.mapping.bean.lazy;
	exports de.ruu.app.jeeeraaah.common.api.mapping.flat.bean;

	requires de.ruu.app.jeeeraaah.common.api.domain;
	requires de.ruu.app.jeeeraaah.common.api.bean;
	requires de.ruu.app.jeeeraaah.common.api.ws.rs;
	requires de.ruu.lib.jpa.core;
	requires de.ruu.lib.mapstruct;
	requires jakarta.annotation;

	requires static lombok;
	requires static java.compiler; // Needed for MapStruct generated code
}