package com.example.game.websocket.handler;

import com.example.game.dto.enums.WebSocketMessageIncomingType;
import com.example.game.dto.model.WebSocketMessageIncoming;
import com.example.game.exception.ValidationException;
import com.example.game.rest.jwt.JwtAuthenticationService;
import com.example.game.service.BetService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RequiredArgsConstructor
@Component
@Slf4j
public class GameHandler extends TextWebSocketHandler {

	private final CopyOnWriteArrayList<WebSocketSession> SESSIONS = new CopyOnWriteArrayList<>();

	private final BetService betService;
	private final JwtAuthenticationService jwtAuthenticationService;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		log.debug("Session with ID {} has been established", session.getId());
	}

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

		log.debug("Message received: {}", message.getPayload());

		String token = session.getHandshakeHeaders().containsKey(AUTHORIZATION)
			? session.getHandshakeHeaders().get(AUTHORIZATION).getFirst()
			: null;
		jwtAuthenticationService.authenticate(token, true);

		WebSocketMessageIncoming webSocketMessage = parseMessage(session, message);

		if (webSocketMessage == null) {
			return;
		}

		if (webSocketMessage.getType() == WebSocketMessageIncomingType.JOIN_GAME) {
			handleUserJoinedGameMessage(session, webSocketMessage);
		} else if (webSocketMessage.getType() == WebSocketMessageIncomingType.PLACE_BET) {
			handlePlaceBetMessage(session, webSocketMessage);
		} else if (webSocketMessage.getType() == WebSocketMessageIncomingType.LEAVE_GAME) {
			handleLeaveGameMessage(session, webSocketMessage);
		} else {
			session.sendMessage(new TextMessage("Unknown message type"));
		}
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		log.debug("Exception occured: {}", exception.getMessage());
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		log.debug("Connection closed on session:  {} with status: {}", session.getId(), closeStatus);

		SESSIONS.remove(session);
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}

	private WebSocketMessageIncoming parseMessage(WebSocketSession session, TextMessage message) throws IOException {
		try {
			return new Gson().fromJson(message.getPayload(), WebSocketMessageIncoming.class);

		} catch (Exception e) {
			SESSIONS.remove(session);
			session.sendMessage(new TextMessage("Error parsing message"));
			session.close();
			return null;
		}
	}

	private void handleUserJoinedGameMessage(WebSocketSession session, WebSocketMessageIncoming webSocketMessage) throws IOException {

		if (SESSIONS.contains(session)) {
			session.sendMessage(new TextMessage("You have already joined the game!"));
			return;
		}

		SESSIONS.add(session);

		session.sendMessage(new TextMessage("Welcome to the number guessing game!"));

		sendMessageToActivePlayers(String.format("Player %s joined the game", SecurityContextHolder.getContext().getAuthentication().getName()));
	}

	public void handlePlaceBetMessage(WebSocketSession session, WebSocketMessageIncoming message) throws IOException {

		if (!SESSIONS.contains(session)) {
			session.sendMessage(new TextMessage("You need to join the game before you can place a bet"));
			return;
		}

		try {
			betService.placeBet(message.getChosenNumber(), message.getBetAmount());
		} catch (ValidationException e) {
			session.sendMessage(new TextMessage(e.getMessage()));
			return;
		}

		String commonMessagePart = String.format("a bet on number %d for %s euros", message.getChosenNumber(), message.getBetAmount());
		session.sendMessage(new TextMessage("You have placed " + commonMessagePart));

		sendMessageToActivePlayers(String.format("Player %s placed %s", SecurityContextHolder.getContext().getAuthentication().getName(), commonMessagePart));
	}

	private void handleLeaveGameMessage(WebSocketSession session, WebSocketMessageIncoming webSocketMessage) throws IOException {
		if (!SESSIONS.contains(session)) {
			session.sendMessage(new TextMessage("You have not joined the game"));
			return;
		}

		session.sendMessage(new TextMessage("You have left the game"));
		SESSIONS.remove(session);
		sendMessageToActivePlayers(String.format("Player %s left the game", SecurityContextHolder.getContext().getAuthentication().getName())); // TODO: Set user name to context
	}

	public void sendMessageToActivePlayers(String message) {
		log.debug("Sending message:{}", message);
		for (WebSocketSession session : SESSIONS) {
			try {
				session.sendMessage(new TextMessage(message));
			} catch (IOException e) {
				log.debug("Exception occured: {}", e.getMessage());
			}
		}
	}
}
