package de.ruu.app.jeeeraaah.frontend.api.client.ws.rs;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ruu.app.jeeeraaah.common.api.domain.flat.TaskGroupFlat;
import de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.auth.KeycloakAuthService;
import de.ruu.lib.cdi.common.CDIExtension;
import de.ruu.lib.cdi.se.CDIContainer;
import de.ruu.lib.junit.DisabledOnServerNotListening;
import de.ruu.lib.ws_rs.NonTechnicalException;
import de.ruu.lib.ws_rs.TechnicalException;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Integration test for TaskGroupService findAllFlat endpoint.
 * 
 * <p>This test verifies the complete authentication and authorization flow:
 * <ol>
 *   <li>Authenticates user r-uu with Keycloak</li>
 *   <li>Obtains JWT access token containing roles</li>
 *   <li>Calls TaskGroupService.findAllFlat() with Bearer token</li>
 *   <li>Verifies successful response</li>
 * </ol>
 * 
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>Keycloak server running on localhost:8080</li>
 *   <li>Realm 'jeeeraaah-realm' configured</li>
 *   <li>User 'r-uu' with password 'r-uu-password'</li>
 *   <li>User has 'taskgroup-read' role assigned</li>
 *   <li>Open Liberty backend running on localhost:9080</li>
 * </ul>
 */
@DisabledOnServerNotListening(propertyNameHost = "jeeeraaah.rest-api.host", propertyNamePort = "jeeeraaah.rest-api.port")
@Slf4j
class TaskGroupServiceAllFlatIT
{
	private static final String TEST_USERNAME = "r-uu";
	private static final String TEST_PASSWORD = "r-uu-password";
	
	private static SeContainer seContainer;

	@Inject private TaskGroupServiceClient taskGroupServiceClient;
	@Inject private KeycloakAuthService    keycloakAuthService;

	@BeforeAll static void beforeAll()
	{
		log.debug("initialising CDI SE container for tests");
		try
		{
			seContainer =
					SeContainerInitializer
							.newInstance()
							.addExtensions(CDIExtension.class)
							.initialize();
			CDIContainer.bootstrap(TaskGroupServiceAllFlatIT.class.getClassLoader());
		}
		catch (Exception e)
		{
			log.error("failure initialising seContainer", e);
		}
		log.debug("cdi container initialisation {}", seContainer == null ? "unsuccessful" : "successful");
	}

	@BeforeEach void beforeEach()
	{
		// obtain client and auth service from CDI
		taskGroupServiceClient = CDI.current().select(TaskGroupServiceClient.class).get();
		keycloakAuthService    = CDI.current().select(KeycloakAuthService   .class).get();
		
		log.debug("initialised taskGroupServiceClient: {}", nonNull(taskGroupServiceClient));
		log.debug("initialised keycloakAuthService   : {}", nonNull(keycloakAuthService   ));
	}

	/**
	 * Tests the complete flow from authentication to authorized endpoint access.
	 * 
	 * <p>This test exercises:
	 * <ul>
	 *   <li>Keycloak authentication via Resource Owner Password Credentials Grant</li>
	 *   <li>JWT token containing realm_access.roles claim with 'taskgroup-read' role</li>
	 *   <li>Liberty MicroProfile JWT validation and role extraction</li>
	 *   <li>@RolesAllowed("taskgroup-read") authorization on findAllFlat() endpoint</li>
	 * </ul>
	 * 
	 * <p><b>Expected Behavior:</b>
	 * <ul>
	 *   <li>Login succeeds and returns valid access token</li>
	 *   <li>Token is automatically attached to REST request via AuthorizationHeaderFilter</li>
	 *   <li>Liberty extracts 'taskgroup-read' role from realm_access.roles claim</li>
	 *   <li>@RolesAllowed("taskgroup-read") permits access</li>
	 *   <li>findAllFlat() returns Set<TaskGroupFlat> (empty or populated)</li>
	 * </ul>
	 * 
	 * <p><b>Debugging Notes:</b>
	 * <ul>
	 *   <li>If test fails with 401 Unauthorized: Check Keycloak user credentials</li>
	 *   <li>If test fails with 403 Forbidden: Check user has 'taskgroup-read' role</li>
	 *   <li>If test fails with 500 Internal Server Error with "Unauthorized" cause:
	 *       <ul>
	 *         <li>Liberty may not be reading roles from correct JWT claim</li>
	 *         <li>Check mp.jwt.claim.groups configuration in microprofile-config.properties</li>
	 *         <li>Verify JWT token contains realm_access.roles claim with taskgroup-read</li>
	 *         <li>Check Liberty messages.log for JWT validation errors</li>
	 *       </ul>
	 *   </li>
	 * </ul>
	 */
	@Test void testFindAllFlatWithAuthentication() throws IOException, InterruptedException, NonTechnicalException, TechnicalException
	{
		// Step 1: Authenticate with Keycloak
		log.info("=== Step 1: Authenticating user '{}' with Keycloak ===", TEST_USERNAME);
		String accessToken = keycloakAuthService.login(TEST_USERNAME, TEST_PASSWORD);
		
		assertThat(accessToken).as("access token should not be null").isNotNull();
		assertThat(accessToken).as("access token should not be empty").isNotEmpty();

		log.info("✅ Authentication successful");
		log.info("   Access token length: {} characters", accessToken.length());
		log.info("   Access token prefix: {} ...", accessToken.substring(0, Math.min(50, accessToken.length())));
		
		// Step 2: Call findAllFlat() endpoint
		// The AuthorizationHeaderFilter will automatically add: Authorization: Bearer <token>
		log.info("=== Step 2: Calling TaskGroupService.findAllFlat() ===");
		log.info("   Expected: Liberty validates JWT and extracts 'taskgroup-read' role from realm_access.roles");
		log.info("   Expected: @RolesAllowed(\"taskgroup-read\") permits access");
		
		Set<TaskGroupFlat> result = taskGroupServiceClient.findAllFlat();
		
		// Step 3: Verify response
		log.info("=== Step 3: Verifying response ===");
		assertThat(result).as("result should not be null").isNotNull();
		// Don't assert empty/non-empty - could be either depending on DB state
		
		log.info("✅ findAllFlat() call succeeded");
		log.info("   Returned {} task groups", result.size());
		
		if (!result.isEmpty())
		{
			log.info("   First task group: {}", result.iterator().next());
		}
		else
		{
			log.info("   Database is empty (no task groups found)");
		}
		
		log.info("=== ✅ TEST PASSED: Complete authentication and authorization flow works! ===");
	}
}