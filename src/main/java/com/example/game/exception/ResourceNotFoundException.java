package com.example.game.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ResourceNotFoundException extends RuntimeException {

	private final transient Map<String, Object> parameters;

	public ResourceNotFoundException(String message, Map<String, Object> parameters) {
		super(message);
		this.parameters = parameters;
	}
}
