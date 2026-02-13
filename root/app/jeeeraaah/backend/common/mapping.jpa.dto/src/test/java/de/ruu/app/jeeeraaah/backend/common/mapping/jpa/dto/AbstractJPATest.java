package de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto;

import static de.ruu.lib.jpa.se.hibernate.PersistenceUnitProperties.HBM2DLLAuto.CREATE_DROP;

import javax.sql.DataSource;

import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import de.ruu.lib.jdbc.core.JDBCURL;
import de.ruu.lib.jdbc.postgres.DataSourceFactory;
import de.ruu.lib.jpa.se.hibernate.EntityManagerFactoryProducer;
import de.ruu.lib.jpa.se.hibernate.PersistenceUnitInfo;
import de.ruu.lib.jpa.se.hibernate.PersistenceUnitProperties;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for JPA tests that provides a PostgreSQL database and EntityManager for testing JPA entities and
 * mappings.
 */
@Slf4j
public abstract class AbstractJPATest
{
	protected EntityManagerFactory entityManagerFactory;
	protected EntityManager entityManager;

	// PostgreSQL connection properties - read from MicroProfile Config (microprofile-config.properties)
	private static final String DB_HOST = ConfigProvider.getConfig().getValue("database.host", String.class);
	private static final Integer DB_PORT = ConfigProvider.getConfig().getValue("database.port", Integer.class);
	private static final String DB_NAME = ConfigProvider.getConfig().getValue("database.name", String.class);
	private static final String DB_USER = ConfigProvider.getConfig().getValue("database.user", String.class);
	private static final String DB_PASS = ConfigProvider.getConfig().getValue("database.password", String.class);
	private static final String PU_NAME = "test-unit";

	@BeforeEach
	void beforeEach()
	{
		log.debug("Setting up PostgreSQL database for tests");

		try
		{
			// Create JDBC URL for PostgreSQL
			JDBCURL jdbcURL = new de.ruu.lib.jdbc.postgres.JDBCURL(DB_HOST, DB_PORT, DB_NAME);

			// Create DataSource
			DataSourceFactory dataSourceFactory = new DataSourceFactory(jdbcURL, DB_USER, DB_PASS);
			DataSource dataSource = dataSourceFactory.create();

			// Create PersistenceUnitInfo
			PersistenceUnitInfo persistenceUnitInfo = new PersistenceUnitInfo(PU_NAME, dataSource);

			// Add managed classes from TestPersistenceUnitInfo
			TestPersistenceUnitInfo testPUI = new TestPersistenceUnitInfo();
			for (String className : testPUI.getManagedClassNames())
			{
				try
				{
					Class<?> clazz = Class.forName(className);
					persistenceUnitInfo.addManagedClass(clazz);
				}
				catch (ClassNotFoundException e)
				{
					throw new RuntimeException("Failed to load managed class: " + className, e);
				}
			}

			// Configure Hibernate properties
			PersistenceUnitProperties hibernateProperties = PersistenceUnitProperties.builder().formatSQL(false)
					.hbm2ddlAuto(CREATE_DROP).jdbcDriver(org.postgresql.Driver.class).jdbcURL(jdbcURL.asString()).showSQL(false)
					.build();

			// Create EntityManagerFactory
			EntityManagerFactoryProducer factoryProducer = new EntityManagerFactoryProducer(persistenceUnitInfo,
					hibernateProperties);

			entityManagerFactory = factoryProducer.produce(DB_USER, DB_PASS);
			entityManager = entityManagerFactory.createEntityManager();

			System.out.println("EntityManager created successfully");
		}
		catch (Exception e)
		{
			System.err.println("Failed to create EntityManager: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Failed to create EntityManager for tests", e);
		}
	}

	@AfterEach
	void afterEach()
	{
		if (entityManager != null && entityManager.isOpen())
		{
			if (entityManager.getTransaction().isActive())
			{
				entityManager.getTransaction().rollback();
			}
			entityManager.close();
		}
		if (entityManagerFactory != null && entityManagerFactory.isOpen())
		{
			entityManagerFactory.close();
		}
		System.out.println("EntityManager and EntityManagerFactory closed");
	}

	/** Persist an entity within a transaction */
	protected <T> T persist(T entity)
	{
		entityManager.getTransaction().begin();
		entityManager.persist(entity);
		entityManager.getTransaction().commit();
		return entity;
	}

	/** Persist and flush an entity within a transaction */
	protected <T> T persistAndFlush(T entity)
	{
		entityManager.getTransaction().begin();
		entityManager.persist(entity);
		entityManager.flush();
		entityManager.getTransaction().commit();
		return entity;
	}

	/** Clear the persistence context (detach all entities) */
	protected void clearPersistenceContext()
	{
		entityManager.clear();
	}

	/** Find an entity by its ID */
	protected <T> T find(Class<T> entityClass, Object id)
	{
		return entityManager.find(entityClass, id);
	}
}