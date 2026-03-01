package de.ruu.app.jeeeraaah.frontend.api.client.ws.rs;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsCommon.PATH_JEEERAAAH_ROOT;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsCommon.TOKEN_BY_ID;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTask.TOKEN_ADD_PREDECESSOR;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTask.TOKEN_ADD_SUB;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTask.TOKEN_ADD_SUCCESSOR;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTask.TOKEN_ALL;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTask.TOKEN_BY_ID_WITH_RELATED;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTask.TOKEN_DOMAIN;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTask.TOKEN_REMOVE_NEIGHBOURS_FROM_TASK;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTask.TOKEN_REMOVE_PREDECESSOR;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTask.TOKEN_REMOVE_SUB;
import static de.ruu.app.jeeeraaah.common.api.domain.PathsTask.TOKEN_REMOVE_SUCCESSOR;
import static de.ruu.app.jeeeraaah.common.api.mapping.Mappings.toBean;
import static de.ruu.app.jeeeraaah.common.api.mapping.Mappings.toDTO;
import static de.ruu.app.jeeeraaah.common.api.mapping.Mappings.toLazy;
import static jakarta.ws.rs.client.Entity.entity;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.MOXY_JSON_FEATURE_DISABLE;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.ConfigProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.domain.InterTaskRelationData;
import de.ruu.app.jeeeraaah.common.api.domain.PathsTask;
import de.ruu.app.jeeeraaah.common.api.domain.RemoveNeighboursFromTaskConfig;
import de.ruu.app.jeeeraaah.common.api.domain.TaskService;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskCreationData;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTOLazy;
import de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.auth.AuthorizationHeaderFilter;
import de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.auth.KeycloakAuthService;
import de.ruu.lib.cdi.common.CDIUtil;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import de.ruu.lib.util.AbstractEvent;
import de.ruu.lib.ws_rs.ErrorResponse;
import de.ruu.lib.ws_rs.NonTechnicalException;
import de.ruu.lib.ws_rs.SessionExpiredException;
import de.ruu.lib.ws_rs.TechnicalException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Singleton @Slf4j
public class TaskServiceClient implements TaskService<TaskGroupBean, TaskBean>
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
		return
				new ObjectMapper()
						.registerModule(new Jdk8Module())     // for Java 8 Optional support
						.registerModule(new JavaTimeModule()) // for java 8 Date/Time support
						.disable(FAIL_ON_EMPTY_BEANS)         // don't fail on empty beans
						.disable(FAIL_ON_UNKNOWN_PROPERTIES)  // don't fail on unknown properties
						.disable(WRITE_DATES_AS_TIMESTAMPS);  // write dates as ISO-8601 strings
	}

	@PostConstruct private void postConstruct()
	{
		String schemeHostPort = scheme + "://" + host + ":" + port;

		baseURL = schemeHostPort + PATH_JEEERAAAH_ROOT;
		// baseURL = schemeHostPort;

		log.debug("scheme        : {}", scheme);
		log.debug("host          : {}", host);
		log.debug("port          : {}", port);
		log.debug("schemeHostPort: {}", schemeHostPort);
		log.debug("root          : {}", PathsTask.TOKEN_DOMAIN);
		log.debug("baseURL       : {}", baseURL);

		// create a properly configured ObjectMapper
		ObjectMapper objectMapper = createObjectMapper();

		// create a JacksonJsonProvider with our custom ObjectMapper
		JacksonJsonProvider jacksonJsonProvider = new JacksonJsonProvider(objectMapper);

		client = ClientBuilder.newBuilder()
				.register(jacksonJsonProvider) // register jackson provider instance with custom ObjectMapper
				// ✅ Register Keycloak authentication filter for automatic token injection
				// Must use instance (not class) because Jersey HK2 cannot inject CDI beans
				.register(new AuthorizationHeaderFilter(authService))
				.property(CONNECT_TIMEOUT, 5000)
				.property(READ_TIMEOUT, 15000)
				.property(MOXY_JSON_FEATURE_DISABLE, true)
				.build();

		// log registered jakarta providers for debugging
		List<String> classNames = new ArrayList<>();
		client.getConfiguration().getClasses().forEach(c -> classNames.add(c.getName()));
		log.debug("jakarta ws.rs client configuration classes:\n{}", String.join("\n", classNames));
	}

	@PreDestroy
	public void preDestroy()
	{
		client.close();
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// interface implementations
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public @NonNull TaskBean create(@NonNull TaskBean task) throws TechnicalException, NonTechnicalException
	{
		WebTarget webTarget = client.target(baseURL).path(TOKEN_DOMAIN);
		log.debug("webTarget: {}", webTarget);

		TaskCreationData creationData = newTaskCreationData(task);

		// Try to serialize to JSON for debugging
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new Jdk8Module());
			mapper.registerModule(new JavaTimeModule());
			String json = mapper.writeValueAsString(creationData);
			log.debug("JSON to be sent: {}", json);
		} catch (JsonProcessingException e) {
			log.warn("Failed to serialize TaskCreationData to JSON for debugging", e);
		}

		// Execute POST request with automatic Keycloak token handling (injection + refresh on 401)
		try (Response response = executeWithAuth(webTarget,
				requestBuilder -> requestBuilder.post(entity(creationData, APPLICATION_JSON))))
		{
			if (response.getStatusInfo().getFamily() == SUCCESSFUL)
			{
				TaskDTO dto = response.readEntity(TaskDTO.class);
				TaskBean result = toBean(dto, new ReferenceCycleTracking());
				// fire event to indicate that a new task has been created in the backend
				CDIUtil.fire(new TaskCreatedInBackendEvent(this, result));
				return result;
			}
			else
				throw newNonTechnicalException(response);
		}
		catch (ProcessingException e)
		{
			// this is thrown for technical issues (server down, wrong URL, timeout)
			throw new TechnicalException("communication error", e);
		}
	}

	@Override
	public Optional<TaskBean> read(@NonNull Long id) throws TechnicalException, NonTechnicalException
	{
		WebTarget webTarget = client.target(baseURL).path(TOKEN_BY_ID).resolveTemplate("id", id);
		log.debug("webTarget: {}", webTarget);
		// Execute GET request with automatic Keycloak token handling
		try (Response response = executeWithAuth(webTarget, requestBuilder -> requestBuilder.get()))
		{
			int status = response.getStatus();

			if (status == Status.OK.getStatusCode())
			{
				return Optional.of(toBean(response.readEntity(TaskDTO.class), new ReferenceCycleTracking()));
			}
			else if (status == Status.NOT_FOUND.getStatusCode())
			{
				return Optional.empty();
			}
			else
				throw newNonTechnicalException(response);
		}
		catch (ProcessingException e)
		{
			// this is thrown for technical issues (server down, wrong URL, timeout)
			throw new TechnicalException("communication error", e);
		}
	}

	@Override
	public @NonNull TaskBean update(@NonNull TaskBean task) throws TechnicalException, NonTechnicalException
	{
		WebTarget webTarget = client.target(baseURL).path(TOKEN_DOMAIN);
		log.debug("webTarget: {}", webTarget);
		// execute PUT request with automatic keycloak token handling
		try (Response response = executeWithAuth(webTarget,
				requestBuilder -> requestBuilder.put(entity(toDTO(task, new ReferenceCycleTracking()), APPLICATION_JSON))))
		{
			if (response.getStatusInfo().getFamily() == SUCCESSFUL)
			{
				TaskBean result = toBean(response.readEntity(TaskDTO.class), new ReferenceCycleTracking());
				// fire event to indicate that a new task has been created in the backend
				CDIUtil.fire(new TaskUpdatedInBackendEvent(this, result));
				return result;
			}
			else
				throw newNonTechnicalException(response);
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
				// fire event to indicate that a new task has been created in the backend
				CDIUtil.fire(new TaskDeletedInBackendEvent(this, id));
			}
			else if (response.getStatus() != NOT_FOUND.getStatusCode())
				// Only throw an exception if it's not a 404 (Not Found)
				throw newNonTechnicalException(response);
			else
				throw newNonTechnicalException(response);
		}
		catch (ProcessingException e)
		{
			// this is thrown for technical issues (server down, wrong URL, timeout)
			throw new TechnicalException("communication error", e);
		}
	}

	@Override
	public Set<TaskBean> findAll() throws TechnicalException, NonTechnicalException
	{
		WebTarget webTarget = client.target(baseURL).path(TOKEN_ALL);
		log.debug("webTarget: {}", webTarget);
		// Execute GET request with automatic Keycloak token handling
		try (Response response = executeWithAuth(webTarget, requestBuilder -> requestBuilder.get()))
		{
			if (response.getStatusInfo().getFamily() == SUCCESSFUL)
			{
				return response.readEntity(new GenericType<HashSet<TaskDTO>>() {
				}).stream().map(t -> toBean((t), new ReferenceCycleTracking())).collect(Collectors.toSet());
			}
			else
				throw newNonTechnicalException(response);
		}
		catch (ProcessingException e)
		{
			// this is thrown for technical issues (server down, wrong URL, timeout)
			throw new TechnicalException("communication error", e);
		}
	}

	@Override
	public Optional<TaskBean> findWithRelated(@NonNull Long id) throws TechnicalException, NonTechnicalException
	{
		WebTarget webTarget = client.target(baseURL).path(TOKEN_DOMAIN).path(TOKEN_BY_ID_WITH_RELATED).path(TOKEN_BY_ID).resolveTemplate("id", id);
		log.debug("webTarget: {}", webTarget);
		// Execute GET request with automatic Keycloak token handling
		try (Response response = executeWithAuth(webTarget, requestBuilder -> requestBuilder.get()))
		{
			if (response.getStatusInfo().getFamily() == SUCCESSFUL)
			{
				TaskDTO taskDTO = response.readEntity(TaskDTO.class);
				return Optional.of(toBean(taskDTO, new ReferenceCycleTracking()));
			}
			else
				throw newNonTechnicalException(response);
		}
		catch (ProcessingException e)
		{
			// this is thrown for technical issues (server down, wrong URL, timeout)
			throw new TechnicalException("communication error", e);
		}
	}

	// @Override public Set<TaskBean> findGroupTasksWithRelated(@NonNull Set<Long> ids) throws TechnicalException,
	// NonTechnicalException
	// {
	// WebTarget webTarget = client.target(PathsTask.PATH_BY_IDS_LAZY);
	// try (Response response = webTarget.request(APPLICATION_JSON).post(entity(ids, APPLICATION_JSON)))
	// {
	// if (response.getStatusInfo().getFamily() == SUCCESSFUL)
	// {
	// Set<TaskLazy> taskLazies = response.readEntity(new GenericType<Set<TaskLazy>>() {});
	// Set<TaskBean> result = new HashSet<>();
	//// for (TaskLazy taskLazy : taskLazies) result.add(new TaskBean(taskLazy.)); return
	// }
	// else
	// // handle business error (payload known)
	// throw newNonTechnicalException(response);
	// }
	// catch (ProcessingException e)
	// {
	// // this is thrown for technical issues (server down, wrong URL, timeout)
	// throw new TechnicalException("communication error", e);
	// }
	// }

	@Override
	public void addSubTask(@NonNull final TaskBean task, @NonNull final TaskBean subTask)
			throws TechnicalException, NonTechnicalException
	{
		WebTarget webTarget = client.target(baseURL).path(TOKEN_DOMAIN).path(TOKEN_ADD_SUB);
		log.debug("webTarget: {}", webTarget);
		// Execute PUT request with automatic Keycloak token handling
		try (Response response = executeWithAuth(webTarget, requestBuilder -> requestBuilder.accept(APPLICATION_JSON)
				.put(entity(newInterTaskRelationData(task, subTask), APPLICATION_JSON))))
		{
			throwExceptionForNoSuccessInRelationalOperationResponse(response);
			// fire event to indicate that a new relation for the task with subtask has been created in the backend
			CDIUtil.fire(new AddNewPredecessorRelationInBackendEvent(this, task, subTask));
		}
		catch (ProcessingException e)
		{
			// this is thrown for technical issues (server down, wrong URL, timeout)
			throw new TechnicalException("communication error", e);
		}
	}

	@Override
	public void addPredecessor(@NonNull final TaskBean task, @NonNull final TaskBean predecessor)
			throws TechnicalException, NonTechnicalException
	{
		WebTarget webTarget = client.target(baseURL).path(TOKEN_DOMAIN).path(TOKEN_ADD_PREDECESSOR);
		log.debug("webTarget: {}", webTarget);
		// Execute PUT request with automatic Keycloak token handling
		try (Response response = executeWithAuth(webTarget, requestBuilder -> requestBuilder.accept(APPLICATION_JSON)
				.put(entity(newInterTaskRelationData(task, predecessor), APPLICATION_JSON))))
		{
			throwExceptionForNoSuccessInRelationalOperationResponse(response);
			// fire event to indicate that a new relation for the task with predecessor has been created in the backend
			CDIUtil.fire(new AddNewPredecessorRelationInBackendEvent(this, task, predecessor));
		}
		catch (ProcessingException e)
		{
			// this is thrown for technical issues (server down, wrong URL, timeout)
			throw new TechnicalException("communication error", e);
		}
	}

	@Override
	public void addSuccessor(@NonNull final TaskBean task, @NonNull final TaskBean successor)
			throws TechnicalException, NonTechnicalException
	{
		WebTarget webTarget = client.target(baseURL).path(TOKEN_DOMAIN).path(TOKEN_ADD_SUCCESSOR);
		log.debug("webTarget: {}", webTarget);
		// Execute PUT request with automatic Keycloak token handling
		try (Response response = executeWithAuth(webTarget, requestBuilder -> requestBuilder.accept(APPLICATION_JSON)
				.put(entity(newInterTaskRelationData(task, successor), APPLICATION_JSON))))
		{

			throwExceptionForNoSuccessInRelationalOperationResponse(response);
			// fire event to indicate that a new relation for the task with successor has been created in the backend
			CDIUtil.fire(new AddNewSuccessorRelationInBackendEvent(this, task, successor));
		}
		catch (ProcessingException e)
		{
			// this is thrown for technical issues (server down, wrong URL, timeout)
			throw new TechnicalException("communication error", e);
		}
	}

	@Override
	public void removeSubTask(@NonNull final TaskBean task, @NonNull final TaskBean subTask)
			throws TechnicalException, NonTechnicalException
	{
		WebTarget webTarget = client.target(baseURL).path(TOKEN_DOMAIN).path(TOKEN_REMOVE_SUB);
		log.debug("webTarget: {}", webTarget);
		// Execute PUT request with automatic Keycloak token handling
		try (Response response = executeWithAuth(webTarget, requestBuilder -> requestBuilder.accept(APPLICATION_JSON)
				.put(entity(newInterTaskRelationData(task, subTask), APPLICATION_JSON))))
		{
			throwExceptionForNoSuccessInRelationalOperationResponse(response);
			// fire event to indicate that the relation for the task with subtask has been removed in the backend
			CDIUtil.fire(new RemoveSubTaskRelationInBackendEvent(this, task, subTask));
		}
		catch (ProcessingException e)
		{
			// this is thrown for technical issues (server down, wrong URL, timeout)
			throw new TechnicalException("communication error", e);
		}
	}

	@Override
	public void removePredecessor(@NonNull final TaskBean task, @NonNull final TaskBean predecessor)
			throws TechnicalException, NonTechnicalException
	{
		WebTarget webTarget = client.target(baseURL).path(TOKEN_DOMAIN).path(TOKEN_REMOVE_PREDECESSOR);
		log.debug("webTarget: {}", webTarget);
		// Execute PUT request with automatic Keycloak token handling
		try (Response response = executeWithAuth(webTarget, requestBuilder -> requestBuilder.accept(APPLICATION_JSON)
				.put(entity(newInterTaskRelationData(task, predecessor), APPLICATION_JSON))))
		{
			throwExceptionForNoSuccessInRelationalOperationResponse(response);
			// fire event to indicate that the relation for the task with subtask has been removed in the backend
			CDIUtil.fire(new RemovePredecessorRelationInBackendEvent(this, task, predecessor));
		}
		catch (ProcessingException e)
		{
			// this is thrown for technical issues (server down, wrong URL, timeout)
			throw new TechnicalException("communication error", e);
		}
	}

	@Override
	public void removeSuccessor(@NonNull final TaskBean task, @NonNull final TaskBean successor)
			throws TechnicalException, NonTechnicalException
	{
		WebTarget webTarget = client.target(baseURL).path(TOKEN_DOMAIN).path(TOKEN_REMOVE_SUCCESSOR);
		log.debug("webTarget: {}", webTarget);
		// Execute PUT request with automatic Keycloak token handling
		try (Response response = executeWithAuth(webTarget, requestBuilder -> requestBuilder.accept(APPLICATION_JSON)
				.put(entity(newInterTaskRelationData(task, successor), APPLICATION_JSON))))
		{
			throwExceptionForNoSuccessInRelationalOperationResponse(response);
			// fire event to indicate that the relation for the task with subtask has been removed in the backend
			CDIUtil.fire(new RemovePredecessorRelationInBackendEvent(this, task, successor));
		}
		catch (ProcessingException e)
		{
			// this is thrown for technical issues (server down, wrong URL, timeout)
			throw new TechnicalException("communication error", e);
		}
	}

	@Override
	public void removeNeighboursFromTask(@NonNull RemoveNeighboursFromTaskConfig removeNeighboursFromTaskConfig)
			throws TechnicalException, NonTechnicalException
	{
		WebTarget webTarget = client.target(baseURL).path(TOKEN_DOMAIN).path(TOKEN_REMOVE_NEIGHBOURS_FROM_TASK);
		log.debug("webTarget: {}", webTarget);
		// Execute PUT request with automatic Keycloak token handling
		try (Response response = executeWithAuth(webTarget, requestBuilder -> requestBuilder.accept(APPLICATION_JSON)
				.put(entity(removeNeighboursFromTaskConfig, APPLICATION_JSON))))
		{
			throwExceptionForNoSuccessInRelationalOperationResponse(response);
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

		if (response.getStatus() == Status.CONFLICT.getStatusCode())
		{
			// try to read the error message from the response
			// create a new error message if the response is not a valid JSON or does not contain a message
			// log raw body for diagnostics first
			String raw = null;
			try
			{
				raw = response.readEntity(String.class);
			}
			catch (Exception ignore)
			{
			}
			log.error("HTTP {} {} - response body:\n{}", response.getStatus(), response.getStatusInfo().getReasonPhrase(),
					raw);
			NonTechnicalException error = null;
			try
			{
				if (raw != null && !raw.isBlank())
				{
					ErrorResponse er = createObjectMapper().readValue(raw, ErrorResponse.class);
					error = new NonTechnicalException(er);
				}
				else
					error = response.readEntity(NonTechnicalException.class);

				if (isNull(error.getMessage()))
					throw new NonTechnicalException(new ErrorResponse("invalid task relation", "unknown cause"));
				else
					throw error;
			}
			catch (ProcessingException | JsonProcessingException e)
			{
				// this is thrown for technical issues (server down, wrong URL, timeout)
				throw new TechnicalException("communication error", e);
			}
		}
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
			throw new TechnicalException("Communication error", e);
		}
	}

	/**
	 * Functional interface for flexible HTTP request execution.
	 *
	 * <p>Allows different HTTP methods (GET, POST, PUT, DELETE) to be passed as lambda expressions
	 * to the {@link #executeWithAuth(WebTarget, RequestExecutor)} method.</p>
	 *
	 * <p><strong>Design pattern:</strong> Strategy pattern - encapsulates the "how" of request execution,
	 * while {@code executeWithAuth()} handles the "what" (authentication logic).</p>
	 *
	 * <p><strong>Note:</strong> Authorization header is automatically added by AuthorizationHeaderFilter.</p>
	 */
	@FunctionalInterface
	private interface RequestExecutor
	{
		/**
		 * Executes an HTTP request using the provided request builder.
		 *
		 * <p>The Authorization header is automatically added by AuthorizationHeaderFilter.</p>
		 *
		 * @param requestBuilder JAX-RS Invocation.Builder
		 * @return HTTP Response
		 */
		Response execute(jakarta.ws.rs.client.Invocation.Builder requestBuilder);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// event classes
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static class TaskCreatedInBackendEvent extends AbstractEvent<TaskServiceClient, TaskBean>
	{
		public TaskCreatedInBackendEvent(@NonNull TaskServiceClient source, @NonNull TaskBean task) {
			super(source, task);
		}
	}

	public static class TaskUpdatedInBackendEvent extends AbstractEvent<TaskServiceClient, TaskBean>
	{
		public TaskUpdatedInBackendEvent(@NonNull TaskServiceClient source, @NonNull TaskBean task) {
			super(source, task);
		}
	}

	public static class TaskDeletedInBackendEvent extends AbstractEvent<TaskServiceClient, Long>
	{
		public TaskDeletedInBackendEvent(@NonNull TaskServiceClient source, @NonNull Long id) {
			super(source, id);
		}
	}

	public static class AddNewSubTaskRelationInBackendEvent extends AbstractEvent<TaskServiceClient, TaskRelationInfo>
	{
		public AddNewSubTaskRelationInBackendEvent(TaskServiceClient taskServiceClient, @NonNull TaskBean task,
				@NonNull TaskBean subTask) {
			super(taskServiceClient, new TaskRelationInfo(task, subTask));
		}
	}

	public static class AddNewPredecessorRelationInBackendEvent extends AbstractEvent<TaskServiceClient, TaskRelationInfo>
	{
		public AddNewPredecessorRelationInBackendEvent(TaskServiceClient taskServiceClient, @NonNull TaskBean task,
				@NonNull TaskBean predecessor) {
			super(taskServiceClient, new TaskRelationInfo(task, predecessor));
		}
	}

	public static class AddNewSuccessorRelationInBackendEvent extends AbstractEvent<TaskServiceClient, TaskRelationInfo>
	{
		public AddNewSuccessorRelationInBackendEvent(TaskServiceClient taskServiceClient, @NonNull TaskBean task,
				@NonNull TaskBean successor) {
			super(taskServiceClient, new TaskRelationInfo(task, successor));
		}
	}

	public static class RemoveSubTaskRelationInBackendEvent extends AbstractEvent<TaskServiceClient, TaskRelationInfo>
	{
		public RemoveSubTaskRelationInBackendEvent(TaskServiceClient taskServiceClient, @NonNull TaskBean task,
				@NonNull TaskBean subTask) {
			super(taskServiceClient, new TaskRelationInfo(task, subTask));
		}
	}

	public static class RemovePredecessorRelationInBackendEvent extends AbstractEvent<TaskServiceClient, TaskRelationInfo>
	{
		public RemovePredecessorRelationInBackendEvent(TaskServiceClient taskServiceClient, @NonNull TaskBean task,
				@NonNull TaskBean subTask) {
			super(taskServiceClient, new TaskRelationInfo(task, subTask));
		}
	}

	public static class RemoveSuccessorRelationInBackendEvent extends AbstractEvent<TaskServiceClient, TaskRelationInfo>
	{
		public RemoveSuccessorRelationInBackendEvent(TaskServiceClient taskServiceClient, @NonNull TaskBean task,
				@NonNull TaskBean subTask) {
			super(taskServiceClient, new TaskRelationInfo(task, subTask));
		}
	}

	@Accessors(fluent = true)
	public record TaskRelationInfo(@NonNull TaskBean task, @NonNull TaskBean relatedTask)
	{
	}

	private TaskCreationData newTaskCreationData(TaskBean task)
	{
		// Validate that task has a task group
		if (task.taskGroup() == null)
		{
			throw new IllegalArgumentException("Task must belong to a task group (task.taskGroup() is null)");
		}

		// Validate that task group has an ID (i.e., is persisted)
		Long taskGroupId = task.taskGroup().id();
		if (taskGroupId == null)
		{
			throw new IllegalArgumentException("Task group must be persisted before creating tasks (taskGroup.id() is null)");
		}

		TaskDTOLazy taskLazy = (TaskDTOLazy) toLazy(task, new ReferenceCycleTracking());
		TaskCreationData data = new TaskCreationData(taskGroupId, taskLazy);

		// Debug logging
		log.debug("Creating TaskCreationData:");
		log.debug("  taskGroupId: {}", taskGroupId);
		log.debug("  taskLazy.taskGroupId(): {}", taskLazy.taskGroupId());
		log.debug("  data.getTaskGroupId(): {}", data.getTaskGroupId());
		log.debug("  data: {}", data);

		return data;
	}

	private InterTaskRelationData newInterTaskRelationData(TaskBean task, TaskBean relatedTask)
	{
		return new InterTaskRelationData(requireNonNull(task.id()), requireNonNull(relatedTask.id()));
	}
}
