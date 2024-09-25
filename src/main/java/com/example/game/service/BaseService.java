package com.example.game.service;

import com.example.game.db.model.User;
import com.example.game.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RequiredArgsConstructor
public class BaseService {

	public final UserRepository userRepository;

	public User getAuthenticatedUser() {
		return userRepository.findByName(
				SecurityContextHolder.getContext()
					.getAuthentication()
					.getName())
			.orElseThrow(() -> new UsernameNotFoundException("User not found"));
	}
}
