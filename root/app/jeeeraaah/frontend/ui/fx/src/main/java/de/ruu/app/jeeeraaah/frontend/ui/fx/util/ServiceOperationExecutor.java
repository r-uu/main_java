package de.ruu.app.jeeeraaah.frontend.ui.fx.util;

import de.ruu.app.jeeeraaah.frontend.ui.fx.auth.AuthenticationHelper;
import de.ruu.lib.fx.control.dialog.ExceptionDialog;
import de.ruu.lib.ws_rs.NonTechnicalException;
import de.ruu.lib.ws_rs.SessionExpiredException;
import de.ruu.lib.ws_rs.TechnicalException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Modern utility class for executing service operations with automatic session expiry handling.
 *
 * <p>This class provides a centralized, fluent mechanism for calling backend service clients
 * with automatic re-authentication when the session expires.</p>
 *
 * <h2>Modern Features (Java 17+)</h2>
 * <ul>
 *   <li><b>Pattern Matching:</b> Clean instanceof checks with automatic casting</li>
 *   <li><b>Records:</b> Immutable operation context</li>
 *   <li><b>Fluent API:</b> Builder pattern for readable configuration</li>
 *   <li><b>Optional:</b> Better error handling without null checks</li>
 *   <li><b>Method References:</b> Clean lambda syntax</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>1. Simple Operation (Classic Style):</h3>
 * <pre>{@code
 * Set<TaskGroupFlat> groups = executor.execute(
 *     () -> taskGroupServiceClient.findAllFlat(),
 *     "fetching task groups",
 *     "Failed to load task groups",
 *     "Load failed after re-login"
 * );
 * }</pre>
 *
 * <h3>2. Fluent API Style (Modern):</h3>
 * <pre>{@code
 * executor.operation(() -> taskGroupServiceClient.create(bean))
 *         .describedAs("creating task group '" + bean.name() + "'")
 *         .onError("Failed to create task group")
 *         .onRetryFailure("Creation failed after re-login")
 *         .execute();
 * }</pre>
 *
 * <h3>3. With Success Handler:</h3>
 * <pre>{@code
 * executor.operation(() -> taskGroupServiceClient.create(bean))
 *         .describedAs("creating task group")
 *         .onError("Failed to create")
 *         .onRetryFailure("Retry failed")
 *         .onSuccess(created -> log.info("Created: {}", created.id()))
 *         .execute();
 * }</pre>
 *
 * <h3>4. Void Operations (Modern):</h3>
 * <pre>{@code
 * executor.voidOperation(() -> taskGroupServiceClient.update(bean))
 *         .describedAs("updating task group")
 *         .onError("Failed to update")
 *         .onRetryFailure("Update failed after re-login")
 *         .execute();
 * }</pre>
 *
 * <h3>5. Optional Results (Modern):</h3>
 * <pre>{@code
 * executor.operation(() -> taskGroupServiceClient.read(id))
 *         .describedAs("reading task group")
 *         .onError("Failed to read")
 *         .onRetryFailure("Read failed after re-login")
 *         .executeOptional()
 *         .ifPresentOrElse(
 *             bean -> processBean(bean),
 *             () -> log.warn("Not found")
 *         );
 * }</pre>
 *
 * <h2>Why This Class Exists</h2>
 * <p>Without this utility, every controller would need 70+ lines of boilerplate code
 * for session expiry handling. This class reduces that to 1-4 lines per operation
 * while maintaining consistent behavior across the entire application.</p>
 *
 * @author r-uu
 * @since Java 17
 */
@Slf4j
public class ServiceOperationExecutor
{
	private final AuthenticationHelper authHelper;

	/**
	 * Creates a new executor.
	 *
	 * @param authHelper the authentication helper for handling re-login
	 */
	public ServiceOperationExecutor(@NonNull AuthenticationHelper authHelper)
	{
		this.authHelper = authHelper;
	}

	// ========== Classic API (Backwards Compatible) ==========

	/**
	 * Executes a service operation with automatic session expiry handling (classic style).
	 *
	 * <p>This is the backwards-compatible API. For modern fluent style, use
	 * {@link #operation(ServiceOperation)} instead.</p>
	 *
	 * @param <T> the type of the result
	 * @param operation the operation to execute
	 * @param operationName description for logging
	 * @param errorTitle error dialog title
	 * @param errorHeaderOnRetryFailure error header when retry fails
	 * @return the result of the operation
	 * @throws TechnicalException if a non-session-expiry error occurs
	 * @throws NonTechnicalException if a non-session-expiry error occurs
	 */
	public <T> T execute(
			@NonNull ServiceOperation<T> operation,
			@NonNull String operationName,
			@NonNull String errorTitle,
			@NonNull String errorHeaderOnRetryFailure
	) throws TechnicalException, NonTechnicalException
	{
		return operation(operation)
				.describedAs(operationName)
				.onError(errorTitle)
				.onRetryFailure(errorHeaderOnRetryFailure)
				.execute();
	}

	// ========== Modern Fluent API ==========

