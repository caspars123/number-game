package com.example.game.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidationException extends RuntimeException {

	private static final String GENERIC_EXCEPTION_MESSAGE = "Request validation failed";

	public ValidationException(String message) {
		super(GENERIC_EXCEPTION_MESSAGE + ": " + message);
	}
}
