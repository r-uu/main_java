package de.ruu.app.jeeeraaah.frontend.api.client.ws.rs;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsCommon.PATH_JEEERAAAH_ROOT;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsCommon.TOKEN_BY_ID;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTaskGroup.TOKEN_ALL_FLAT;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTaskGroup.TOKEN_DOMAIN;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTaskGroup.TOKEN_REMOVE_TASK_FROM_GROUP;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTaskGroup.TOKEN_WITH_TASKS;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTaskGroup.TOKEN_WITH_TASKS_AND_DIRECT_NEIGHBOURS;
import static de.ruu.app.jeeeraaah.common.api.mapping.Mappings.toBean;
import static de.ruu.app.jeeeraaah.common.api.mapping.Mappings.toDTO;
import static jakarta.ws.rs.client.Entity.entity;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.MOXY_JSON_FEATURE_DISABLE;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.ConfigProvider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;

import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.domain.PathsTaskGroup;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupFlat;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupService;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTO;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTOFlat;
import de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.auth.AuthorizationHeaderFilter;
import de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.auth.KeycloakAuthService;
import de.ruu.lib.cdi.common.CDIUtil;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import de.ruu.lib.util.AbstractEvent;
import de.ruu.lib.ws.rs.ErrorResponse;
import de.ruu.lib.ws.rs.NonTechnicalException;
import de.ruu.lib.ws.rs.SessionExpiredException;
import de.ruu.lib.ws.rs.TechnicalException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Singleton @Slf4j
public class TaskGroupServiceClient implements TaskGroupService<TaskGroupBean>
{
	// error message constants
	private static final String UNEXPECTED_STATUS = "unexpected status: ";

	private final String scheme = ConfigProvider.getConfig().getOptionalValue("jeeeraaah.rest-api.scheme", String.class)
			.orElse("http");

	private final String host =
			// ConfigProvider.getConfig().getOptionalValue("jeeeraaah.rest-api.host" , String.class).orElse("127.0.0.1");
			ConfigProvider.getConfig().getOptionalValue("jeeeraaah.rest-api.host", String.class).orElse("localhost");

	private final Integer port = ConfigProvider.getConfig().getOptionalValue("jeeeraaah.rest-api.port", Integer.class)
			.orElse(9080);

	/**
	 * Keycloak authentication service for JWT token management.
	 * Provides login, token refresh, and logout functionality.
	 * Injected as CDI singleton - all service clients share the same instance.
	 */
	@Inject
	private KeycloakAuthService authService;

	private Client client;

	private String baseURL;

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// lifecycle methods
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a properly configured ObjectMapper that works with Lombok and Java 8+ features.
	 *
	 * @return Configured ObjectMapper instance
	 */
	private ObjectMapper createObjectMapper()
	{
		return new ObjectMapper().registerModule(new Jdk8Module()) // for Java 8 Optional support
				.registerModule(new JavaTimeModule()) // for java 8 Date/Time support
				.disable(FAIL_ON_EMPTY_BEANS) // don't fail on empty beans
				.disable(FAIL_ON_UNKNOWN_PROPERTIES) // don't fail on unknown properties
				.disable(WRITE_DATES_AS_TIMESTAMPS); // write dates as ISO-8601 strings
	}

