/**
 * Backend Persistence JPA module containing JPA entities and repository services.
 * <p>
 * This module provides:
 * <ul>
 *   <li>TaskJPA, TaskGroupJPA - JPA entity implementations</li>
 *   <li>TaskServiceJPA, TaskGroupServiceJPA - CDI-managed repository services</li>
 *   <li>Database persistence layer for the task management system</li>
 * </ul>
 * <p>
 * All entities are managed by Hibernate and exposed through CDI-injectable services
 * for use in REST endpoints and business logic.
 *
 * @since 0.0.1
 */
module de.ruu.app.jeeeraaah.backend.persistence.jpa
{
	exports de.ruu.app.jeeeraaah.backend.persistence.jpa;

	requires jakarta.annotation;
	requires jakarta.cdi;
	requires jakarta.inject;
	requires jakarta.persistence;
	requires jakarta.ws.rs;
	requires static jakarta.transaction;

	requires de.ruu.lib.jpa.core;
	requires de.ruu.lib.mapstruct;
	requires de.ruu.lib.util;
	requires de.ruu.app.jeeeraaah.common.api.mapping.bean.dto;
	requires de.ruu.app.jeeeraaah.common.api.domain;
	requires de.ruu.app.jeeeraaah.common.api.ws.rs;
	requires org.slf4j;

	requires static lombok;

	// Open for reflection-based frameworks (minimal, targeted access):
	// - org.hibernate.orm.core: JPA entity scanning and proxy generation
	// - weld.se.shaded: CDI bean discovery for @ApplicationScoped services
	opens de.ruu.app.jeeeraaah.backend.persistence.jpa to org.hibernate.orm.core, weld.se.shaded;

	// - weld.se.shaded: CDI bean discovery and proxy generation for @ApplicationScoped services
	opens de.ruu.app.jeeeraaah.backend.persistence.jpa.ee to weld.se.shaded;
}