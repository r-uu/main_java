/**
 * Time-cycle constraint module for jeeeraaah.
 * <p>
 * Detects and prevents cycles in predecessor/successor relationships.
 * This module is a <em>plugin</em>: it depends on the persistence layer,
 * but the persistence layer does NOT depend on this module.
 * <p>
 * The extension point is {@link de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskRelationValidator}:
 * this module provides a CDI bean implementing that interface, which the persistence
 * layer discovers automatically via {@code Instance<TaskRelationValidator>} injection.
 * <p>
 * Architecture rule: no existing module may {@code requires} this module.
 */
module de.ruu.app.jeeeraaah.backend.constraint.timecycle
{
	// No public API export — this module contributes only CDI beans

	requires de.ruu.app.jeeeraaah.backend.persistence.jpa;
	requires de.ruu.app.jeeeraaah.common.api.domain;

	requires jakarta.annotation;
	requires jakarta.cdi;
	requires jakarta.inject;
	requires jakarta.persistence;
	requires static jakarta.transaction;

	requires org.slf4j;
	requires static lombok;

        // Open for CDI proxy generation (Weld/OpenLiberty)
        opens de.ruu.app.jeeeraaah.backend.constraint.timecycle;
}

