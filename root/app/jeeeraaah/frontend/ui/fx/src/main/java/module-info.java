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
	requires transitive de.ruu.app.jeeeraaah.common.api.bean;
	requires transitive de.ruu.app.jeeeraaah.common.api.domain;
	requires de.ruu.app.jeeeraaah.common.api.ws.rs;
	requires de.ruu.app.jeeeraaah.common.api.mapping.bean.dto;
	requires de.ruu.app.jeeeraaah.frontend.ui.fx.model;
	requires de.ruu.app.jeeeraaah.frontend.api.client.ws.rs;
	requires de.ruu.app.jeeeraaah.frontend.common.mapping.bean.fxbean;

	requires static lombok;
	requires org.slf4j;  // Automatic module - needs --add-modules in .mvn/jvm.config for IntelliJ
	requires de.ruu.lib.util.config.mp;
	requires de.ruu.lib.postgres.util.ui;

	// Export packages explicitly for CDI and JavaFX
	exports de.ruu.app.jeeeraaah.frontend.ui.fx;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.auth;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.dash;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.task;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.task.edit;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.task.selector;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.task.view;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.directneighbours;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy.predecessor;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy.predecessor.add;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy.predecessor.add.super_sub_or_predecessor;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy.successor;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy.successor.add;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy.successor.add.super_sub_or_successor;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy.supersub;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.hierarchy.supersub.add;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.task.view.list.directneighbours;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.taskgroup;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.taskgroup.edit;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.taskgroup.selector;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.test;
	exports de.ruu.app.jeeeraaah.frontend.ui.fx.util;  // Session retry executor

	// Open packages for CDI bean discovery and proxy generation (Weld SE) and JavaFX reflection
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
	opens de.ruu.app.jeeeraaah.frontend.ui.fx.util;  // Session retry executor
}
