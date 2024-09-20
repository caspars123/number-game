package com.example.game.db.repository;

import com.example.game.db.model.Bet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BetRepository extends BaseEntityRepository<Bet> {

	Optional<Bet> findByUserIdAndRoundId(UUID userId, UUID roundId);

	List<Bet> findAllByRoundId(UUID roundId);
}
