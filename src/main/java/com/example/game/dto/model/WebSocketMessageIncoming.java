package com.example.game.dto.model;

import com.example.game.dto.enums.WebSocketMessageIncomingType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class WebSocketMessageIncoming {

	private WebSocketMessageIncomingType type;

	private Short chosenNumber;

	private BigDecimal betAmount;
}
