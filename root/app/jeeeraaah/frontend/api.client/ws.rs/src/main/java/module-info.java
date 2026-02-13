module de.ruu.app.jeeeraaah.frontend.api.client.ws.rs
{
	exports de.ruu.app.jeeeraaah.frontend.api.client.ws.rs;
	exports de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.auth;
	exports de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.dto;

	opens de.ruu.app.jeeeraaah.frontend.api.client.ws.rs;
	opens de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.auth;
	opens de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.dbcommand;
	opens de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.dto;
	opens de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.example;

	// internal libraries
	requires de.ruu.lib.cdi.se;
	requires de.ruu.lib.cdi.common;
	requires de.ruu.lib.jackson;
	requires de.ruu.lib.mapstruct;
	requires de.ruu.lib.util;
	requires de.ruu.lib.ws.rs;

	// internal project modules
	requires de.ruu.app.jeeeraaah.common.api.bean;
	requires de.ruu.app.jeeeraaah.common.api.domain;
	requires de.ruu.app.jeeeraaah.common.api.mapping.bean.dto;
	requires de.ruu.app.jeeeraaah.common.api.ws.rs;

	// Java standard library modules
	requires java.net.http;

	// external libraries jackson
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.datatype.jsr310;
	requires com.fasterxml.jackson.datatype.jdk8;
	requires com.fasterxml.jackson.jakarta.rs.json;

	// external libraries jakarta
	requires jakarta.annotation;
	requires jakarta.inject;
	requires jakarta.cdi;
	requires jakarta.ws.rs;

	// external libraries jersey
	requires jersey.client;

	// external libraries microprofile
	requires microprofile.config.api;

	// other external libraries
	requires org.slf4j;

	requires static lombok;
}