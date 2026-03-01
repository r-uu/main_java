module de.ruu.app.jeeeraaah.frontend.ui.fx
{
	requires jakarta.annotation;
	requires jakarta.inject;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;

	requires de.ruu.lib.jpa.core;
	requires de.ruu.lib.fx.comp;
	requires de.ruu.lib.fx.core;
	requires de.ruu.lib.util;
	requires de.ruu.lib.cdi.se;
	requires de.ruu.lib.cdi.common;
	requires de.ruu.lib.mapstruct;
	requires de.ruu.lib.ws.rs;
	requires de.ruu.lib.docker.health;
	requires de.ruu.lib.keycloak.admin;  // Required for docker.health auto-fix
	requires org.postgresql.jdbc;  // Required by docker.health JDBC health checks
	requires transitive de.ruu.app.jeeeraaah.common.api.bean;
	requires transitive de.ruu.app.jeeeraaah.common.api.domain;
	requires de.ruu.app.jeeeraaah.common.api.ws.rs;
	requires de.ruu.app.jeeeraaah.common.api.mapping;
	requires de.ruu.app.jeeeraaah.frontend.ui.fx.model;
	requires de.ruu.app.jeeeraaah.frontend.api.client.ws.rs;
	requires de.ruu.app.jeeeraaah.frontend.common.mapping.bean.fxbean;

	requires static lombok;
	requires org.slf4j;  // Automatic module - needs --add-modules in .mvn/jvm.config for IntelliJ
	requires de.ruu.lib.util.config.mp;
	requires de.ruu.lib.postgres.util.ui;

	// Export only main package for fx.executable (classpath) access to MainAppRunner
	// All other packages remain encapsulated - they only need 'opens' for CDI/JavaFX reflection
	exports de.ruu.app.jeeeraaah.frontend.ui.fx;

	// Open packages for CDI bean discovery and proxy generation (Weld SE) and JavaFX reflection
	// Opens are open without restrictions because:
	// - javafx.fxml needs access for FXML controller instantiation and @FXML field injection
	// - weld.se.shaded needs access for CDI @Inject and @ApplicationScoped proxy generation
	// - unnamed modules (e.g., during application startup via runners) also need reflection access
	opens de.ruu.app.jeeeraaah.frontend.ui.fx;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.auth;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.dash;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.task;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.task.edit;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.task.selector;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.task.view;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.directneighbours;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy.predecessor;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy.predecessor.add;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy.predecessor.add.super_sub_or_predecessor;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy.successor;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy.successor.add;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy.successor.add.super_sub_or_successor;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy.supersub;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy.supersub.add;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.list.directneighbours;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.taskgroup;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.taskgroup.edit;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.taskgroup.selector;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.test;
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.util;
}
