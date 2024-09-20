package com.example.game.dto.model;

import com.example.game.dto.enums.WebSocketMessageOutgoingType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class WebSocketMessageOutgoing {

	private WebSocketMessageOutgoingType type;

	private List<WinnerDto> winners;

	private Integer winningNumber;

	private BigDecimal totalBet;

	private BigDecimal totalWon;

	private Integer playerCount;
}
