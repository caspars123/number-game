package com.example.game.dto.model;

import java.math.BigDecimal;
import java.util.UUID;

public record UserDto(UUID id, String email, String password, String name, BigDecimal balance) {
}
