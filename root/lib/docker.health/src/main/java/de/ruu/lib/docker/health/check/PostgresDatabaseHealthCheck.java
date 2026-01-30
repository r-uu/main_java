package de.ruu.lib.docker.health.check;

import de.ruu.lib.docker.health.HealthCheckResult;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Checks if a PostgreSQL database is accessible.
 */
@Slf4j
public class PostgresDatabaseHealthCheck implements HealthCheck
{
	private final String containerName;
	private final String databaseName;
	private final int    port;
	private final String username;
	private final String password;

	public PostgresDatabaseHealthCheck(String containerName, String databaseName, int port, String username, String password)
	{
		this.containerName = containerName;
		this.databaseName  = databaseName;
		this.port          = port;
		this.username      = username;
		this.password      = password;
	}

	/**
	 * Convenience constructor with default credentials from .env:
	 * - For jeeeraaah DB: jeeeraaah / jeeeraaah
	 * - For keycloak DB: keycloak / keycloak
	 * - For lib_test DB: lib_test / lib_test
	 */
	public PostgresDatabaseHealthCheck(String containerName, String databaseName, int port)
	{
		// Determine credentials based on database name
		String user, pass;
		if ("keycloak".equals(databaseName))
		{
			user = "keycloak";
			pass = "keycloak";
		}
		else if ("lib_test".equals(databaseName))
		{
			user = "lib_test";
			pass = "lib_test";
		}
		else
		{
			// jeeeraaah
			user = "jeeeraaah";
			pass = "jeeeraaah";
		}
		this.containerName = containerName;
		this.databaseName  = databaseName;
		this.port          = port;
		this.username      = user;
		this.password      = pass;
	}

	@Override
	public HealthCheckResult check()
	{
		log.info("Checking database '{}' in container '{}'...", databaseName, containerName);

		// First check if container is running
		if (!isContainerRunning(containerName))
		{
			log.error("  ❌ Container '{}' is not running", containerName);
			return HealthCheckResult.failure(
				"PostgreSQL Container: " + containerName,
				"Container is not running",
				"cd ~/develop/github/main/config/shared/docker && docker compose up -d " + containerName,
				"ruu-docker-up"
			);
		}

		// Check if database exists
		try
		{
			String jdbcUrl = "jdbc:postgresql://localhost:" + port + "/" + databaseName;
			try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password))
			{
				log.info("  ✅ Database '{}' is accessible", databaseName);
				return HealthCheckResult.success("Database: " + databaseName);
			}
		}
		catch (Exception e)
		{
			log.error("  ❌ Cannot connect to database '{}': {}", databaseName, e.getMessage());

		String fixCommand;
		String alias;

		if (databaseName.equals("lib_test"))
		{
			// lib_test created by initdb script on container restart
			fixCommand = "cd ~/develop/github/main/config/shared/docker && docker compose restart postgres-jeeeraaah";
			alias = "ruu-docker-restart-postgres";
		}
		else
		{
			// General: restart container
			fixCommand = "cd ~/develop/github/main/config/shared/docker && docker compose restart " + containerName;
			alias = "ruu-docker-restart";
		}

			return HealthCheckResult.failure(
				"Database: " + databaseName,
				"Cannot connect to database: " + e.getMessage(),
				fixCommand,
				alias
			);
		}
	}

	@Override
	public String getName()
	{
		return "PostgreSQL Database: " + databaseName;
	}

	private boolean isContainerRunning(String containerName)
	{
		try
		{
			Process process = Runtime.getRuntime().exec(
				new String[]{"docker", "inspect", "-f", "{{.State.Running}}", containerName}
			);

			try (java.io.BufferedReader reader = new java.io.BufferedReader(
					new java.io.InputStreamReader(process.getInputStream())))
			{
				String line = reader.readLine();
				return "true".equals(line);
			}
		}
		catch (Exception e)
		{
			return false;
		}
	}
}
