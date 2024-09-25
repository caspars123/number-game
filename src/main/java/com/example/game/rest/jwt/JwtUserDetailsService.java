package com.example.game.rest.jwt;

import com.example.game.db.model.Role;
import com.example.game.db.model.User;
import com.example.game.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String name) {

		Optional<User> optionalUser = userRepository.findByNameAndActiveTrue(name);

		if (optionalUser.isEmpty()) {
			throw new AccessDeniedException("User not found");
		}

		User user = optionalUser.get();

		return new org.springframework.security.core.userdetails.User(
			user.getName(),
			user.getPassword(),
			getAuthorities(user.getRoles()));
	}

	private Collection<? extends GrantedAuthority> getAuthorities(Collection<Role> roles) {
		List<String> privileges = roles.stream()
			.map(role -> role.getName().name())
			.toList();

		return privileges.stream()
			.map(SimpleGrantedAuthority::new)
			.toList();
	}
}