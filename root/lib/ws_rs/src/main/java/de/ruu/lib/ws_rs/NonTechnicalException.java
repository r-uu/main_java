package de.ruu.lib.ws_rs;

import lombok.Getter;

public class NonTechnicalException extends RuntimeException {
	@Getter
	private final ErrorResponse errorResponse;

	public NonTechnicalException(ErrorResponse errorResponse) {
		super(errorResponse.message());
		this.errorResponse = errorResponse;
	}
}
