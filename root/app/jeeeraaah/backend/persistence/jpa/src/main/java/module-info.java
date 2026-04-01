/**
 * Backend Persistence JPA module with strict JPMS encapsulation.
 * <p>
 * This module provides:
 * <ul>
 *   <li><strong>DTO-based Service Interfaces</strong> (TaskDTOService, TaskGroupDTOService) - Public API for REST layer</li>
 *   <li><strong>JPA Entities</strong> (entity.TaskJPA, entity.TaskGroupJPA) - Hidden in entity subpackage with qualified export</li>
 *   <li><strong>Internal Service Implementations</strong> (ee/internal packages) - Fully encapsulated</li>
 *   <li><strong>Helper Interfaces</strong> (TaskCreationService, TaskLazyMapper, TaskRelationService)</li>
 * </ul>
 * <p>
 * <strong>Package Structure (Strict Encapsulation):</strong>
 * <ul>
 *   <li><code>de.ruu.app.jeeeraaah.backend.persistence.jpa</code> - Service interfaces (public export)</li>
 *   <li><code>de.ruu.app.jeeeraaah.backend.persistence.jpa.entity</code> - JPA entities (qualified export)</li>
 *   <li><code>de.ruu.app.jeeeraaah.backend.persistence.jpa.internal</code> - Service implementations (not exported)</li>
 *   <li><code>de.ruu.app.jeeeraaah.backend.persistence.jpa.ee</code> - EE-specific implementations (not exported)</li>
 * </ul>
 * <p>
 * <strong>Architecture (Option 2 - Strict Encapsulation):</strong>
 * <ul>
 *   <li>JPA entities are in separate entity package with qualified export only</li>
 *   <li>REST layer uses DTO-only services (TaskDTOService, TaskGroupDTOService)</li>
 *   <li>DTO service implementations (in backend.api.ws.rs) delegate to internal JPA services</li>
 *   <li>All JPA↔DTO mapping happens internally - REST layer is 100% JPA-free</li>
 * </ul>
 *
 * @since 0.0.1
 */
module de.ruu.app.jeeeraaah.backend.persistence.jpa
{
	// Public export: Service interfaces and helpers (available to all modules)
	// - TaskDTOService, TaskGroupDTOService (DTO-based service interfaces)
	// - TaskCreationService, TaskLazyMapper, TaskRelationService (helper interfaces)
	exports de.ruu.app.jeeeraaah.backend.persistence.jpa;
	
	// Qualified export: JPA entities ONLY for authorized modules (strict encapsulation)
	// - backend.common.mapping.jpa.dto: needs TaskJPA/TaskGroupJPA for DTO↔JPA mappings
	// - backend.api.ws.rs: needs TaskJPA/TaskGroupJPA as type parameters in service implementations
	// This prevents uncontrolled JPA entity exposure while keeping them accessible where needed
	exports de.ruu.app.jeeeraaah.backend.persistence.jpa.entity 
		to de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto,
		   de.ruu.app.jeeeraaah.backend.api.ws.rs;

	requires jakarta.annotation;
	requires jakarta.cdi;
	requires jakarta.inject;
	requires jakarta.persistence;
	requires jakarta.ws.rs;
	requires static jakarta.transaction;

	requires de.ruu.lib.jpa.core;
	requires de.ruu.lib.mapstruct;
	requires de.ruu.lib.util;
	requires de.ruu.app.jeeeraaah.common.api.domain;
	requires de.ruu.app.jeeeraaah.common.api.ws.rs;
	requires org.slf4j;

	requires static lombok;

	// Unrestricted opens required for Jakarta EE container (Liberty):
	// Liberty bundles Hibernate and Weld as unnamed/container modules with non-standard
	// module names - targeted "opens ... to X" is not reliable in this context.
	// This is Jakarta EE's recommended approach for JPMS + container-managed frameworks.
	opens de.ruu.app.jeeeraaah.backend.persistence.jpa.entity;
	opens de.ruu.app.jeeeraaah.backend.persistence.jpa.internal;
	opens de.ruu.app.jeeeraaah.backend.persistence.jpa.ee;
}