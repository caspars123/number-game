package com.example.game.db.repository;

import com.example.game.db.model.Round;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RoundRepository extends BaseEntityRepository<Round> {

	@Query("SELECT r from Round r ORDER BY r.startedAt DESC limit 1")
	Optional<Round> findLatestRound();
}
