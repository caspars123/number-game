package com.example.game.rest.external.api.v1;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PingRest {

	@GetMapping("/ping")
	public String ping() {
		return "pong";
	}
}
