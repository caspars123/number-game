package com.example.game.db.repository;

import com.example.game.db.model.User;
import com.example.game.exception.ResourceNotFoundException;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends BaseEntityRepository<User> {

	Optional<User> findByEmailAndActiveTrue(String email);

	Optional<User> findByNameAndActiveTrue(String name);

	Optional<User> findByName(String name);

	default User getByEmail(String email) {
		return findByEmailAndActiveTrue(email.toLowerCase())
			.orElseThrow(() -> new ResourceNotFoundException("User not found", Map.of("email", email)));
	}

	default User getById(UUID id) {
		return findByIdAndActiveTrue(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found", Map.of("id", id)));
	}

	Optional<User> findByIdAndActiveTrue(UUID id);
}
