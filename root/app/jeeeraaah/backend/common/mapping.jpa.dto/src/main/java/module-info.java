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
 * and proper detachment for network transfer.
 * <p>
 * <b>Architecture:</b>
 * <ul>
 *   <li>Public API: {@code Mappings} facade (exported to backend REST services only)</li>
 *   <li>Internal Implementation: MapStruct mapper interfaces (not exported)</li>
 * </ul>
 * This ensures strong encapsulation and prevents direct usage of mapper implementations.
 *
 * @since 0.0.1
 */
module de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto
{
	// ============================================================
	// Public Exports
	// ============================================================

	// Main mapping facade - for backend REST services
	// Note: backend.api.ws.rs is currently an automatic module (no module-info.java),
	// so we cannot use qualified exports. Once it becomes a proper JPMS module,
	// change this to: exports ... to de.ruu.app.jeeeraaah.backend.api.ws.rs;
	exports de.ruu.app.jeeeraaah.backend.common.mapping;

	// MapStruct processor access (unchanged)
	exports de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto
		to org.mapstruct;

	// ============================================================
	// Reflective access for frameworks
	// ============================================================

	// CDI (Weld): Needs reflection for bean discovery and injection
	opens de.ruu.app.jeeeraaah.backend.common.mapping
		to weld.core.impl, weld.spi;
	opens de.ruu.app.jeeeraaah.backend.common.mapping.dto.jpa
		to weld.core.impl, weld.spi;
	opens de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto
		to weld.core.impl, weld.spi;
	opens de.ruu.app.jeeeraaah.backend.common.mapping.lazy.jpa
		to weld.core.impl, weld.spi;
	opens de.ruu.app.jeeeraaah.backend.common.mapping.jpa.lazy
		to weld.core.impl, weld.spi;

	// ============================================================
	// Dependencies
	// ============================================================

	// Transitive: types exposed in public API
	requires transitive de.ruu.app.jeeeraaah.backend.persistence.jpa;
	requires transitive de.ruu.app.jeeeraaah.common.api.ws.rs;
	requires transitive de.ruu.app.jeeeraaah.common.api.domain;
	requires transitive de.ruu.lib.mapstruct;
	requires de.ruu.lib.jpa.core;
	requires transitive jakarta.persistence;
	requires jakarta.cdi;

	// Build-time only
	requires static lombok;
	requires static org.slf4j;
}