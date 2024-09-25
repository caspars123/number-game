package com.example.game.service;

import com.example.game.db.model.Bet;
import com.example.game.db.model.Round;
import com.example.game.db.model.User;
import com.example.game.db.repository.BetRepository;
import com.example.game.db.repository.RoundRepository;
import com.example.game.db.repository.UserRepository;
import com.example.game.dto.enums.WebSocketMessageOutgoingType;
import com.example.game.dto.model.WebSocketMessageOutgoing;
import com.example.game.dto.model.WinnerDto;
import com.example.game.websocket.handler.GameHandler;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.example.game.util.DateUtil.now;

@Service
@Slf4j
public class RoundService extends BaseService {

	private final RoundRepository repository;
	private final BetRepository betRepository;
	private final RandomizerService randomizerService;
	private final GameHandler gameHandler;

	@Value("${game.round.multiplier}")
	private BigDecimal winMultiplier;

	public RoundService(UserRepository userRepository, RoundRepository roundRepository, BetRepository betRepository, RandomizerService randomizerService, GameHandler gameHandler) {
		super(userRepository);
		this.repository = roundRepository;
		this.betRepository = betRepository;
		this.randomizerService = randomizerService;
		this.gameHandler = gameHandler;
	}

	@Transactional
	public void startRound() {
		repository.save(
			Round.builder()
				.startedAt(now())
				.build()
		);

		WebSocketMessageOutgoing message = WebSocketMessageOutgoing.builder()
			.type(WebSocketMessageOutgoingType.ROUND_STARTED)
			.build();

		gameHandler.sendMessageToActivePlayers(new Gson().toJson(message));
	}

	@Transactional
	public void playRound(Round round) {

		int rolledNumber = randomizerService.generateRandomNumber();

		BigDecimal totalBetAmount = BigDecimal.valueOf(0);
		BigDecimal totalWinAmount = BigDecimal.valueOf(0);
		List<WinnerDto> winnerDtos = new ArrayList<>();

		List<Bet> bets = betRepository.findAllByRoundId(round.getId());
		List<User> betWinners = new ArrayList<>();

		for (Bet bet : bets) {
			if (bet.getChosenNumber() == rolledNumber) {
				BigDecimal winAmount = bet.getAmount().multiply(winMultiplier);
				User user = bet.getUser();

				user.setBalance(user.getBalance().add(winAmount));

				betWinners.add(user);
				winnerDtos.add(new WinnerDto(user.getName(), winAmount));
				totalWinAmount = totalWinAmount.add(winAmount);
			}
			totalBetAmount = totalBetAmount.add(bet.getAmount());
		}

		userRepository.saveAll(betWinners);
		betRepository.saveAll(bets);

		round.setResult(rolledNumber);
		round.setTotalBet(totalBetAmount);
		round.setTotalWon(totalWinAmount);
		round.setPlayerCount(bets.size());
		round.setResultAt(now());
		repository.save(round);

		WebSocketMessageOutgoing message = WebSocketMessageOutgoing.builder()
			.type(WebSocketMessageOutgoingType.ROUND_RESULT)
			.winners(winnerDtos)
			.winningNumber(round.getResult())
			.totalBet(round.getTotalBet())
			.totalWon(round.getTotalWon())
			.playerCount(round.getPlayerCount())
			.build();

		gameHandler.sendMessageToActivePlayers(new Gson().toJson(message));
	}

	@Transactional
	public void finishRound(Round round) {

		round.setFinishedAt(now());
		repository.save(round);

		WebSocketMessageOutgoing message = WebSocketMessageOutgoing.builder()
			.type(WebSocketMessageOutgoingType.ROUND_FINISHED)
			.build();

		gameHandler.sendMessageToActivePlayers(new Gson().toJson(message));
	}
}
