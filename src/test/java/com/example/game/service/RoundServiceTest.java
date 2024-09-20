package com.example.game.service;

import com.example.game.BaseTest;
import com.example.game.db.model.Round;
import com.example.game.db.model.User;
import com.example.game.db.repository.RoundRepository;
import com.example.game.db.repository.UserRepository;
import com.example.game.dto.enums.WebSocketMessageOutgoingType;
import com.example.game.dto.model.WebSocketMessageOutgoing;
import com.example.game.dto.model.WinnerDto;
import com.example.game.websocket.handler.GameHandler;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.example.game.DbHelper.ROUND_2_ID;
import static com.example.game.DbHelper.ROUND_2_NO_WINNERS_NUMBER;
import static com.example.game.DbHelper.ROUND_2_START;
import static com.example.game.DbHelper.ROUND_2_USER_1_BET_AMOUNT;
import static com.example.game.DbHelper.ROUND_2_USER_1_CHOSEN_NUMBER;
import static com.example.game.DbHelper.ROUND_2_USER_2_BET_AMOUNT;
import static com.example.game.DbHelper.ROUND_2_USER_2_BET_ID;
import static com.example.game.DbHelper.ROUND_2_USER_2_CHOSEN_NUMBER;
import static com.example.game.DbHelper.USER_1_BALANCE;
import static com.example.game.DbHelper.USER_1_ID;
import static com.example.game.DbHelper.USER_1_NAME;
import static com.example.game.DbHelper.USER_2_BALANCE;
import static com.example.game.DbHelper.USER_2_ID;
import static com.example.game.DbHelper.USER_2_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Sql({
	"/sql/users.sql",
	"/sql/rounds.sql",
	"/sql/bets_round_1.sql",
	"/sql/bets_round_2.sql",
})
public class RoundServiceTest extends BaseTest {

	@Autowired
	private RoundRepository repository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoundService service;

	@MockBean
	private RandomizerService randomizerService;

	@SpyBean
	private GameHandler gameHandler;

	@Value("${game.round.multiplier}")
	private BigDecimal winMultiplier;

	@Test
	public void test_start_round() {
		OffsetDateTime now = ROUND_2_START.plusSeconds(30);
		freezeClockAt(now);

		service.startRound();
		Round round = repository.findLatestRound().get();

		assertThat(round.getStartedAt().toInstant(), is(now.toInstant()));

		WebSocketMessageOutgoing message = WebSocketMessageOutgoing.builder()
			.type(WebSocketMessageOutgoingType.ROUND_STARTED)
			.build();

		verify(gameHandler, times(1)).sendMessageActivePlayers(new Gson().toJson(message));
	}

	@Test
	public void test_play_round_no_user_wins() {
		OffsetDateTime now = ROUND_2_START.plusSeconds(11);
		freezeClockAt(now);
		when(randomizerService.generateRandomNumber()).thenReturn(ROUND_2_NO_WINNERS_NUMBER);

		Round round2 = repository.findById(ROUND_2_ID).get();
		service.playRound(round2);

		assertUserBalance(USER_1_ID, USER_1_BALANCE);
		assertUserBalance(USER_2_ID, USER_2_BALANCE);

		BigDecimal totalBet = ROUND_2_USER_1_BET_AMOUNT.add(ROUND_2_USER_2_BET_AMOUNT);
		BigDecimal totalWon = BigDecimal.ZERO;
		assertRound(repository.findById(ROUND_2_ID).get(), ROUND_2_START, ROUND_2_NO_WINNERS_NUMBER, totalBet, totalWon, 2, now);

		WebSocketMessageOutgoing message = WebSocketMessageOutgoing.builder()
			.type(WebSocketMessageOutgoingType.ROUND_RESULT)
			.winners(Collections.emptyList())
			.winningNumber(ROUND_2_NO_WINNERS_NUMBER)
			.totalBet(totalBet)
			.totalWon(totalWon)
			.playerCount(2)
			.build();

		verify(gameHandler, times(1)).sendMessageActivePlayers(new Gson().toJson(message));
	}


	@Test
	public void test_play_round_user_1_wins() {
		OffsetDateTime now = ROUND_2_START.plusSeconds(11);
		freezeClockAt(now);
		when(randomizerService.generateRandomNumber()).thenReturn(ROUND_2_USER_1_CHOSEN_NUMBER);

		Round round2 = repository.findById(ROUND_2_ID).get();
		service.playRound(round2);

		assertUserBalance(USER_1_ID, USER_1_BALANCE.add(ROUND_2_USER_1_BET_AMOUNT.multiply(winMultiplier)));
		assertUserBalance(USER_2_ID, USER_2_BALANCE);

		BigDecimal totalBet = ROUND_2_USER_1_BET_AMOUNT.add(ROUND_2_USER_2_BET_AMOUNT);
		BigDecimal totalWon = ROUND_2_USER_1_BET_AMOUNT.multiply(winMultiplier);
		assertRound(repository.findById(ROUND_2_ID).get(), ROUND_2_START, ROUND_2_USER_1_CHOSEN_NUMBER, totalBet, totalWon, 2, now);

		WebSocketMessageOutgoing message = WebSocketMessageOutgoing.builder()
			.type(WebSocketMessageOutgoingType.ROUND_RESULT)
			.winners(List.of(new WinnerDto(USER_1_NAME, totalWon)))
			.winningNumber(ROUND_2_USER_1_CHOSEN_NUMBER)
			.totalBet(totalBet)
			.totalWon(totalWon)
			.playerCount(2)
			.build();

		verify(gameHandler, times(1)).sendMessageActivePlayers(new Gson().toJson(message));
	}

