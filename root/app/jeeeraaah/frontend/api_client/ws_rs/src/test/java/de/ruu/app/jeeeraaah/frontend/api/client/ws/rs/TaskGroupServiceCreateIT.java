package de.ruu.app.jeeeraaah.frontend.api.client.ws.rs;

import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.auth.KeycloakAuthService;
import de.ruu.lib.cdi.common.CDIExtension;
import de.ruu.lib.cdi.se.CDIContainer;
import de.ruu.lib.junit.DisabledOnServerNotListening;
import de.ruu.lib.ws_rs.NonTechnicalException;
import de.ruu.lib.ws_rs.TechnicalException;
import java.io.IOException;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static java.util.Objects.nonNull;

/**
 * Integration test for TaskGroupService create endpoint (client).
 * <p>
 * Authenticates with Keycloak using testuser/testpassword before calling the
 * protected REST endpoint.
 * <p>
 * <b>Prerequisites:</b> Keycloak running, realm 'jeeeraaah-realm' configured
 * (run: {@code ruu-kc-setup}), backend on localhost:9080.
 */
@DisabledOnServerNotListening(propertyNameHost = "jeeeraaah.rest-api.host", propertyNamePort = "jeeeraaah.rest-api.port")
@Slf4j
class TaskGroupServiceCreateIT
{
        private static final String TEST_USERNAME = "test";
        private static final String TEST_PASSWORD = "test";

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
                        CDIContainer.bootstrap(TaskGroupServiceCreateIT.class.getClassLoader());
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
                log.debug("initialised keycloakAuthService   : {}", nonNull(keycloakAuthService));
        }

        @Test void testCreateTaskGroup() throws NonTechnicalException, TechnicalException, IOException, InterruptedException
        {
                // authenticate with Keycloak first
                log.info("=== Authenticating user '{}' with Keycloak ===", TEST_USERNAME);
                String accessToken = keycloakAuthService.login(TEST_USERNAME, TEST_PASSWORD);
                assertThat(accessToken).as("access token").isNotNull().isNotEmpty();
                log.info("✅ Authentication successful");

                // prepare DTO
                String name = "test-create-" + System.currentTimeMillis();
                TaskGroupBean dto = new TaskGroupBean(name);

                // call backend via client — AuthorizationHeaderFilter injects the Bearer token
                TaskGroupBean created = taskGroupServiceClient.create(dto);

                // assertions
                assertThat(created).as("created result").isNotNull();
                assertThat(created.id()).as("created id").isNotNull();
                assertThat(created.name()).as("created name").isEqualTo(name);

                log.info("✅ TaskGroup created: id={}, name={}", created.id(), created.name());
        }
}