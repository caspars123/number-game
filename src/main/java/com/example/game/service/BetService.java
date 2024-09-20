package com.example.game.service;

import com.example.game.db.model.Bet;
import com.example.game.db.model.Round;
import com.example.game.db.model.User;
import com.example.game.db.repository.BetRepository;
import com.example.game.db.repository.RoundRepository;
import com.example.game.db.repository.UserRepository;
import com.example.game.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.example.game.util.DateUtil.now;

@Service
@Slf4j
public class BetService extends BaseService {

	private final RoundRepository roundRepository;
	private final BetRepository repository;

	@Value("${game.round.duration}")
	private int roundDuration;

	@Value("${game.round.betDelayLeewayNanos}")
	private int betDelayLeewayNanos;

	public BetService(UserRepository userRepository, RoundRepository roundRepository, BetRepository betRepository) {
		super(userRepository);
		this.roundRepository = roundRepository;
		this.repository = betRepository;
	}

	@Transactional
	public UUID placeBet(short chosenNumber, BigDecimal amount) {

		OffsetDateTime now = now();

		Round round = validate(chosenNumber, amount, now);

		User user = getAuthenticatedUser();
		Optional<Bet> optionalBet = repository.findByUserIdAndRoundId(user.getId(), round.getId());

		Bet bet;
		if (optionalBet.isPresent()) {

			bet = optionalBet.get();

			user.setBalance(
				user.getBalance()
					.add(bet.getAmount())
					.subtract(amount)
			);

			bet.setChosenNumber(chosenNumber);
			bet.setAmount(amount);
			bet.setPlacedAt(now);
		} else {

			user.setBalance(user.getBalance().subtract(amount));


			bet = Bet.builder()
				.round(round)
				.user(user)
				.chosenNumber(chosenNumber)
				.amount(amount)
				.placedAt(now)
				.build();
		}

		if (BigDecimal.ZERO.compareTo(user.getBalance()) > 0) {
			throw new ValidationException("Not enough funds on account");
		}

		userRepository.save(user);
		return repository.save(bet).getId();
	}

	private Round validate(short chosenNumber, BigDecimal amount, OffsetDateTime now) {
		if (chosenNumber < 1 || chosenNumber > 10) {
			throw new ValidationException("Chosen number must be in-between 1 and 10");
		}

		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ValidationException("Bet amount must be greater than zero");
		}

		Optional<Round> optionalRound = roundRepository.findLatestRound();

		if (optionalRound.isEmpty()) {
			throw new ValidationException("Betting is currently not allowed");
		}

		Round round = optionalRound.get();

		// Give some leeway - takes time for request to arrive and be processed
		if (round.getStartedAt().plusSeconds(roundDuration).plusNanos(betDelayLeewayNanos).isBefore(now)) {
			throw new ValidationException("Bet was placed after round was over");
		}

		return round;
	}
}
