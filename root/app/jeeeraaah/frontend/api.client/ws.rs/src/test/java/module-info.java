/**
 * Test module descriptor for JUnit 5 tests in IntelliJ IDEA.
 * 
 * This module-info.java is required for IntelliJ's test runner to discover and execute JUnit tests in a JPMS (Java
 * Platform Module System) environment.
 * 
 * Maven (surefire) handles test execution differently and doesn't require this file, but IntelliJ's integrated test
 * runner needs explicit module configuration.
 */
open module de.ruu.app.jeeeraaah.frontend.api.client.ws.rs
{
	// Re-export all requirements from main module
	exports de.ruu.app.jeeeraaah.frontend.api.client.ws.rs;
	exports de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.dto;

	// Open ALL packages for reflection (needed for JUnit, Mockito, IntelliJ test runner)
	// Using 'open module' (see module declaration above) makes all packages accessible for reflection

	// internal libraries
	requires de.ruu.lib.cdi.se;
	requires de.ruu.lib.cdi.common;
	requires de.ruu.lib.jackson;
	requires de.ruu.lib.junit;
	requires de.ruu.lib.mapstruct;
	requires de.ruu.lib.util;
	requires de.ruu.lib.ws.rs;

	// internal project modules
	requires de.ruu.app.jeeeraaah.common.api.bean;
	requires de.ruu.app.jeeeraaah.common.api.domain;
	requires de.ruu.app.jeeeraaah.common.api.mapping;
	requires de.ruu.app.jeeeraaah.common.api.ws.rs;

	// external libraries jackson
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.datatype.jsr310;
	requires com.fasterxml.jackson.jakarta.rs.json;

	// external libraries jakarta
	requires jakarta.inject;
	requires jakarta.cdi;

	// external libraries jersey
	requires jersey.client;

	// external libraries microprofile
	requires microprofile.config.api;

	// other external libraries
	requires org.slf4j;
	requires jakarta.ws.rs;
	requires com.fasterxml.jackson.datatype.jdk8;
	requires static lombok;

	// JUnit 5 dependencies
	requires org.junit.jupiter.api;
	requires org.junit.platform.commons;
	requires org.hamcrest;
}
