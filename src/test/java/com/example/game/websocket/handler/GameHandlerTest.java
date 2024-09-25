package com.example.game.websocket.handler;

import com.example.game.BaseTest;
import com.example.game.dto.enums.WebSocketMessageIncomingType;
import com.example.game.dto.model.JwtRequestDto;
import com.example.game.dto.model.WebSocketMessageIncoming;
import com.example.game.rest.external.api.v1.UserController;
import com.example.game.rest.jwt.JwtAuthenticationService;
import com.example.game.service.BetService;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

import static com.example.game.DbHelper.USER_1_EMAIL;
import static com.example.game.DbHelper.USER_1_ID;
import static com.example.game.DbHelper.USER_1_PASSWORD_TEXT;
import static com.example.game.DbHelper.USER_2_EMAIL;
import static com.example.game.DbHelper.USER_2_ID;
import static com.example.game.DbHelper.USER_2_PASSWORD_TEXT;
import static com.example.game.dto.enums.WebSocketMessageIncomingType.JOIN_GAME;
import static com.example.game.dto.enums.WebSocketMessageIncomingType.LEAVE_GAME;
import static com.example.game.dto.enums.WebSocketMessageIncomingType.PLACE_BET;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Sql({
	"/sql/users.sql",
	"/sql/round_ongoing.sql",
})
public class GameHandlerTest extends BaseTest {

	@MockBean
	private BetService betService;

	@Autowired
	private JwtAuthenticationService jwtAuthenticationService;

	@Autowired
	private UserController userController;

	@Mock
	private WebSocketSession user1Session;

	@Mock
	private WebSocketSession user2Session;

	@BeforeEach
	public void setup() {
		setUpAuth(true);
	}

	private void setUpAuth(boolean player1) {
		SecurityContextHolder.clearContext();

		String email = player1 ? USER_1_EMAIL : USER_2_EMAIL;
		String password = player1 ? USER_1_PASSWORD_TEXT : USER_2_PASSWORD_TEXT;
		WebSocketSession webSocketSession = player1 ? user1Session : user2Session;

		String token = userController.authenticate(new JwtRequestDto(email, password)).token();

		// Mock session id
		when(webSocketSession.getId()).thenReturn(email);

		// Mock the handshake headers
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
		when(webSocketSession.getHandshakeHeaders()).thenReturn(headers);
	}

	@Test
	public void test_join_game() throws Exception {

		// Create a GameHandler instance
		GameHandler handler = new GameHandler(betService, jwtAuthenticationService);

		TextMessage joinGameMessage = getTextMessage(JOIN_GAME, null, null);

		// Call the method to test
		handler.handleTextMessage(user1Session, joinGameMessage);
		checkJoinGameMessagesSentToSession(user1Session, 2, USER_1_ID);
	}

	@Test
	public void test_join_game_two_times() throws Exception {

		// Create a GameHandler instance
		GameHandler handler = new GameHandler(betService, jwtAuthenticationService);

		TextMessage joinGameMessage = getTextMessage(JOIN_GAME, null, null);

		// Call the method to test
		handler.handleTextMessage(user1Session, joinGameMessage);
		checkJoinGameMessagesSentToSession(user1Session, 2, USER_1_ID);

		handler.handleTextMessage(user1Session, joinGameMessage);
		verify(user1Session, times(3)).sendMessage(any());
		verify(user1Session, times(1)).sendMessage(new TextMessage("You have already joined the game!"));
	}

	@Test
	public void test_join_game_with_two_players() throws Exception {

		// Create a GameHandler instance
		GameHandler handler = new GameHandler(betService, jwtAuthenticationService);

		TextMessage joinGameMessage = getTextMessage(JOIN_GAME, null, null);

		// Call the method to test
		handler.handleTextMessage(user1Session, joinGameMessage);
		checkJoinGameMessagesSentToSession(user1Session, 2, USER_1_ID);

		setUpAuth(false);

		handler.handleTextMessage(user2Session, joinGameMessage);
		checkJoinGameMessagesSentToSession(user2Session, 2, USER_2_ID);

		verify(user1Session, times(3)).sendMessage(any());
		verify(user1Session, times(1)).sendMessage(new TextMessage(String.format("Player %s joined the game", USER_2_ID)));
	}