	@PostConstruct public void postConstruct()
	{
		String schemeHostPort = scheme + "://" + host + ":" + port;

		baseURL = schemeHostPort + PATH_JEEERAAAH_ROOT;

		log.debug("scheme        : {}", scheme);
		log.debug("host          : {}", host);
		log.debug("port          : {}", port);
		log.debug("schemeHostPort: {}", schemeHostPort);
		log.debug("root          : {}", PathsTaskGroup.PATH_DOMAIN);
		log.debug("baseURL       : {}", baseURL);

// Create a properly configured ObjectMapper for JSON serialization/deserialization
		// Supports Java 8 Optional, LocalDate/LocalDateTime, and Lombok-generated code
		ObjectMapper objectMapper = createObjectMapper();

		// Create a JacksonJsonProvider with our custom ObjectMapper
		// This ensures consistent JSON handling across all REST calls
		JacksonJsonProvider jacksonJsonProvider = new JacksonJsonProvider(objectMapper);

		// Build JAX-RS client with registered providers and configuration
		client = ClientBuilder.newBuilder()
				// Register JSON provider for request/response serialization
				.register(jacksonJsonProvider)
				// ✅ Register Keycloak authentication filter for automatic token injection
				// Must use instance (not class) because Jersey HK2 cannot inject CDI beans
				// See AuthorizationHeaderFilter for details on automatic token handling
				.register(new AuthorizationHeaderFilter(authService))
				// Configure connection timeouts
				.property(CONNECT_TIMEOUT, 5000)   // 5 seconds to establish connection
				.property(READ_TIMEOUT, 15000)     // 15 seconds to read response
				// Disable MOXy JSON feature (we use Jackson instead)
				.property(MOXY_JSON_FEATURE_DISABLE, true)
				.build();

		// log registered jakarta providers for debugging
		List<String> classNames = new ArrayList<>();
		client.getConfiguration().getClasses().forEach(c -> classNames.add(c.getName()));
		log.debug("jakarta ws.rs client configuration classes\n{}", String.join("\n", classNames));
	}

