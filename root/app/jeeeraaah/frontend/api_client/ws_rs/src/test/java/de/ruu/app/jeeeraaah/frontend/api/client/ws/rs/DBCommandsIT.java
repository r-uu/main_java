package de.ruu.app.jeeeraaah.frontend.api.client.ws.rs;

import de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.dbcommand.DBClean;
import de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.dbcommand.DBPopulate;
import de.ruu.lib.cdi.common.CDIExtension;
import de.ruu.lib.cdi.se.CDIContainer;
import de.ruu.lib.junit.DisabledOnServerNotListening;
import de.ruu.lib.ws_rs.NonTechnicalException;
import de.ruu.lib.ws_rs.TechnicalException;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.enterprise.inject.spi.CDI;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.ruu.lib.util.BooleanFunctions.not;
import static java.util.Objects.isNull;

//@Disabled
@DisabledOnServerNotListening(propertyNameHost = "jeeeraaah.rest-api.host", propertyNamePort = "jeeeraaah.rest-api.port")
@Slf4j
class DBCommandsIT
{
	private static SeContainer seContainer; // initialisation and closure handled in before/after all methods

	private DBClean    dbClean;
	private DBPopulate dbPopulate;

	@BeforeAll static void beforeAll()
	{
		log.debug("cdi container initialisation");
		try
		{
			seContainer =
					SeContainerInitializer
							.newInstance()
							.addExtensions(CDIExtension.class)
							.initialize();
			CDIContainer.bootstrap(DBCommandsIT.class.getClassLoader());
		}
		catch (Exception e)
		{
			log.error("failure initialising seContainer", e);
		}
		log.debug("cdi container initialisation {}", seContainer == null ? "unsuccessful" : "successful");
	}

	@BeforeEach void beforeEach()
	{
		dbClean    = CDI.current().select(DBClean   .class).get();
		dbPopulate = CDI.current().select(DBPopulate.class).get();
		log.debug("initialised successfully: {}", not(isNull(dbClean)) && not(isNull(dbPopulate)));
	}

	@Test void testCleanThenPopulate() throws NonTechnicalException, TechnicalException
	{
		dbClean.run();
		dbPopulate.run();
	}
}