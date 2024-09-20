package com.example.game.dto.model;

import com.example.game.dto.enums.WebSocketMessageIncomingType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WebSocketMessageIncoming {

	private WebSocketMessageIncomingType type;

	private BigDecimal betAmount;

	private short chosenNumber;
}
