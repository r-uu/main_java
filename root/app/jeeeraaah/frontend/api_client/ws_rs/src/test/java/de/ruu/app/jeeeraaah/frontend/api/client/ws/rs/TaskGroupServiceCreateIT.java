package de.ruu.app.jeeeraaah.frontend.api.client.ws.rs;

import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.lib.cdi.common.CDIExtension;
import de.ruu.lib.cdi.se.CDIContainer;
import de.ruu.lib.junit.DisabledOnServerNotListening;
import de.ruu.lib.ws_rs.NonTechnicalException;
import de.ruu.lib.ws_rs.TechnicalException;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.*;
import static java.util.Objects.nonNull;

/**
 * Test for TaskGroupService create endpoint (client).
 * <p>
 * Analogous to TestDBCommands: boots a CDI SE container and uses the ws.rs client to call the backend.
 */
@DisabledOnServerNotListening(propertyNameHost = "jeeeraaah.rest-api.host", propertyNamePort = "jeeeraaah.rest-api.port")
@Slf4j
class TaskGroupServiceCreateIT
{
	private static SeContainer seContainer;

	@Inject
	private TaskGroupServiceClient taskGroupServiceClient;

	@BeforeAll static void beforeAll()
	{
		log.debug("initialising CDI SE container for tests");
		try
		{
			seContainer =
					SeContainerInitializer
							.newInstance()
							.addExtensions(CDIExtension.class)
							.initialize();
			CDIContainer.bootstrap(TaskGroupServiceCreateIT.class.getClassLoader());
		}
		catch (Exception e)
		{
			log.error("failure initialising seContainer", e);
		}
		log.debug("cdi container initialisation {}", seContainer == null ? "unsuccessful" : "successful");
	}

	@BeforeEach void beforeEach()
	{
		// obtain client from CDI
		taskGroupServiceClient = CDI.current().select(TaskGroupServiceClient.class).get();
		log.debug("initialised taskGroupServiceClient: {}", nonNull(taskGroupServiceClient));
	}

	@Test void testCreateTaskGroup() throws NonTechnicalException, TechnicalException
	{
		// prepare DTO
		String name = "test-create-" + System.currentTimeMillis();
		TaskGroupBean dto = new TaskGroupBean(name);

		// call backend via client
		TaskGroupBean created = taskGroupServiceClient.create(dto);

		// assertions using AssertJ
		assertThat(created).as("created result should not be null").isNotNull();
		// id should be present after creation
		assertThat(created.id()).as("created id should not be null").isNotNull();
		assertThat(created.name()).as("created name should match request").isEqualTo(name);
	}
}