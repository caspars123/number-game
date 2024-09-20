package com.example.game.conf;

import com.example.game.rest.jwt.JwtAuthenticationService;
import com.example.game.service.BetService;
import com.example.game.websocket.handler.GameHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.List;

@EnableWebSocket
@Configuration
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

	private final BetService betService;
	private final JwtAuthenticationService jwtAuthenticationService;

	@Value("${game.security.cors.allowed-origins}")
	private List<String> allowedOrigins;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry
			.addHandler(new GameHandler(betService, jwtAuthenticationService), "/play")
			.setAllowedOrigins(allowedOrigins.toArray(new String[0]));
	}
}