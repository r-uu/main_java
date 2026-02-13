package de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.PersistenceUnitTransactionType;

/**
 * Simple PersistenceUnitInfo implementation for testing.
 * 
 * Note: This class implements PersistenceUnitInfo which requires returning PersistenceUnitTransactionType
 * from getTransactionType(). PersistenceUnitTransactionType was deprecated in Jakarta Persistence 3.2.0,
 * but the replacement API is not yet available in any released version. The @SuppressWarnings annotation
 * is necessary until the Jakarta Persistence API provides the new TransactionType enum and updates the
 * PersistenceUnitInfo interface to use it.
 */
@SuppressWarnings("removal")
public class TestPersistenceUnitInfo implements PersistenceUnitInfo
{
	private final List<String> managedClassNames = new ArrayList<>();

	public TestPersistenceUnitInfo() {
		// Add entity classes
		managedClassNames.add("de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskGroupJPA");
		managedClassNames.add("de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskJPA");
	}

	@Override
	public String getPersistenceUnitName()
	{
		return "test-unit";
	}

	@Override
	public String getPersistenceProviderClassName()
	{
		return "org.hibernate.jpa.HibernatePersistenceProvider";
	}

	@Override
	public PersistenceUnitTransactionType getTransactionType()
	{
		return PersistenceUnitTransactionType.RESOURCE_LOCAL;
	}

	@Override
	public DataSource getJtaDataSource()
	{
		return null;
	}

	@Override
	public DataSource getNonJtaDataSource()
	{
		return null;
	}

	@Override
	public List<String> getMappingFileNames()
	{
		return Collections.emptyList();
	}

	@Override
	public List<URL> getJarFileUrls()
	{
		return Collections.emptyList();
	}

	@Override
	public URL getPersistenceUnitRootUrl()
	{
		return null;
	}

	@Override
	public List<String> getManagedClassNames()
	{
		return managedClassNames;
	}

	@Override
	public boolean excludeUnlistedClasses()
	{
		return false;
	}

	@Override
	public SharedCacheMode getSharedCacheMode()
	{
		return SharedCacheMode.UNSPECIFIED;
	}

	@Override
	public ValidationMode getValidationMode()
	{
		return ValidationMode.AUTO;
	}

	@Override
	public Properties getProperties()
	{
		return new Properties();
	}

	@Override
	public String getPersistenceXMLSchemaVersion()
	{
		return "3.0";
	}

	@Override
	public ClassLoader getClassLoader()
	{
		return Thread.currentThread().getContextClassLoader();
	}

	@Override
	public void addTransformer(ClassTransformer transformer)
	{
		// Not needed for tests
	}

	@Override
	public ClassLoader getNewTempClassLoader()
	{
		return null;
	}

	@Override
	public List<String> getQualifierAnnotationNames()
	{
		return Collections.emptyList();
	}

	@Override
	public String getScopeAnnotationName()
	{
		return null;
	}
}