	/**
	 * Starts building a fluent operation configuration.
	 *
	 * <p><b>Modern Usage:</b></p>
	 * <pre>{@code
	 * TaskGroupBean result = executor
	 *     .operation(() -> taskGroupServiceClient.create(bean))
	 *     .describedAs("creating task group")
	 *     .onError("Failed to create")
	 *     .onRetryFailure("Retry failed")
	 *     .execute();
	 * }</pre>
	 *
	 * @param <T> the type of the result
	 * @param operation the operation to execute
	 * @return a fluent builder for configuration
	 */
	public <T> OperationBuilder<T> operation(@NonNull ServiceOperation<T> operation)
	{
		return new OperationBuilder<>(operation, this);
	}

	/**
	 * Starts building a fluent void operation configuration.
	 *
	 * <p><b>Modern Usage:</b></p>
	 * <pre>{@code
	 * executor
	 *     .voidOperation(() -> taskGroupServiceClient.update(bean))
	 *     .describedAs("updating task group")
	 *     .onError("Failed to update")
	 *     .execute();
	 * }</pre>
	 *
	 * @param operation the void operation to execute
	 * @return a fluent builder for configuration
	 */
	public VoidOperationBuilder voidOperation(@NonNull VoidServiceOperation operation)
	{
		return new VoidOperationBuilder(operation, this);
	}

	// ========== Internal Execution Logic ==========

	/**
	 * Internal method that executes the operation with session retry logic.
	 *
	 * <p>Uses modern Java 17+ pattern matching for instanceof checks.</p>
	 */
	private <T> T executeInternal(@NonNull OperationContext<T> context)
			throws TechnicalException, NonTechnicalException
	{
		try
		{
			return context.operation().execute();
		}
		catch (TechnicalException | NonTechnicalException e)
		{
			// Modern Pattern Matching (Java 17+)
			if (e.getCause() instanceof SessionExpiredException sessionExpired)
			{
				return handleSessionExpired(context, sessionExpired);
			}
			else
			{
				// Not a session expiry - re-throw to let caller handle
				throw e;
			}
		}
	}

	/**
	 * Handles session expiration with re-login and retry logic.
	 *
	 * @param context the operation context
	 * @param sessionExpired the session expired exception
	 * @return the result after successful retry
	 * @throws TechnicalException if retry fails
	 * @throws NonTechnicalException if retry fails or user cancels
	 */
	private <T> T handleSessionExpired(
			@NonNull OperationContext<T> context,
			@NonNull SessionExpiredException sessionExpired
	) throws TechnicalException, NonTechnicalException
	{
		log.warn("Session expired while {}", context.operationName(), sessionExpired);

		boolean reLoginSuccessful = authHelper.handleSessionExpired(false);

		if (reLoginSuccessful)
		{
			return retryAfterReLogin(context);
		}
		else
		{
			log.info("User cancelled re-login during {}", context.operationName());
			// Re-throw the original exception
			throw new TechnicalException("User cancelled re-login", sessionExpired);
		}
	}

	/**
	 * Retries the operation after successful re-login.
	 *
	 * @param context the operation context
	 * @return the result of the retry
	 * @throws TechnicalException if retry fails
	 * @throws NonTechnicalException if retry fails
	 */
	private <T> T retryAfterReLogin(@NonNull OperationContext<T> context)
			throws TechnicalException, NonTechnicalException
	{
		log.info("Re-login successful, retrying {}...", context.operationName());

		try
		{
			T result = context.operation().execute();
			log.info("✓ Successfully completed {} after re-authentication", context.operationName());

			// Call success handler if present
			context.onSuccess().ifPresent(handler -> handler.accept(result));

			return result;
		}
		catch (TechnicalException | NonTechnicalException retryException)
		{
			log.error("Failed {} even after re-authentication", context.operationName(), retryException);

			ExceptionDialog.showAndWait(
				context.errorTitle(),
				context.errorHeaderOnRetryFailure(),
				"Could not complete operation even after successful re-authentication.",
				retryException
			);

			throw retryException;
		}
	}

	// ========== Fluent Builder ==========

	/**
	 * Fluent builder for configuring and executing operations.
	 *
	 * <p>This builder provides a modern, readable API for operation execution.</p>
	 *
	 * @param <T> the type of the result
	 */
	public static class OperationBuilder<T>
	{
		private final ServiceOperation<T> operation;
		private final ServiceOperationExecutor executor;

		private String operationName = "operation";
		private String errorTitle = "Operation failed";
		private String errorHeaderOnRetryFailure = "Operation failed after re-login";
		private Consumer<T> onSuccess = null;

		private OperationBuilder(ServiceOperation<T> operation, ServiceOperationExecutor executor)
		{
			this.operation = operation;
			this.executor = executor;
		}

		/**
		 * Sets the operation description (for logging).
		 *
		 * @param description the description
		 * @return this builder
		 */
		public OperationBuilder<T> describedAs(@NonNull String description)
		{
			this.operationName = description;
			return this;
		}

		/**
		 * Sets the error dialog title.
		 *
		 * @param title the error title
		 * @return this builder
		 */
		public OperationBuilder<T> onError(@NonNull String title)
		{
			this.errorTitle = title;
			return this;
		}

