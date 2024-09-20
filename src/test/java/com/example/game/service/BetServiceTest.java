package com.example.game.service;

import com.example.game.BaseTest;
import com.example.game.db.model.Bet;
import com.example.game.db.model.User;
import com.example.game.db.repository.BetRepository;
import com.example.game.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static com.example.game.DbHelper.LIVE_ROUND_ID;
import static com.example.game.DbHelper.NON_EXISTENT_ROUND_ID;
import static com.example.game.DbHelper.NON_EXISTENT_ROUND_START;
import static com.example.game.DbHelper.NOW;
import static com.example.game.DbHelper.ROUND_1_ID;
import static com.example.game.DbHelper.ROUND_1_START;
import static com.example.game.DbHelper.ROUND_1_USER_1_BET_AMOUNT;
import static com.example.game.DbHelper.ROUND_2_START;
import static com.example.game.DbHelper.USER_1_BALANCE;
import static com.example.game.DbHelper.USER_1_ID;
import static com.example.game.DbHelper.USER_2_BALANCE;
import static com.example.game.DbHelper.USER_2_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

@Sql({
	"/sql/users.sql",
	"/sql/rounds.sql",
	"/sql/bets_round_1.sql"
})
public class BetServiceTest extends BaseTest {

	@Autowired
	private BetRepository repository;

	@Autowired
	private BetService service;

	@Test
	public void test_add_bet_to_new_round() {
		testBet((short) 1, BigDecimal.valueOf(10), LIVE_ROUND_ID, USER_1_ID, USER_1_BALANCE, NOW);
	}

	@Test
	public void test_add_bet_to_new_round_with_two_users() {
		testBet((short) 1, BigDecimal.valueOf(10), LIVE_ROUND_ID, USER_1_ID, USER_1_BALANCE, NOW);
		testBet((short) 5, BigDecimal.valueOf(50), LIVE_ROUND_ID, USER_2_ID, USER_2_BALANCE, NOW);
	}

	@Sql({
		"/sql/users.sql",
		"/sql/round_ongoing.sql",
		"/sql/bets_round_1.sql"
	})
	@Test
	public void test_override_bet() {
		freezeClockAt(ROUND_1_START);
		BigDecimal user1BalanceAfter1BetGetsWithdrawn = USER_1_BALANCE.add(ROUND_1_USER_1_BET_AMOUNT);
		testBet((short) 1, BigDecimal.valueOf(10), ROUND_1_ID, USER_1_ID, user1BalanceAfter1BetGetsWithdrawn, ROUND_1_START);
		testBet((short) 1, user1BalanceAfter1BetGetsWithdrawn, ROUND_1_ID, USER_1_ID, user1BalanceAfter1BetGetsWithdrawn, ROUND_1_START);
	}

	@Test
	public void test_wrong_bet_values() {
		assertThrowsErrorOnIncorrectValues((short) 0, BigDecimal.valueOf(10), "Chosen number must be in-between 1 and 10");
		assertThrowsErrorOnIncorrectValues((short) -1, BigDecimal.valueOf(10), "Chosen number must be in-between 1 and 10");
		assertThrowsErrorOnIncorrectValues((short) 11, BigDecimal.valueOf(10), "Chosen number must be in-between 1 and 10");
		assertThrowsErrorOnIncorrectValues((short) 1, BigDecimal.valueOf(0), "Bet amount must be greater than zero");
		assertThrowsErrorOnIncorrectValues((short) 1, BigDecimal.valueOf(-1), "Bet amount must be greater than zero");
		assertThrowsErrorOnIncorrectValues((short) 1, USER_1_BALANCE.add(BigDecimal.valueOf(9999999)), "Not enough funds on account");
	}

	@Sql({
		"/sql/users.sql",
		"/sql/round_ongoing.sql",
		"/sql/bets_round_1.sql"
	})
	@Test
	public void test_override_bet_not_enough_values() {
		freezeClockAt(ROUND_1_START);
		assertThrowsErrorOnIncorrectValues((short) 1, USER_1_BALANCE.add(ROUND_1_USER_1_BET_AMOUNT).add(BigDecimal.ONE), "Not enough funds on account");
	}

	@Test
	public void test_bet_on_expired_round() {
		OffsetDateTime now = ROUND_2_START.plusMinutes(20);
		freezeClockAt(now);
		validateException(
			() -> testBet((short) 2, BigDecimal.valueOf(10), ROUND_1_ID, USER_1_ID, USER_1_BALANCE, now),
			ValidationException.class,
			"Request validation failed: Bet was placed after round was over"
		);
	}

	@Sql({
		"/sql/users.sql",
		"/sql/delete_rounds.sql"
	})
	@Test
	public void test_bet_when_no_rounds_have_started_round() {
		validateException(
			() -> testBet((short) 1, BigDecimal.valueOf(10), NON_EXISTENT_ROUND_ID, USER_1_ID, USER_1_BALANCE, NON_EXISTENT_ROUND_START),
			ValidationException.class,
			"Request validation failed: Betting is currently not allowed"
		);
	}

	private void testBet(short chosenValue, BigDecimal amount, UUID roundId, UUID userId, BigDecimal userBalance, OffsetDateTime timestamp) {
		if (userId != USER_1_ID) {
			setUser(userId);
		}

		UUID betId = service.placeBet(chosenValue, amount);

		Bet bet = repository.findById(betId).get();

		assertBet(bet, roundId, userId, chosenValue, amount, timestamp);
		assertUserBalance(userId, userBalance.subtract(amount));
	}

	private void assertUserBalance(UUID userId, BigDecimal balance) {

		User user = service.getAuthenticatedUser();
		assertThat(user, is(notNullValue()));
		assertThat(user.getId(), is(userId));
		assertThat(user.getBalance(), is(balance));
	}

	public void assertBet(Bet bet, UUID roundId, UUID userId, short chosenNumber, BigDecimal amount, OffsetDateTime placedAt) {

		assertThat(bet, is(notNullValue()));
		assertThat(bet.getRound().getId(), is(roundId));
		assertThat(bet.getUser().getId(), is(userId));
		assertThat(bet.getChosenNumber(), is(chosenNumber));
		assertThat(bet.getAmount(), is(amount));
		assertThat(bet.getPlacedAt(), is(placedAt));
	}

	private void assertThrowsErrorOnIncorrectValues(short chosenNumber, BigDecimal amount, String errorMessage) {
		validateException(
			() -> testBet(chosenNumber, amount, LIVE_ROUND_ID, USER_1_ID, USER_1_BALANCE, NOW),
			ValidationException.class,
			"Request validation failed: " + errorMessage
		);
	}
}
