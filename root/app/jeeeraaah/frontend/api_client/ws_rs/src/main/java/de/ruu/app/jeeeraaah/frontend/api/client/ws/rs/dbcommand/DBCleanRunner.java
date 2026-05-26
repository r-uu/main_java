package de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.dbcommand;

import de.ruu.lib.cdi.se.CDIContainer;
import de.ruu.lib.ws_rs.NonTechnicalException;
import de.ruu.lib.ws_rs.TechnicalException;
import jakarta.enterprise.inject.spi.CDI;

/** Standalone runner: cleans the database via {@link DBClean}. */
public class DBCleanRunner
{
        public static void main(String[] args) throws NonTechnicalException, TechnicalException
        {
                CDIContainer.bootstrap(DBClean.class.getClassLoader());
                CDI.current().select(DBClean.class).get().run();
        }
}

