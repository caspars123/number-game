package com.example.game.dto.model;

import jakarta.validation.constraints.NotBlank;

public record UpdatePasswordDto(@NotBlank String currentPassword,
                                @NotBlank String newPassword) {
}