	@Test
	public void test_place_bet() throws Exception {

		// Create a GameHandler instance
		GameHandler handler = new GameHandler(betService, jwtAuthenticationService);

		TextMessage joinGameMessage = getTextMessage(JOIN_GAME, null, null);

		// Call the method to test
		handler.handleTextMessage(user1Session, joinGameMessage);
		checkJoinGameMessagesSentToSession(user1Session, 2, USER_1_ID);

		BigDecimal betAmount = BigDecimal.valueOf(100);
		short chosenNumber = (short) 5;
		TextMessage placeBetMessage = getTextMessage(PLACE_BET, chosenNumber, betAmount);

		handler.handleTextMessage(user1Session, placeBetMessage);
		checkPlaceBetMessagesSentToSession(user1Session, 4, USER_1_ID, chosenNumber, betAmount);

		verify(betService, times(1)).placeBet(chosenNumber, betAmount);
	}

	@Test
	public void test_place_bet_without_joining_game_error() throws Exception {

		// Create a GameHandler instance
		GameHandler handler = new GameHandler(betService, jwtAuthenticationService);

		BigDecimal betAmount = BigDecimal.valueOf(100);
		short chosenNumber = (short) 5;
		TextMessage placeBetMessage = getTextMessage(PLACE_BET, chosenNumber, betAmount);

		handler.handleTextMessage(user1Session, placeBetMessage);
		verify(user1Session, times(1)).sendMessage(any());
		verify(user1Session, times(1)).sendMessage(new TextMessage("You need to join the game before you can place a bet"));

		verify(betService, times(0)).placeBet(any(Short.class), any());
	}

	@Test
	public void test_place_bet_with_two_players() throws Exception {

		// Create a GameHandler instance
		GameHandler handler = new GameHandler(betService, jwtAuthenticationService);

		TextMessage joinGameMessage = getTextMessage(JOIN_GAME, null, null);

		// Player 1 actions
		handler.handleTextMessage(user1Session, joinGameMessage);
		checkJoinGameMessagesSentToSession(user1Session, 2, USER_1_ID);

		BigDecimal player1BetAmount = BigDecimal.valueOf(100);
		short player1ChosenNumber = (short) 5;
		TextMessage player1PlaceBetMessage = getTextMessage(PLACE_BET, player1ChosenNumber, player1BetAmount);

		handler.handleTextMessage(user1Session, player1PlaceBetMessage);
		checkPlaceBetMessagesSentToSession(user1Session, 4, USER_1_ID, player1ChosenNumber, player1BetAmount);

		verify(betService, times(1)).placeBet(player1ChosenNumber, player1BetAmount);

		// Player 2 actions
		setUpAuth(false);
		handler.handleTextMessage(user2Session, joinGameMessage);
		checkJoinGameMessagesSentToSession(user2Session, 2, USER_2_ID);

		verify(user1Session, times(5)).sendMessage(any());
		verify(user1Session, times(1)).sendMessage(new TextMessage(String.format("Player %s joined the game", USER_2_ID)));

		BigDecimal player2BetAmount = BigDecimal.valueOf(200);
		short player2ChosenNumber = (short) 10;
		TextMessage player2PlaceBetMessage = getTextMessage(PLACE_BET, player2ChosenNumber, player2BetAmount);

		handler.handleTextMessage(user2Session, player2PlaceBetMessage);
		checkPlaceBetMessagesSentToSession(user2Session, 4, USER_2_ID, player2ChosenNumber, player2BetAmount);

		verify(user1Session, times(6)).sendMessage(any());
		verify(user1Session, times(1)).sendMessage(new TextMessage(String.format("Player %s placed a bet on number %d for %s euros", USER_2_ID, player2ChosenNumber, player2BetAmount)));

		verify(betService, times(2)).placeBet(any(Short.class), any());
		verify(betService, times(1)).placeBet(player2ChosenNumber, player2BetAmount);
	}

