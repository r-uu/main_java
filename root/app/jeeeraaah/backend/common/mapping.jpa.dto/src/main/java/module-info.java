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
	exports de.ruu.app.jeeeraaah.backend.common.mapping;
	exports de.ruu.app.jeeeraaah.backend.common.mapping.dto.jpa;
	exports de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto to org.mapstruct;
	exports de.ruu.app.jeeeraaah.backend.common.mapping.lazy.jpa;
	exports de.ruu.app.jeeeraaah.backend.common.mapping.jpa.lazy;

	// Open for CDI bean discovery (minimal, targeted access):
	// - Weld: @ApplicationScoped bean proxying for CDI-managed mappers (e.g., TaskLazyMapperCDI)
	// Note: Weld modules are provided by application server at runtime
	opens de.ruu.app.jeeeraaah.backend.common.mapping;
	opens de.ruu.app.jeeeraaah.backend.common.mapping.lazy.jpa;

	requires de.ruu.app.jeeeraaah.backend.persistence.jpa;
	requires de.ruu.app.jeeeraaah.common.api.ws.rs;
	requires de.ruu.app.jeeeraaah.common.api.domain;
	requires de.ruu.lib.mapstruct;
	requires de.ruu.lib.jpa.core;
	requires jakarta.persistence;
	requires jakarta.cdi;

	requires static lombok;
	requires static org.slf4j;
}