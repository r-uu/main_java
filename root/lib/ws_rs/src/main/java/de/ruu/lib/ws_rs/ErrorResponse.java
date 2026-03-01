package de.ruu.lib.ws_rs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import jakarta.ws.rs.core.Response.Status;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Getter
@Accessors(fluent = true)
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse
{
	private final @NonNull String message;
	private final @NonNull String cause;
	private final @NonNull Status httpStatus;

	// no-arg constructor for deserialization
	protected ErrorResponse()                                            { this(""     , ""   , INTERNAL_SERVER_ERROR); }

	public ErrorResponse(@NonNull String message)                        { this(message, ""   , INTERNAL_SERVER_ERROR); }
	public ErrorResponse(@NonNull String message, @NonNull String cause) { this(message, cause, INTERNAL_SERVER_ERROR); }
	public ErrorResponse(@NonNull String message, @NonNull String cause, Status httpStatus)
	{
		this.message    = message;
		this.cause      = cause;
		this.httpStatus = httpStatus;
	}

	// java bean style getters for deserialisation
	public @NonNull String getMessage   () { return message;    }
	public @NonNull String getCause     () { return cause;      }
	public @NonNull Status getHttpStatus() { return httpStatus; }
}