	@PreDestroy public void preDestroy()
	{
		if (client != null) client.close();
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// interface implementations
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public @NonNull TaskGroupBean create(@NonNull TaskGroupBean taskGroup)
			throws TechnicalException, NonTechnicalException
	{
		WebTarget webTarget = client.target(baseURL).path(TOKEN_DOMAIN);
		log.debug("webTarget: {}", webTarget);
		// Execute POST request with automatic Keycloak token handling (injection + refresh on 401)
		try (Response response = executeWithAuth(webTarget,
				requestBuilder -> requestBuilder.post(entity(toDTO(taskGroup, newContext()), APPLICATION_JSON))))
		{
			if (response.getStatusInfo().getFamily() == SUCCESSFUL)
			{
				TaskGroupDTO dto = response.readEntity(TaskGroupDTO.class);
				TaskGroupBean result = toBean(dto, newContext());
				// fire event to indicate that a new task group has been created in the backend
				CDIUtil.fire(new TaskGroupCreatedInBackendEvent(this, result));
				return result;
			}
			else
				// handle business error (payload known)
				throw newNonTechnicalException(response);
		}
		catch (ProcessingException e)
		{
			// this is thrown for technical issues (server down, wrong URL, timeout)
			throw new TechnicalException("communication error", e);
		}
	}

	@Override
	public Optional<TaskGroupBean> read(@NonNull Long id) throws TechnicalException, NonTechnicalException
	{
		WebTarget webTarget = client.target(baseURL).path(TOKEN_DOMAIN).path(TOKEN_BY_ID).resolveTemplate("id", id);
		log.debug("webTarget: {}", webTarget);
		// Execute GET request with automatic Keycloak token handling
		try (Response response = executeWithAuth(webTarget, requestBuilder -> requestBuilder.get()))
		{
			int status = response.getStatus();

			if (status == Status.OK.getStatusCode())
			{
				TaskGroupDTO dto = response.readEntity(TaskGroupDTO.class);
				return Optional.of(toBean(dto, newContext()));
			}
			else if (status == NOT_FOUND.getStatusCode())
				return Optional.empty();
			else
				// handle business error (payload known)
				throw newNonTechnicalException(response);
		}
		catch (ProcessingException e)
		{
			// this is thrown for technical issues (server down, wrong URL, timeout)
			throw new TechnicalException("communication error", e);
		}
	}

	@Override
	public @NonNull TaskGroupBean update(@NonNull TaskGroupBean taskGroup)
			throws TechnicalException, NonTechnicalException
	{
		WebTarget webTarget = client.target(baseURL).path(TOKEN_DOMAIN);
		log.debug("webTarget: {}", webTarget);
		// Execute PUT request with automatic Keycloak token handling
		try (Response response = executeWithAuth(webTarget,
				requestBuilder -> requestBuilder.put(entity(toDTO(taskGroup, newContext()), APPLICATION_JSON))))
		{
			if (response.getStatusInfo().getFamily() == SUCCESSFUL)
			{
				TaskGroupDTO dto = response.readEntity(TaskGroupDTO.class);
				TaskGroupBean result = toBean(dto, newContext());
				// fire event to indicate that a task group has been updated in the backend
				CDIUtil.fire(new TaskGroupUpdatedInBackendEvent(this, result));
				return result;
			}
			else
				// handle business error (payload known)
				throw new NonTechnicalException(response.readEntity(ErrorResponse.class));
		}
		catch (ProcessingException e)
		{
			// this is thrown for technical issues (server down, wrong URL, timeout)
			throw new TechnicalException("communication error", e);
		}
	}

	@Override
	public void delete(@NonNull Long id) throws TechnicalException, NonTechnicalException
	{
		WebTarget webTarget = client.target(baseURL).path(TOKEN_DOMAIN).path(TOKEN_BY_ID).resolveTemplate("id", id);
		log.debug("webTarget: {}", webTarget);
		// Execute DELETE request with automatic Keycloak token handling
		try (Response response = executeWithAuth(webTarget, requestBuilder -> requestBuilder.delete()))
		{
			if (response.getStatusInfo().getFamily() == SUCCESSFUL)
			{
				// fire event to indicate that a task group has been deleted in the backend
				CDIUtil.fire(new TaskGroupDeletedInBackendEvent(this, id));
			}
			else if (response.getStatus() != NOT_FOUND.getStatusCode())
				// Only throw an exception if it's not a 404 (Not Found)
				throw newNonTechnicalException(response);
			else
				// handle business error (payload known)
				throw newNonTechnicalException(response);
		}
		catch (ProcessingException e)
		{
			// this is thrown for technical issues (server down, wrong URL, timeout)
			throw new TechnicalException("communication error", e);
		}
	}

	@Override
	public Optional<TaskGroupBean> findWithTasks(@NonNull Long id) throws TechnicalException, NonTechnicalException
	{
		WebTarget webTarget = client.target(baseURL).path(TOKEN_DOMAIN).path(TOKEN_WITH_TASKS).path(TOKEN_BY_ID)
				.resolveTemplate("id", id);
		log.debug("web target: {}", webTarget);
		// Execute GET request with automatic Keycloak token handling
		try (Response response = executeWithAuth(webTarget, requestBuilder -> requestBuilder.accept(APPLICATION_JSON).get()))
		{
			log.debug("response: {}", response);
			if (response.getStatusInfo().getFamily() == SUCCESSFUL)
				return Optional.of(toBean(response.readEntity(TaskGroupDTO.class), newContext()));
			else if (response.getStatus() == NOT_FOUND.getStatusCode())
				return Optional.empty();
			else
			{
				// handle error response
				String errorJson = response.readEntity(String.class);
				log.error("error response\n{}", errorJson);
				try
				{
					ErrorResponse error = createObjectMapper().readValue(errorJson, ErrorResponse.class);
					throw new NonTechnicalException(error);
				}
				catch (Exception e)
				{
					// If we can't parse the error response, throw a generic error
					throw new TechnicalException("failed to parse error response: " + errorJson, e);
				}
			}
		}
		catch (Exception e)
		{
			log.error("error finding task group with tasks", e);
			throw new TechnicalException("error finding task group with tasks", e);
		}
	}

	@Override
	public Optional<TaskGroupBean> findWithTasksAndDirectNeighbours(@NonNull Long id)
			throws TechnicalException, NonTechnicalException
	{
		WebTarget webTarget = client.target(baseURL).path(TOKEN_DOMAIN).path(TOKEN_WITH_TASKS_AND_DIRECT_NEIGHBOURS)
				.path(TOKEN_BY_ID).resolveTemplate("id", id);
		log.debug("web target: {}", webTarget);
		// Execute GET request with automatic Keycloak token handling
		try (Response response = executeWithAuth(webTarget, requestBuilder -> requestBuilder.get()))
		{
			if (response.getStatusInfo().getFamily() == SUCCESSFUL)
			{
				TaskGroupDTO dto = response.readEntity(TaskGroupDTO.class);
				return Optional.of(toBean(dto, newContext()));
			}
			else if (response.getStatus() == NOT_FOUND.getStatusCode())
				return Optional.empty();
			else
				// handle business error (payload known)
				throw new NonTechnicalException(response.readEntity(ErrorResponse.class));
		}
		catch (ProcessingException e)
		{
			// this is thrown for technical issues (server down, wrong URL, timeout)
			throw new TechnicalException("communication error", e);
		}
	}

	@Override
	public @NonNull Set<TaskGroupFlat> findAllFlat() throws TechnicalException, NonTechnicalException
	{
		WebTarget webTarget = client.target(baseURL).path(TOKEN_DOMAIN).path(TOKEN_ALL_FLAT);
		log.debug("Retrieving all flat task groups from: {}", webTarget.getUri());

		// Execute GET request with automatic Keycloak token handling
		try (Response response = executeWithAuth(webTarget, requestBuilder -> requestBuilder.accept(APPLICATION_JSON).get()))
		{
			if (response.getStatusInfo().getFamily() == SUCCESSFUL)
			{
				// read the response as a string first to log it
				String json = response.readEntity(String.class);
				log.debug("Received task groups JSON response:\n{}", json);
				Set<TaskGroupDTOFlat> result = createObjectMapper().readValue(json, new TypeReference<Set<TaskGroupDTOFlat>>() {
				});
				log.info("Successfully retrieved {} task groups", result.size());
				return result.stream().map(dto -> (TaskGroupFlat) dto).collect(Collectors.toSet());
			}
			else
			{
				String errorMsg = response.readEntity(String.class);
				String detailedError = String.format(
					"Failed to retrieve task groups from backend\n  Status: %d %s\n  URL: %s\n  Error: %s",
					response.getStatus(),
					response.getStatusInfo().getReasonPhrase(),
					webTarget.getUri(),
					errorMsg
				);
				log.error(detailedError);
				throw new TechnicalException(detailedError);
			}
		}
		catch (TechnicalException e)
		{
			// Re-throw TechnicalException from executeWithAuth (already has detailed message)
			throw e;
		}
		catch (Exception e)
		{
			String detailedError = String.format(
				"Unexpected error while retrieving task groups\n  URL: %s\n  Error type: %s\n  Message: %s",
				webTarget.getUri(),
				e.getClass().getSimpleName(),
				e.getMessage()
			);
			log.error(detailedError, e);
			throw new TechnicalException(detailedError, e);
		}
	}

	// @Override public Optional<TaskGroupLazy> findLazy(@NonNull Long id)
	// throws TechnicalException, NonTechnicalException
	// {
	// WebTarget webTarget = client.target(PathsTaskGroup.PATH_LAZY);
	// try (Response response = webTarget.resolveTemplate("id", id).request().get())
	// {
	// if (response.getStatusInfo().getFamily() == SUCCESSFUL)
	// {
	// return Optional.of(response.readEntity(TaskGroupLazy.class));
	// }
	// else
	// // handle business error (payload known)
	// throw new NonTechnicalException(response.readEntity(ErrorResponse.class));
	// }
	// catch (ProcessingException e)
	// {
	// // this is thrown for technical issues (server down, wrong URL, timeout)
	// throw new TechnicalException("communication error", e);
	// }
	// }

	@Override
	public void removeTaskFromGroup(@NonNull Long idGroup, @NonNull Long idTask)
			throws TechnicalException, NonTechnicalException
	{
		WebTarget webTarget = client.target(baseURL).path(TOKEN_DOMAIN).path(TOKEN_REMOVE_TASK_FROM_GROUP)
				.resolveTemplate("idGroup", idGroup).resolveTemplate("idTask", idTask);
		log.debug("web target: {}", webTarget);
		// Execute GET request with automatic Keycloak token handling
		try (Response response = executeWithAuth(webTarget, requestBuilder -> requestBuilder.get()))
		{
			throwExceptionForNoSuccessInRelationalOperationResponse(response);
			// fire event to indicate that the task has been removed from the group in the backend
			CDIUtil.fire(new RemoveFromGroupInBackendEvent(this, new TaskGroupTaskRelationInfo(idGroup, idTask)));
		}
		catch (ProcessingException e)
		{
			// this is thrown for technical issues (server down, wrong URL, timeout)
			throw new TechnicalException("communication error", e);
		}
	}

	private void throwExceptionForNoSuccessInRelationalOperationResponse(Response response)
			throws TechnicalException, NonTechnicalException
	{
		if (response.getStatusInfo().getFamily() == SUCCESSFUL)
			return;

		String raw = null;
		try
		{
			raw = response.readEntity(String.class);
		}
		catch (Exception ignore)
		{
		}
		log.error("HTTP {} {} - response body:\n{}", response.getStatus(), response.getStatusInfo().getReasonPhrase(), raw);
		try
		{
			if (raw != null && !raw.isBlank())
			{
				ErrorResponse er = createObjectMapper().readValue(raw, ErrorResponse.class);
				throw new NonTechnicalException(er);
			}
			else
			{
				throw new NonTechnicalException(
						new ErrorResponse("HTTP " + response.getStatus(), response.getStatusInfo().getReasonPhrase()));
			}
		}
		catch (ProcessingException | com.fasterxml.jackson.core.JsonProcessingException e)
		{
			// this is thrown for technical issues (server down, wrong URL, timeout)
			throw new TechnicalException("communication error", e);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Keycloak authentication helper methods
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Executes an HTTP request with automatic Keycloak authentication.
	 * 
	 * <p><strong>Features:</strong></p>
	 * <ul>
	 *   <li>Automatically injects {@code Authorization: Bearer <token>} header</li>
	 *   <li>Handles token expiry: On 401 Unauthorized, refreshes token and retries request</li>
	 *   <li>Centralized error handling for authentication failures</li>
	 * </ul>
	 * 
	 * <p><strong>Flow:</strong></p>
	 * <ol>
	 *   <li>Get current access token from {@link KeycloakAuthService}</li>
	 *   <li>Add {@code Authorization} header to request</li>
	 *   <li>Execute request via provided {@code requestExecutor}</li>
	 *   <li>If 401 Unauthorized: Token expired → refresh token and retry once</li>
	 *   <li>If refresh fails or second 401: User must re-login</li>
	 * </ol>
	 * 
	 * <p><strong>Usage examples:</strong></p>
	 * <pre>
	 * // GET request
	 * Response response = executeWithAuth(webTarget, builder -&gt; builder.get());
	 * 
	 * // POST request
	 * Response response = executeWithAuth(webTarget, 
	 *     builder -&gt; builder.post(entity(dto, APPLICATION_JSON)));
	 * 
	 * // PUT request
	 * Response response = executeWithAuth(webTarget, 
	 *     builder -&gt; builder.put(entity(dto, APPLICATION_JSON)));
	 * 
	 * // DELETE request
	 * Response response = executeWithAuth(webTarget, builder -&gt; builder.delete());
	 * </pre>
	 * 
	 * <p><strong>Error scenarios:</strong></p>
	 * <ul>
	 *   <li><strong>401 on first call:</strong> Token expired → automatic refresh + retry</li>
	 *   <li><strong>401 after refresh:</strong> Refresh token expired → logout + exception</li>
	 *   <li><strong>403 Forbidden:</strong> User lacks required role → returned to caller</li>
	 *   <li><strong>Network errors:</strong> Server unreachable → ProcessingException</li>
	 * </ul>
	 * 
	 * @param webTarget      The JAX-RS WebTarget (URL + path parameters)
	 * @param requestExecutor Lambda that executes the actual HTTP call (GET/POST/PUT/DELETE)
	 * @return HTTP Response (caller must close it with try-with-resources)
	 * @throws TechnicalException if authentication refresh fails or network error occurs
	 */
	private Response executeWithAuth(WebTarget webTarget, RequestExecutor requestExecutor) throws TechnicalException
	{
		try
		{
			// STEP 1: Execute request with current access token
			// The Authorization header is automatically added by AuthorizationHeaderFilter
			Response response = requestExecutor.execute(webTarget.request());

			// STEP 2: Check if token expired (HTTP 401 Unauthorized)
			if (response.getStatus() == 401)
			{
				log.debug("Access token expired (401 Unauthorized), attempting token refresh...");
				response.close(); // Important: Close first response before retry

				try
				{
					// STEP 3: Refresh access token using refresh token
					String newToken = authService.refreshAccessToken();
					log.debug("Token refreshed successfully, retrying request...");

					// STEP 4: Retry request with new access token
					// AuthorizationHeaderFilter will use the new token automatically
					response = requestExecutor.execute(webTarget.request());

					// STEP 5: If still 401 after refresh → refresh token also expired
					if (response.getStatus() == 401)
					{
						log.error("Authentication failed even after token refresh. Refresh token expired. Re-login required.");
						authService.logout(); // Clear local tokens
						throw new SessionExpiredException("Your session has expired due to inactivity. Please login again to continue.");
					}
				}
				catch (SessionExpiredException e)
				{
					// Re-throw SessionExpiredException directly so UI can handle it
					throw e;
				}
				catch (Exception e)
				{
					// Token refresh failed (network error, invalid refresh token, etc.)
					log.error("Token refresh failed", e);
					authService.logout(); // Clear local tokens
					throw new SessionExpiredException("Your session has expired. Please login again to continue.", e);
				}
			}

			return response;
		}
		catch (ProcessingException e)
		{
			// Network error: server unreachable, timeout, connection refused, etc.
			String detailedMessage = buildDetailedErrorMessage(webTarget, e);
			log.error("Communication error: {}", detailedMessage, e);
			throw new TechnicalException(detailedMessage, e);
		}
	}

	/**
	 * Builds a detailed error message for communication failures.
	 *
	 * @param webTarget the target URL that was being accessed
	 * @param e the exception that occurred
	 * @return a detailed error message
	 */
	private String buildDetailedErrorMessage(WebTarget webTarget, Exception e)
	{
		StringBuilder msg = new StringBuilder("Backend communication failed");

		// Add target URL
		if (webTarget != null)
		{
			msg.append("\n  Target URL: ").append(webTarget.getUri());
		}

		// Analyze exception type for specific guidance
		String causeMsg = e.getMessage();
		Throwable cause = e.getCause();

		if (cause instanceof java.net.SocketTimeoutException)
		{
			msg.append("\n  Problem: Connection timeout - backend did not respond in time");
			msg.append("\n  Possible causes:");
			msg.append("\n    - Backend server is not running");
			msg.append("\n    - Backend is overloaded or performing slow operations");
			msg.append("\n    - Network connectivity issues");
			msg.append("\n  Suggested actions:");
			msg.append("\n    - Check if Liberty server is running: ps aux | grep liberty");
			msg.append("\n    - Check Liberty logs for errors");
			msg.append("\n    - Verify backend is accessible: curl ").append(baseURL).append("/health");
		}
		else if (cause instanceof java.net.ConnectException)
		{
			msg.append("\n  Problem: Connection refused - cannot reach backend");
			msg.append("\n  Possible causes:");
			msg.append("\n    - Backend server is not running");
			msg.append("\n    - Wrong port configuration");
			msg.append("\n    - Firewall blocking connection");
			msg.append("\n  Suggested actions:");
			msg.append("\n    - Start Liberty server: mvn liberty:dev");
			msg.append("\n    - Check if port 9080 is listening: ss -tlnp | grep 9080");
			msg.append("\n    - Verify configuration in microprofile-config.properties");
		}
		else if (cause instanceof java.net.UnknownHostException)
		{
			msg.append("\n  Problem: Cannot resolve hostname");
			msg.append("\n  Possible causes:");
			msg.append("\n    - DNS issue or invalid hostname");
			msg.append("\n    - Wrong backend URL configuration");
			msg.append("\n  Suggested actions:");
			msg.append("\n    - Check backend.url in microprofile-config.properties");
			msg.append("\n    - Try using localhost or 127.0.0.1 instead of hostname");
		}
		else
		{
			msg.append("\n  Problem: ").append(causeMsg != null ? causeMsg : e.getClass().getSimpleName());
			if (cause != null)
			{
				msg.append("\n  Cause: ").append(cause.getClass().getSimpleName()).append(": ").append(cause.getMessage());
			}
		}

		return msg.toString();
	}

	/**
	 * Functional interface for flexible HTTP request execution.
	 * 
	 * <p>Allows different HTTP methods (GET, POST, PUT, DELETE) to be passed as lambda expressions
	 * to the {@link #executeWithAuth(WebTarget, RequestExecutor)} method.</p>
	 * 
	 * <p><strong>Design pattern:</strong> Strategy pattern - encapsulates the "how" of request execution,
	 * while {@code executeWithAuth()} handles the "what" (authentication logic).</p>
	 */
	@FunctionalInterface
	private interface RequestExecutor
	{
		/**
		 * Executes an HTTP request using the provided request builder.
		 * 
		 * <p>The request builder already has the {@code Authorization} header set by
		 * {@link #buildAuthenticatedRequest(WebTarget)}.</p>
		 * 
		 * @param requestBuilder JAX-RS Invocation.Builder with authentication header
		 * @return HTTP Response
		 */
		Response execute(jakarta.ws.rs.client.Invocation.Builder requestBuilder);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// utility methods
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private ReferenceCycleTracking newContext()
	{
		return new ReferenceCycleTracking();
	}

	// event classes
	public static class TaskGroupCreatedInBackendEvent extends AbstractEvent<TaskGroupServiceClient, TaskGroupBean>
	{
		public TaskGroupCreatedInBackendEvent(TaskGroupServiceClient source, @NonNull TaskGroupBean taskGroup) {
			super(source, taskGroup);
		}
	}

	public static class TaskGroupUpdatedInBackendEvent extends AbstractEvent<TaskGroupServiceClient, TaskGroupBean>
	{
		public TaskGroupUpdatedInBackendEvent(TaskGroupServiceClient source, @NonNull TaskGroupBean taskGroup) {
			super(source, taskGroup);
		}
	}

	public static class TaskGroupDeletedInBackendEvent extends AbstractEvent<TaskGroupServiceClient, Long>
	{
		public TaskGroupDeletedInBackendEvent(TaskGroupServiceClient source, @NonNull Long id) {
			super(source, id);
		}
	}

	public static class RemoveFromGroupInBackendEvent
			extends AbstractEvent<TaskGroupServiceClient, TaskGroupTaskRelationInfo>
	{
		public RemoveFromGroupInBackendEvent(TaskGroupServiceClient source, @NonNull TaskGroupTaskRelationInfo info) {
			super(source, info);
		}
	}

	@Accessors(fluent = true) @lombok.Data @lombok.AllArgsConstructor
	public static class TaskGroupTaskRelationInfo
	{
		@NonNull
		private final Long taskGroupId;
		@NonNull
		private final Long taskId;
	}

	private NonTechnicalException newNonTechnicalException(Response response)
	{
		String body;
		try
		{
			body = response.readEntity(String.class);
		}
		catch (Exception ignore)
		{
			body = null;
		}
		log.error("HTTP {} {} - response body:\n{}", response.getStatus(), response.getStatusInfo().getReasonPhrase(),
				body);
		try
		{
			if (body != null && !body.isBlank())
			{
				ErrorResponse error = createObjectMapper().readValue(body, ErrorResponse.class);
				return new NonTechnicalException(error);
			}
		}
		catch (Exception ignore)
		{
		}
		String message = (body == null || body.isBlank()) ? response.getStatusInfo().getReasonPhrase() : body;
		return new NonTechnicalException(new ErrorResponse("HTTP " + response.getStatus(), message));
	}
}
