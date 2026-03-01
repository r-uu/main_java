package de.ruu.lib.ws_rs;

import jakarta.ws.rs.core.Response.Status;
import lombok.Getter;
import lombok.NonNull;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Getter
public class ErrorResponseWithException extends ErrorResponse
{
	private final @NonNull SerializableException serializableException;

	public ErrorResponseWithException(@NonNull String message, @NonNull SerializableException serializableException)
	{
		this(message, "", serializableException);
	}

	public ErrorResponseWithException(
			@NonNull String message, @NonNull String cause, @NonNull SerializableException serializableException)
	{
		this(message, cause, INTERNAL_SERVER_ERROR, serializableException);
	}

	public ErrorResponseWithException
	(
			@NonNull String message,
			@NonNull String cause,
			@NonNull Status httpStatus,
			@NonNull SerializableException serializableException
	)
	{
		super(message, cause, httpStatus);
		this.serializableException = serializableException;
	}
}
