package com.example.game.dto.model;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserDto(@NotBlank String name, String password) {

}
