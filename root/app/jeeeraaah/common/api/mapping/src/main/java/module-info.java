/**
 * Common API Mapping module containing bidirectional Bean ↔ DTO mappers.
 * <p>
 * This module provides bidirectional mappings between:
 * <ul>
 *   <li>Bean ↔ DTO - for REST API communication</li>
 *   <li>Bean ↔ Lazy - for lazy-loading scenarios</li>
 *   <li>Bean ↔ Flat - for simplified data structures</li>
 * </ul>
 * <p>
 * All mappers use MapStruct for type-safe, compile-time code generation and include
 * proper cycle detection for circular references.
 * <p>
 * <strong>Package Structure:</strong>
 * <ul>
 *   <li>{@code bean_dto} - Task and TaskGroup Bean ↔ DTO (bidirectional)</li>
 *   <li>{@code bean_lazy} - Task and TaskGroup Bean ↔ Lazy (bidirectional)</li>
 *   <li>{@code bean_flat} - TaskGroup Bean ↔ Flat (bidirectional)</li>
 * </ul>
 *
 * @since 0.0.1
 */
module de.ruu.app.jeeeraaah.common.api.mapping
{
	// Public API exports - consolidated packages
	exports de.ruu.app.jeeeraaah.common.api.mapping;
	exports de.ruu.app.jeeeraaah.common.api.mapping.bean_dto;
	exports de.ruu.app.jeeeraaah.common.api.mapping.bean_lazy;
	exports de.ruu.app.jeeeraaah.common.api.mapping.bean_flat;

	requires de.ruu.app.jeeeraaah.common.api.domain;
	requires de.ruu.app.jeeeraaah.common.api.bean;
	requires de.ruu.app.jeeeraaah.common.api.ws.rs;
	requires de.ruu.lib.jpa.core;
	requires de.ruu.lib.mapstruct;
	requires jakarta.annotation;

	requires static lombok;
	requires static java.compiler; // Needed for MapStruct generated code
}