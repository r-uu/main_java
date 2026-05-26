package de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.dbcommand;

import de.ruu.lib.cdi.se.CDIContainer;
import de.ruu.lib.ws_rs.NonTechnicalException;
import de.ruu.lib.ws_rs.TechnicalException;
import jakarta.enterprise.inject.spi.CDI;

/** Standalone runner: populates the database via {@link DBPopulate}. */
public class DBPopulateRunner
{
        public static void main(String[] args) throws NonTechnicalException, TechnicalException
        {
                CDIContainer.bootstrap(DBPopulate.class.getClassLoader());
                CDI.current().select(DBPopulate.class).get().run();
        }
}

