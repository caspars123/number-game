package com.example.game.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "bet")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Bet extends BaseEntity {

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "round_id", nullable = false, updatable = false)
	private Round round;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "user_id", nullable = false, updatable = false)
	private User user;

	@Column(name = "chosen_number", nullable = false)
	private short chosenNumber;

	@Column(name = "amount", nullable = false)
	private BigDecimal amount;

	@Column(name = "placed_at", nullable = false)
	private OffsetDateTime placedAt;
}