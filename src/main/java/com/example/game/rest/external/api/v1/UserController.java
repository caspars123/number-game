package com.example.game.rest.external.api.v1;

import com.example.game.dto.model.JwtRequestDto;
import com.example.game.dto.model.JwtTokenDto;
import com.example.game.dto.model.UpdatePasswordDto;
import com.example.game.dto.model.UpdateUserDto;
import com.example.game.dto.model.UserDto;
import com.example.game.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

import static com.example.game.util.Constants.API_V1;

@RestController
@RequestMapping(API_V1 + "users")
@RequiredArgsConstructor
@Tag(name = "User", description = "Endpoints for user operations")
public class UserController {

	private final UserService service;

	@GetMapping("logged-in")
	@Operation(summary = "Get currently logged in user data")
	public UserDto get() {
		return service.get();
	}

	@PostMapping("auth")
	@Operation(summary = "Authenticate user login")
	public JwtTokenDto authenticate(@Valid @RequestBody JwtRequestDto dto) {
		return service.authenticate(dto.email(), dto.password());
	}

	@PostMapping("/register")
	@ApiResponse(responseCode = "201", description = "User created")
	@Operation(summary = "Create new user")
	public void register(@Valid @RequestBody UserDto dto) {
		service.create(dto);
	}

	@PutMapping
	@Operation(summary = "Update currently logged in user data")
	public UUID update(@Valid @RequestBody UpdateUserDto dto) {
		return service.update(dto);
	}

	@PatchMapping
	@Operation(summary = "Update currently logged in user password")
	public void update(@RequestBody UpdatePasswordDto dto) {
		service.update(dto);
	}

	@PostMapping("/add-to-balance")
	@Operation(summary = "Add money to your balance")
	public BigDecimal balanceTopUp(@RequestParam BigDecimal amount) {
		return service.balanceTopUp(amount);
	}
}