	@Test
	public void test_leave_game() throws Exception {

		// Create a GameHandler instance
		GameHandler handler = new GameHandler(betService, jwtAuthenticationService);

		TextMessage joinTableMessage = getTextMessage(JOIN_GAME, null, null);
		TextMessage leaveGameMessage = getTextMessage(LEAVE_GAME, null, null);

		// Call the method to test
		handler.handleTextMessage(user1Session, joinTableMessage);
		checkJoinGameMessagesSentToSession(user1Session, 2, USER_1_ID);

		handler.handleTextMessage(user1Session, leaveGameMessage);
		checkLeaveGameMessagesSentToSession(user1Session, 3);
	}

	@Test
	public void test_leave_table_error() throws Exception {

		// Create a GameHandler instance
		GameHandler handler = new GameHandler(betService, jwtAuthenticationService);

		TextMessage leaveGameMessage = getTextMessage(LEAVE_GAME, null, null);

		// Call the method to test
		handler.handleTextMessage(user1Session, leaveGameMessage);
		verify(user1Session, times(1)).sendMessage(any());
		verify(user1Session, times(1)).sendMessage(new TextMessage("You have not joined the game"));
	}

	@Test
	public void test_leave_game_with_two_players() throws Exception {

		// Create a GameHandler instance
		GameHandler handler = new GameHandler(betService, jwtAuthenticationService);

		TextMessage joinTableMessage = getTextMessage(JOIN_GAME, null, null);
		TextMessage leaveGameMessage = getTextMessage(LEAVE_GAME, null, null);

		// Call the method to test
		handler.handleTextMessage(user1Session, joinTableMessage);
		checkJoinGameMessagesSentToSession(user1Session, 2, USER_1_ID);

		setUpAuth(false);
		handler.handleTextMessage(user2Session, joinTableMessage);
		checkJoinGameMessagesSentToSession(user2Session, 2, USER_2_ID);

		verify(user1Session, times(3)).sendMessage(any());
		verify(user1Session, times(1)).sendMessage(new TextMessage(String.format("Player %s joined the game", USER_2_ID)));

		setUpAuth(true);
		handler.handleTextMessage(user1Session, leaveGameMessage);
		checkLeaveGameMessagesSentToSession(user1Session, 4);

		verify(user2Session, times(3)).sendMessage(any());
		verify(user2Session, times(1)).sendMessage(new TextMessage(String.format("Player %s left the game", USER_1_ID)));

		setUpAuth(false);
		handler.handleTextMessage(user2Session, leaveGameMessage);
		checkLeaveGameMessagesSentToSession(user2Session, 4);

		verify(user1Session, times(4)).sendMessage(any());
	}

	private void checkJoinGameMessagesSentToSession(WebSocketSession session, int totalMessagesSent, UUID userId) throws IOException {
		verify(session, times(totalMessagesSent)).sendMessage(any());
		verify(session, times(1)).sendMessage(new TextMessage("Welcome to the number guessing game!"));
		verify(session, times(1)).sendMessage(new TextMessage(String.format("Player %s joined the game", userId)));
	}

	private void checkPlaceBetMessagesSentToSession(WebSocketSession session, int totalMessagesSent, UUID userId, short chosenNumber, BigDecimal betAmount) throws IOException {
		verify(session, times(totalMessagesSent)).sendMessage(any());
		verify(session, times(1)).sendMessage(new TextMessage(String.format("You have placed a bet on number %d for %s euros", chosenNumber, betAmount)));
		verify(session, times(1)).sendMessage(new TextMessage(String.format("Player %s placed a bet on number %d for %s euros", userId, chosenNumber, betAmount)));
	}

	private void checkLeaveGameMessagesSentToSession(WebSocketSession session, int totalMessagesSent) throws IOException {
		verify(session, times(totalMessagesSent)).sendMessage(any());
		verify(session, times(1)).sendMessage(new TextMessage("You have left the game"));
	}

	private TextMessage getTextMessage(WebSocketMessageIncomingType type, Short chosenNumber, BigDecimal betAmount) {
		WebSocketMessageIncoming webSocketMessageIncoming = new WebSocketMessageIncoming(type, chosenNumber, betAmount);

		return new TextMessage(new Gson().toJson(webSocketMessageIncoming));
	}
}
