package com.example.game.dto.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record JwtRequestDto(@Email @NotBlank String email, @NotBlank String password) {

}
