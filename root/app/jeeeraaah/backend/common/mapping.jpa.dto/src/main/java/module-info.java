/**
 * Backend JPA-DTO Mapping module containing bidirectional JPA ↔ DTO mappers.
 * <p>
 * This module provides bidirectional mappings between:
 * <ul>
 *   <li>JPA ↔ DTO - for REST API data transfer</li>
 *   <li>JPA ↔ Lazy - for lazy-loading optimization</li>
 * </ul>
 * <p>
 * These mappers handle the complexity of JPA entity graphs, lazy loading,
 * and proper detachment for network transfer. Includes CDI-managed mappers
 * for dependency injection in the backend services.
 *
 * @since 0.0.1
 */
module de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto
{
	// Public API exports - used by backend services
	exports de.ruu.app.jeeeraaah.backend.common.mapping;
	exports de.ruu.app.jeeeraaah.backend.common.mapping.dto.jpa;
	exports de.ruu.app.jeeeraaah.backend.common.mapping.lazy.jpa;
	exports de.ruu.app.jeeeraaah.backend.common.mapping.jpa.lazy;

	// MapStruct processor needs access to generate mapper implementations
	exports de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto to org.mapstruct;

	// Reflective access for frameworks (targeted to specific packages):
	// - CDI (Weld): Needs reflection for bean discovery and injection
	// - MapStruct: Needs reflection during annotation processing
	opens de.ruu.app.jeeeraaah.backend.common.mapping to weld.core.impl, weld.spi;
	opens de.ruu.app.jeeeraaah.backend.common.mapping.dto.jpa to weld.core.impl, weld.spi;
	opens de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto to weld.core.impl, weld.spi;
	opens de.ruu.app.jeeeraaah.backend.common.mapping.lazy.jpa to weld.core.impl, weld.spi;

	// Use transitive requires for modules whose types are exposed in this module's public API
	requires transitive de.ruu.app.jeeeraaah.backend.persistence.jpa;
	requires transitive de.ruu.app.jeeeraaah.common.api.ws.rs;
	requires transitive de.ruu.app.jeeeraaah.common.api.domain;
	requires transitive de.ruu.lib.mapstruct;
	requires de.ruu.lib.jpa.core;
	requires transitive jakarta.persistence;
	requires jakarta.cdi;

	requires static lombok;
	requires static org.slf4j;
}