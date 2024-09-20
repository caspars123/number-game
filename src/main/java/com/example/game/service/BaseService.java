package com.example.game.service;

import com.example.game.db.model.User;
import com.example.game.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.UUID;

@RequiredArgsConstructor
public class BaseService {

	public final UserRepository userRepository;

	public User getAuthenticatedUser() {
		return userRepository.findById(UUID.fromString(
				SecurityContextHolder.getContext()
					.getAuthentication()
					.getName()))
			.orElseThrow(() -> new UsernameNotFoundException("User not found"));
	}
}
