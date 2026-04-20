package de.ruu.lib.cdi.se;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.interceptor.Interceptor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.List;

@Slf4j
public abstract class CDIContainer
{
	private CDIContainer()
	{
		throw new IllegalStateException("utility class with static methods only must not be instantiated");
	}

	public static void bootstrap() { bootstrap(ClassLoader.getSystemClassLoader()); }

	/** @param classLoader used for loading {@code META-INF/beans.xml} */
	public static void bootstrap(@NonNull ClassLoader classLoader)
	{
		bootstrap(classLoader, List.of());
	}

	/**
	 * @param classLoader used for loading {@code META-INF/beans.xml}}
	 * @param interceptorClasses list of {@link Interceptor} classes to enable
	 */
	public static void bootstrap(
			@NonNull ClassLoader classLoader,
			@NonNull List<Class<?>> interceptorClasses)
	{
		bootstrap(classLoader, interceptorClasses, List.of());
	}

	public static void bootstrap(
			@NonNull ClassLoader                      classLoader,
			@NonNull List<Class<?>>                   interceptorClasses,
			@NonNull List<Class<? extends Extension>> extensionClasses)
	{
		bootstrap(classLoader, interceptorClasses, extensionClasses, List.of());
	}

	/**
	 * @param classLoader used for loading {@code META-INF/beans.xml}}
	 * @param interceptorClasses list of {@link Interceptor} classes to enable
	 * @param extensionClasses list of {@link Extension} classes to enable
	 */
	public static void bootstrap(
			@NonNull ClassLoader                      classLoader,
			@NonNull List<Class<?>>                   interceptorClasses,
			@NonNull List<Class<? extends Extension>> extensionClasses,
			@NonNull List<Class<?>>                   beanClasses)
	{
		try
		{
			CDI.current();
			// container available, do not bootstrap another one
			return;
		}
		catch (IllegalStateException e)
		{
			// no container available, fine, let's bootstrap a new one
		}

		log.debug("initialising CDI");

		checkIfBeansXMLIsPresent(classLoader);

		try
		{
			final SeContainerInitializer initializer =
					SeContainerInitializer
							.newInstance()
							.enableInterceptors(interceptorClasses.toArray(Class[]::new))
							.addExtensions     (extensionClasses  .toArray(Class[]::new))
							.addBeanClasses    (beanClasses       .toArray(Class[]::new));

			final SeContainer container = initializer.initialize();

			log.debug("initialised CDI successfully: {}", container != null);
		}
		catch (Throwable t) // NOSONAR
		{
			log.error("failure initialising CDI", t);
			throw t;
		}
	}

	private static final String META_INF_BEANS_XML = "META-INF/beans.xml";

	private static void checkIfBeansXMLIsPresent(ClassLoader classLoader)
	{
		URL url = classLoader.getResource(META_INF_BEANS_XML);

		if (url == null)
		{
			log.warn("could not find {}", META_INF_BEANS_XML);
		}
		else
		{
			log.debug("found {} in {}", META_INF_BEANS_XML, url.getPath());
		}
	}
}