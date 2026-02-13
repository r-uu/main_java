/**
 * Frontend Bean-FXBean Mapping module containing bidirectional Bean ↔ FXBean mappers.
 * <p>
 * This module provides bidirectional mappings between:
 * <ul>
 *   <li>Bean ↔ FXBean - for JavaFX property binding and UI reactivity</li>
 *   <li>Bean → FlatBean - for simplified table/tree representations</li>
 * </ul>
 * <p>
 * These mappers enable seamless integration between backend data structures
 * and JavaFX UI components, handling observable properties and UI-specific requirements.
 *
 * @since 0.0.1
 */
module de.ruu.app.jeeeraaah.frontend.common.mapping.bean.fxbean
{
	exports de.ruu.app.jeeeraaah.frontend.common.mapping;
	exports de.ruu.app.jeeeraaah.frontend.common.mapping.bean.flatbean;
	exports de.ruu.app.jeeeraaah.frontend.common.mapping.bean.fxbean;
	exports de.ruu.app.jeeeraaah.frontend.common.mapping.fxbean.bean;

	requires de.ruu.lib.mapstruct;
	requires de.ruu.app.jeeeraaah.common.api.bean;
	requires de.ruu.app.jeeeraaah.common.api.domain;
	requires de.ruu.app.jeeeraaah.frontend.ui.fx.model;

	requires static lombok;
}
