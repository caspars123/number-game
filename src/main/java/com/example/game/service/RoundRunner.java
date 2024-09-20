package com.example.game.service;

import com.example.game.db.model.Round;
import com.example.game.db.repository.RoundRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.example.game.util.DateUtil.now;

@Service
@Slf4j
public class RoundRunner {

	private final RoundRepository repository;
	private final RoundService roundService;

	@Value("${game.round.duration}")
	private int roundDuration;

	@Value("${game.round.newRoundStartDelay}")
	private int newRoundStartDelay;

	@Value("${game.round.rollResultDelayNanos}")
	private int rollResultDelayNanos;

	public RoundRunner(RoundRepository repository, RoundService roundService) {
		this.repository = repository;
		this.roundService = roundService;
	}

	@Scheduled(fixedRateString = "${game.round.runner.fixedRate}", initialDelayString = "${game.round.runner.initialDelay}")
	public void run() {
		Optional<Round> latestRound = repository.findLatestRound();

		if (latestRound.isEmpty()) {
			roundService.startRound();
			return;
		}

		Round round = latestRound.get();
		if (round.getResult() != null) {
			if (round.getStartedAt().plusSeconds(roundDuration).plusSeconds(newRoundStartDelay).isBefore(now())) {
				roundService.startRound();
			}
			return;
		}

		if (round.getFinishedAt() == null && now().isAfter(round.getStartedAt().plusSeconds(roundDuration))) {
			roundService.finishRound(round);
		}

		if (now().isAfter(round.getStartedAt().plusSeconds(roundDuration).plusNanos(rollResultDelayNanos))) {
			roundService.playRound(round);
		}
	}
}