	@Test
	public void test_play_round_user_2_wins() {
		OffsetDateTime now = ROUND_2_START.plusSeconds(11);
		freezeClockAt(now);
		when(randomizerService.generateRandomNumber()).thenReturn(ROUND_2_USER_2_CHOSEN_NUMBER);

		Round round2 = repository.findById(ROUND_2_ID).get();
		service.playRound(round2);

		assertUserBalance(USER_1_ID, USER_1_BALANCE);
		assertUserBalance(USER_2_ID, USER_2_BALANCE.add(ROUND_2_USER_2_BET_AMOUNT.multiply(winMultiplier)));

		BigDecimal totalBet = ROUND_2_USER_1_BET_AMOUNT.add(ROUND_2_USER_2_BET_AMOUNT);
		BigDecimal totalWon = ROUND_2_USER_2_BET_AMOUNT.multiply(winMultiplier);
		assertRound(repository.findById(ROUND_2_ID).get(), ROUND_2_START, ROUND_2_USER_2_CHOSEN_NUMBER, totalBet, totalWon, 2, now);

		WebSocketMessageOutgoing message = WebSocketMessageOutgoing.builder()
			.type(WebSocketMessageOutgoingType.ROUND_RESULT)
			.winners(List.of(new WinnerDto(USER_2_NAME, totalWon)))
			.winningNumber(ROUND_2_USER_2_CHOSEN_NUMBER)
			.totalBet(totalBet)
			.totalWon(totalWon)
			.playerCount(2)
			.build();

		verify(gameHandler, times(1)).sendMessageActivePlayers(new Gson().toJson(message));
	}

	@Test
	public void test_play_round_both_users_win() {
		OffsetDateTime now = ROUND_2_START.plusSeconds(11);
		freezeClockAt(now);
		when(randomizerService.generateRandomNumber()).thenReturn(ROUND_2_USER_1_CHOSEN_NUMBER);

		executeJpql("update Bet b set b.chosenNumber = :value where b.id = :id", Map.of(
			"value", ROUND_2_USER_1_CHOSEN_NUMBER,
			"id", ROUND_2_USER_2_BET_ID
		));

		Round round2 = repository.findById(ROUND_2_ID).get();
		service.playRound(round2);

		assertUserBalance(USER_1_ID, USER_1_BALANCE.add(ROUND_2_USER_1_BET_AMOUNT.multiply(winMultiplier)));
		assertUserBalance(USER_2_ID, USER_2_BALANCE.add(ROUND_2_USER_2_BET_AMOUNT.multiply(winMultiplier)));

		BigDecimal totalBet = ROUND_2_USER_1_BET_AMOUNT.add(ROUND_2_USER_2_BET_AMOUNT);
		BigDecimal totalWon = totalBet.multiply(winMultiplier);
		assertRound(repository.findById(ROUND_2_ID).get(), ROUND_2_START, ROUND_2_USER_1_CHOSEN_NUMBER, totalBet, totalWon, 2, now);

		WebSocketMessageOutgoing message = WebSocketMessageOutgoing.builder()
			.type(WebSocketMessageOutgoingType.ROUND_RESULT)
			.winners(List.of(
				new WinnerDto(USER_1_NAME, ROUND_2_USER_1_BET_AMOUNT.multiply(winMultiplier)),
				new WinnerDto(USER_2_NAME, ROUND_2_USER_2_BET_AMOUNT.multiply(winMultiplier))
			))
			.winningNumber(ROUND_2_USER_1_CHOSEN_NUMBER)
			.totalBet(totalBet)
			.totalWon(totalWon)
			.playerCount(2)
			.build();

		verify(gameHandler, times(1)).sendMessageActivePlayers(new Gson().toJson(message));
	}

	@Test
	public void test_finish_round() {
		OffsetDateTime now = ROUND_2_START.plusSeconds(11);
		freezeClockAt(now);

		Round round = repository.findLatestRound().get();
		service.finishRound(round);

		clearHibernateFirstLevelCache();

		round = repository.findLatestRound().get();

		assertThat(round.getFinishedAt().toInstant(), is(now.toInstant()));

		WebSocketMessageOutgoing message = WebSocketMessageOutgoing.builder()
			.type(WebSocketMessageOutgoingType.ROUND_FINISHED)
			.build();

		verify(gameHandler, times(1)).sendMessageActivePlayers(new Gson().toJson(message));
	}

	private void assertRound(Round round, OffsetDateTime roundStart, int result, BigDecimal totalBet, BigDecimal totalWon, int playerCount, OffsetDateTime resultAt) {
		assertThat(round, is(notNullValue()));
		assertThat(round.getStartedAt(), is(roundStart));
		assertThat(round.getResult(), is(result));
		assertThat(round.getTotalBet(), is(totalBet));
		assertThat(round.getTotalWon(), is(totalWon));
		assertThat(round.getPlayerCount(), is(playerCount));
		assertThat(round.getResultAt().toInstant(), is(resultAt.toInstant()));
	}

	private void assertUserBalance(UUID userId, BigDecimal balance) {

		User user = userRepository.findById(userId).get();
		assertThat(user, is(notNullValue()));
		assertThat(user.getId(), is(userId));
		assertThat(user.getBalance(), is(balance));
	}
}
