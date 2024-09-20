package com.example.game.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "round")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Round extends BaseEntity {

	@Column(name = "started_at", nullable = false, updatable = false)
	private OffsetDateTime startedAt;

	@Column(name = "result")
	private Integer result;

	@Column(name = "total_bet")
	private BigDecimal totalBet;

	@Column(name = "total_won")
	private BigDecimal totalWon;

	@Column(name = "player_count")
	private Integer playerCount;

	@Column(name = "finished_at")
	private OffsetDateTime finishedAt;

	@Column(name = "result_at")
	private OffsetDateTime resultAt;
}