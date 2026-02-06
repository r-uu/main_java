package de.ruu.lib.util.config.mp;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.*;

/**
 * Health check for configuration properties.
 * Validates that all required properties are present and have valid values.
 *
 * <p>Usage:
 * <pre>
 * ConfigHealthCheck check = new ConfigHealthCheck();
 * ConfigHealthCheck.Result result = check.validate();
 * if (!result.isHealthy()) {
 *     System.err.println("Config validation failed:");
 *     result.getErrors().forEach(System.err::println);
 * }
 * </pre>
 */
@Slf4j
public class ConfigHealthCheck
{
	/**
	 * Required database properties for the jeeeraaah database.
	 */
	private static final List<String> REQUIRED_DB_JEEERAAAH_PROPS = List.of(
			"db.jeeeraaah.host",
			"db.jeeeraaah.port",
			"db.jeeeraaah.database",
			"db.jeeeraaah.username",
			"db.jeeeraaah.password"
	);

	/**
	 * Required database properties for the lib_test database.
	 */
	private static final List<String> REQUIRED_DB_LIB_TEST_PROPS = List.of(
			"db.lib_test.host",
			"db.lib_test.port",
			"db.lib_test.database",
			"db.lib_test.username",
			"db.lib_test.password"
	);

	/**
	 * Required Keycloak properties.
	 */
	private static final List<String> REQUIRED_KEYCLOAK_PROPS = List.of(
			"keycloak.url",
			"keycloak.realm",
			"keycloak.client.id",
			"keycloak.admin.username",
			"keycloak.admin.password",
			"keycloak.test.user",
			"keycloak.test.password"
	);

	/**
	 * Required service URLs.
	 */
	private static final List<String> REQUIRED_SERVICE_URLS = List.of(
			"service.backend.url",
			"service.jasperreports.url"
	);


	private final Config config;

	/**
	 * Create a new health check using the default Config.
	 */
	public ConfigHealthCheck()
	{
		this(ConfigProvider.getConfig());
	}

	/**
	 * Create a new health check using the provided Config.
	 * @param config the Config to validate
	 */
	public ConfigHealthCheck(Config config)
	{
		this.config = config;
	}

	/**
	 * Validate all required properties.
	 * @return validation result
	 */
	public Result validate()
	{
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();
		Map<String, String> validatedProperties = new LinkedHashMap<>();

		// Validate database properties
		validatePropertyGroup("Database (jeeeraaah)", REQUIRED_DB_JEEERAAAH_PROPS, errors, validatedProperties);
		validatePropertyGroup("Database (lib_test)", REQUIRED_DB_LIB_TEST_PROPS, errors, validatedProperties);

		// Validate Keycloak properties
		validatePropertyGroup("Keycloak", REQUIRED_KEYCLOAK_PROPS, errors, validatedProperties);

		// Validate service URLs
		validatePropertyGroup("Service URLs", REQUIRED_SERVICE_URLS, errors, validatedProperties);


		// Check for weak passwords in production
		if (!"true".equals(config.getOptionalValue("testing", String.class).orElse("false")))
		{
			checkPasswordStrength("db.jeeeraaah.password", warnings);
			checkPasswordStrength("db.lib_test.password", warnings);
			checkPasswordStrength("keycloak.admin.password", warnings);
		}

		return new Result(errors.isEmpty(), errors, warnings, validatedProperties);
	}

	/**
	 * Validate a group of related properties.
	 */
	private void validatePropertyGroup(
			String groupName,
			List<String> propertyNames,
			List<String> errors,
			Map<String, String> validatedProperties)
	{
		for (String propertyName : propertyNames)
		{
			Optional<String> value = config.getOptionalValue(propertyName, String.class);

			if (value.isEmpty() || value.get().trim().isEmpty())
			{
				errors.add(String.format("[%s] Missing or empty property: %s", groupName, propertyName));
			}
			else
			{
				// Mask passwords in output
				String displayValue = propertyName.contains("password")
						? "***" + value.get().substring(Math.max(0, value.get().length() - 3))
						: value.get();
				validatedProperties.put(propertyName, displayValue);
			}
		}
	}

	/**
	 * Check if a password is too weak.
	 */
	private void checkPasswordStrength(String propertyName, List<String> warnings)
	{
		config.getOptionalValue(propertyName, String.class).ifPresent(password ->
		{
			if (password.length() < 8)
			{
				warnings.add(String.format("Weak password for %s (length < 8)", propertyName));
			}
			if (password.equals("admin") || password.equals("password") || password.equals("test"))
			{
				warnings.add(String.format("Insecure password for %s (common password)", propertyName));
			}
		});
	}

	/**
	 * Validation result.
	 */
	public static class Result
	{
		private final boolean healthy;
		private final List<String> errors;
		private final List<String> warnings;
		private final Map<String, String> validatedProperties;

		public Result(boolean healthy, List<String> errors, List<String> warnings, Map<String, String> validatedProperties)
		{
			this.healthy = healthy;
			this.errors = Collections.unmodifiableList(errors);
			this.warnings = Collections.unmodifiableList(warnings);
			this.validatedProperties = Collections.unmodifiableMap(validatedProperties);
		}

		public boolean isHealthy() { return healthy; }
		public List<String> getErrors() { return errors; }
		public List<String> getWarnings() { return warnings; }
		public Map<String, String> getValidatedProperties() { return validatedProperties; }

		/**
		 * Print a formatted report to the console.
		 */
		public void printReport()
		{
			System.out.println("════════════════════════════════════════════════════════════════");
			System.out.println("📋 Configuration Health Check");
			System.out.println("════════════════════════════════════════════════════════════════");

			if (healthy)
			{
				System.out.println("✅ All required properties are present");
				System.out.println();
				System.out.println("Validated properties:");
				validatedProperties.forEach((key, value) ->
						System.out.printf("  %-30s = %s%n", key, value));
			}
			else
			{
				System.out.println("❌ Configuration validation FAILED");
				System.out.println();
				System.out.println("Errors:");
				errors.forEach(error -> System.out.println("  ❌ " + error));
			}

			if (!warnings.isEmpty())
			{
				System.out.println();
				System.out.println("Warnings:");
				warnings.forEach(warning -> System.out.println("  ⚠️  " + warning));
			}

			System.out.println("════════════════════════════════════════════════════════════════");
		}

		/**
		 * Throw an exception if validation failed.
		 */
		public void throwIfUnhealthy()
		{
			if (!healthy)
			{
				throw new IllegalStateException(
						"Configuration validation failed:\n  " + String.join("\n  ", errors));
			}
		}
	}

	/**
	 * Main method for standalone validation.
	 */
	public static void main(String[] args)
	{
		ConfigHealthCheck check = new ConfigHealthCheck();
		Result result = check.validate();
		result.printReport();
		System.exit(result.isHealthy() ? 0 : 1);
	}
}
