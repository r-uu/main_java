package de.ruu.lib.jpa.se.hibernate.postgres;

import static de.ruu.lib.jpa.se.hibernate.PersistenceUnitProperties.HBM2DLLAuto.CREATE;
import static java.util.Objects.isNull;

import java.util.List;

import javax.sql.DataSource;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import de.ruu.lib.jdbc.core.JDBCURL;
import de.ruu.lib.jdbc.postgres.DataSourceFactory;
import de.ruu.lib.jpa.se.hibernate.EntityManagerFactoryProducer;
import de.ruu.lib.jpa.se.hibernate.PersistenceUnitInfo;
import de.ruu.lib.jpa.se.hibernate.PersistenceUnitProperties;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link #produce()} returns an instance of a hibernate {@link EntityManager}.
 * Call this method from method annotated
 * with {@link jakarta.enterprise.inject.Produces} in subclasses.
 */
@Slf4j
public abstract class AbstractEntityManagerProducer
{
    private EntityManager entityManager;

	@Inject
	@ConfigProperty(name = "de.ruu.lib.jpa.se.hibernate.postgres.host", defaultValue = "localhost")
	private String host;

	@Inject
	@ConfigProperty(name = "de.ruu.lib.jpa.se.hibernate.postgres.port", defaultValue = "5432")
	private Integer port;

	@Inject
	@ConfigProperty(name = "de.ruu.lib.jpa.se.hibernate.postgres.database", defaultValue = "lib_test")
	private String database;

	@Inject
	@ConfigProperty(name = "de.ruu.lib.jpa.se.hibernate.postgres.username", defaultValue = "lib_test")
	private String username;

	@Inject
	@ConfigProperty(name = "de.ruu.lib.jpa.se.hibernate.postgres.password", defaultValue = "lib_test")
	private String password;

    /** puname -> persistence unit name */
	@Inject
	@ConfigProperty(name = "de.ruu.lib.jpa.se.hibernate.postgres.puname", defaultValue = "lib_test")
	private String puname;

	/**
	 * call this method from method annotated with
	 * {@link jakarta.enterprise.inject.Produces} in subclasses
	 */
	protected EntityManager produce()
	{
		if (!isNull(entityManager)) return entityManager;

		JDBCURL jdbcURL = new de.ruu.lib.jdbc.postgres.JDBCURL(host, port, database);

		DataSourceFactory dataSourceFactory = new DataSourceFactory(jdbcURL, username, password);
		DataSource dataSource = dataSourceFactory.create();

		PersistenceUnitInfo persistenceUnitInfo = new PersistenceUnitInfo(puname, dataSource);

		// customise managed classes
		for (Class<?> clazz : managedClasses()) { persistenceUnitInfo.addManagedClass(clazz); }

		PersistenceUnitProperties hibernateProperties = PersistenceUnitProperties
				.builder()
				// .dialect(PostgreSQLDialect.class)
				.formatSQL(true)
				.hbm2ddlAuto(CREATE)
				.jdbcDriver(org.postgresql.Driver.class)
				.jdbcURL(jdbcURL.asString())
				.showSQL(true)
				.build();

		EntityManagerFactoryProducer factoryProducer = new EntityManagerFactoryProducer(persistenceUnitInfo,
				hibernateProperties);

		EntityManagerFactory entityManagerFactory = factoryProducer.produce(username, password);

		entityManager = entityManagerFactory.createEntityManager();
		log.debug("created entity manager: {}", entityManager);

		return entityManager;
	}

	public abstract List<Class<?>> managedClasses();
}