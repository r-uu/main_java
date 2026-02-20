/**
 * Common API domain module containing core domain model interfaces and base classes.
 * <p>
 * This module provides the foundation for the Jeeeraaah task management system by defining:
 * <ul>
 *   <li>Core domain entities and their contracts ({@link de.ruu.app.jeeeraaah.common.api.domain.TaskEntity},
 *       {@link de.ruu.app.jeeeraaah.common.api.domain.TaskGroupEntity})</li>
 *   <li>Lazy-loading variants for performance optimization</li>
 *   <li>Flat representations for simplified data transfer</li>
 *   <li>Inter-task relationship configurations</li>
 * </ul>
 * <p>
 * <strong>Transitive Dependency Pattern:</strong>
 * This module is declared as {@code requires transitive} by several API modules
 * (e.g., {@code common.api.ws.rs}, {@code frontend.ui.fx}), making its types
 * automatically available to all modules that depend on those modules. This ensures
 * consistent access to core domain types across all application layers without
 * requiring every module to explicitly declare this dependency.
 *
 * @since 0.0.1
 */
module de.ruu.app.jeeeraaah.common.api.domain
{
	exports de.ruu.app.jeeeraaah.common.api.domain;
	exports de.ruu.app.jeeeraaah.common.api.domain.flat;
	exports de.ruu.app.jeeeraaah.common.api.domain.lazy;

	requires transitive de.ruu.lib.jpa.core;
	requires static transitive lombok;
	requires transitive jakarta.annotation;
	requires transitive java.desktop;

	requires de.ruu.lib.mapstruct;
	requires static com.fasterxml.jackson.annotation;
	requires static com.fasterxml.jackson.databind;
	requires de.ruu.lib.util;

	// Open for reflection-based frameworks (minimal, targeted access):
	// - Lombok: @AllArgsConstructor, @NoArgsConstructor, @Getter processing
	// - Jackson: JSON serialization/deserialization via @JsonProperty
	opens de.ruu.app.jeeeraaah.common.api.domain      to lombok, com.fasterxml.jackson.databind;
	opens de.ruu.app.jeeeraaah.common.api.domain.flat to lombok, com.fasterxml.jackson.databind;
	opens de.ruu.app.jeeeraaah.common.api.domain.lazy to lombok, com.fasterxml.jackson.databind;
}