		/**
		 * Sets the error message when retry fails.
		 *
		 * @param message the retry failure message
		 * @return this builder
		 */
		public OperationBuilder<T> onRetryFailure(@NonNull String message)
		{
			this.errorHeaderOnRetryFailure = message;
			return this;
		}

		/**
		 * Sets a success handler to be called after successful execution.
		 *
		 * <p><b>Example:</b></p>
		 * <pre>{@code
		 * .onSuccess(result -> log.info("Created: {}", result.id()))
		 * }</pre>
		 *
		 * @param handler the success handler
		 * @return this builder
		 */
		public OperationBuilder<T> onSuccess(@NonNull Consumer<T> handler)
		{
			this.onSuccess = handler;
			return this;
		}

		/**
		 * Executes the configured operation.
		 *
		 * @return the result of the operation
		 * @throws TechnicalException if an error occurs
		 * @throws NonTechnicalException if an error occurs
		 */
		public T execute() throws TechnicalException, NonTechnicalException
		{
			OperationContext<T> context = new OperationContext<>(
				operation,
				operationName,
				errorTitle,
				errorHeaderOnRetryFailure,
				Optional.ofNullable(onSuccess)
			);

			return executor.executeInternal(context);
		}

		/**
		 * Executes the operation and wraps the result in an Optional.
		 *
		 * <p><b>Example:</b></p>
		 * <pre>{@code
		 * executor.operation(() -> serviceClient.read(id))
		 *         .describedAs("reading entity")
		 *         .onError("Failed to read")
		 *         .executeOptional()
		 *         .ifPresent(entity -> process(entity));
		 * }</pre>
		 *
		 * @return Optional containing the result, or empty if result is null
		 * @throws TechnicalException if an error occurs
		 * @throws NonTechnicalException if an error occurs
		 */
		public Optional<T> executeOptional() throws TechnicalException, NonTechnicalException
		{
			return Optional.ofNullable(execute());
		}
	}

	/**
	 * Fluent builder for void operations.
	 */
	public static class VoidOperationBuilder
	{
		private final VoidServiceOperation operation;
		private final ServiceOperationExecutor executor;

		private String operationName = "operation";
		private String errorTitle = "Operation failed";
		private String errorHeaderOnRetryFailure = "Operation failed after re-login";

		private VoidOperationBuilder(VoidServiceOperation operation, ServiceOperationExecutor executor)
		{
			this.operation = operation;
			this.executor = executor;
		}

		public VoidOperationBuilder describedAs(@NonNull String description)
		{
			this.operationName = description;
			return this;
		}

		public VoidOperationBuilder onError(@NonNull String title)
		{
			this.errorTitle = title;
			return this;
		}

		public VoidOperationBuilder onRetryFailure(@NonNull String message)
		{
			this.errorHeaderOnRetryFailure = message;
			return this;
		}

		/**
		 * Executes the void operation.
		 *
		 * @throws TechnicalException if an error occurs
		 * @throws NonTechnicalException if an error occurs
		 */
		public void execute() throws TechnicalException, NonTechnicalException
		{
			// Wrap void operation in ServiceOperation<Void>
			ServiceOperation<Void> wrappedOperation = () -> {
				operation.execute();
				return null;
			};

			OperationContext<Void> context = new OperationContext<>(
				wrappedOperation,
				operationName,
				errorTitle,
				errorHeaderOnRetryFailure,
				Optional.empty()
			);

			executor.executeInternal(context);
		}
	}

	// ========== Records (Immutable Context) ==========

	/**
	 * Immutable context for operation execution.
	 *
	 * <p>This record encapsulates all configuration for an operation execution.
	 * Using a record ensures immutability and provides automatic equals/hashCode/toString.</p>
	 *
	 * @param <T> the type of the result
	 * @param operation the operation to execute
	 * @param operationName description for logging
	 * @param errorTitle error dialog title
	 * @param errorHeaderOnRetryFailure error message when retry fails
	 * @param onSuccess optional success handler
	 */
	private record OperationContext<T>(
		@NonNull ServiceOperation<T> operation,
		@NonNull String operationName,
		@NonNull String errorTitle,
		@NonNull String errorHeaderOnRetryFailure,
		@NonNull Optional<Consumer<T>> onSuccess
	) {}

	// ========== Functional Interfaces ==========

	/**
	 * Functional interface for service operations that return a result.
	 *
	 * @param <T> the type of the result
	 */
	@FunctionalInterface
	public interface ServiceOperation<T>
	{
		/**
		 * Executes the service operation.
		 *
		 * @return the result of the operation (may be {@code null})
		 * @throws TechnicalException if a technical error occurs
		 * @throws NonTechnicalException if a non-technical error occurs
		 */
		T execute() throws TechnicalException, NonTechnicalException;
	}

	/**
	 * Functional interface for void service operations.
	 */
	@FunctionalInterface
	public interface VoidServiceOperation
	{
		/**
		 * Executes the void service operation.
		 *
		 * @throws TechnicalException if a technical error occurs
		 * @throws NonTechnicalException if a non-technical error occurs
		 */
		void execute() throws TechnicalException, NonTechnicalException;
	}
}
