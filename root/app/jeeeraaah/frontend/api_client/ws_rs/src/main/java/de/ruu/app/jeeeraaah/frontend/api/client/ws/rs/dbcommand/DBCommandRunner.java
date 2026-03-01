package de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.dbcommand;

import de.ruu.lib.cdi.se.CDIContainer;
import de.ruu.lib.ws_rs.NonTechnicalException;
import de.ruu.lib.ws_rs.TechnicalException;
import jakarta.enterprise.inject.spi.CDI;

public class DBCommandRunner
{
	public static void main(String[] args) throws NonTechnicalException, TechnicalException
	{
		CDIContainer.bootstrap(DBPopulate.class.getClassLoader());
		DBClean    clean    = CDI.current().select(DBClean   .class).get();
		DBPopulate populate = CDI.current().select(DBPopulate.class).get();
		clean   .run();
		populate.run();
	}
}