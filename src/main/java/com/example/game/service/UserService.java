package com.example.game.service;

import com.example.game.db.model.User;
import com.example.game.db.repository.UserRepository;
import com.example.game.dto.mapper.UserMapper;
import com.example.game.dto.model.JwtTokenDto;
import com.example.game.dto.model.UpdatePasswordDto;
import com.example.game.dto.model.UpdateUserDto;
import com.example.game.dto.model.UserDto;
import com.example.game.exception.ResourceNotFoundException;
import com.example.game.exception.ValidationException;
import com.example.game.rest.jwt.JwtTokenUtil;
import com.example.game.rest.jwt.JwtUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasText;

@Service
@Slf4j
public class UserService extends BaseService {

	private final UserMapper mapper;
	private final PasswordEncoder bCryptPasswordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenUtil jwtTokenUtil;
	private final JwtUserDetailsService userDetailsService;

	public UserService(UserRepository userRepository, UserMapper mapper,
	                   PasswordEncoder bCryptPasswordEncoder, AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, JwtUserDetailsService userDetailsService
	) {
		super(userRepository);
		this.mapper = mapper;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtTokenUtil = jwtTokenUtil;
		this.userDetailsService = userDetailsService;
	}

	public JwtTokenDto authenticate(String email, String password) {
		User user;
		try {
			user = userRepository.getByEmail(email.toLowerCase());
		} catch (ResourceNotFoundException e) {
			throw new UsernameNotFoundException("User not found");
		}
		authenticateUser(user.getName(), password);
		UserDetails userDetails = userDetailsService.loadUserByUsername(user.getName());
		return new JwtTokenDto(jwtTokenUtil.generateToken(userDetails));
	}

	private void authenticateUser(String name, String password) {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(name, password));
		} catch (Exception e) {
			throw new AccessDeniedException("Wrong password", e);
		}
	}

	@Transactional
	public UUID create(UserDto dto) {
		validateEmail(dto);
		validateName(dto.name());

		User user = User.builder()
			.name(dto.name())
			.balance(BigDecimal.ZERO)
			.build();

		user.setEmail(dto.email().trim().toLowerCase());
		user.setPassword(bCryptPasswordEncoder.encode(dto.password()));

		userRepository.save(user);

		log.debug("Created user: {}", user.getId());

		return user.getId();
	}

	private void validateEmail(UserDto dto) {
		userRepository.findByEmailAndActiveTrue(dto.email().trim().toLowerCase()).ifPresent(user -> {
			throw new ValidationException("User already exists");
		});
	}

	private void validateName(String name) {
		if (name.length() > 100) {
			throw new ValidationException("User name max length is 100");
		}

		userRepository.findByNameAndActiveTrue(name).ifPresent(user -> {
			throw new ValidationException("User already exists");
		});
	}

	public UserDto get() {
		User user = getAuthenticatedUser();
		return mapper.convert(user);
	}

	public UUID update(UpdateUserDto dto) {
		User user = getAuthenticatedUser();

		if (!user.getName().equals(dto.name())) {
			validateName(dto.name());
			user.setName(dto.name());
		}

		if (hasText(dto.password())) {
			user.setPassword(bCryptPasswordEncoder.encode(dto.password()));
		}

		userRepository.save(user);

		log.debug("Updated user: {}", user.getId());

		return user.getId();
	}

	public void update(UpdatePasswordDto dto) {
		User entity = getAuthenticatedUser();

		if (!bCryptPasswordEncoder.matches(dto.currentPassword(), entity.getPassword())) {
			throw new ValidationException("Current password is incorrect");
		} else if (bCryptPasswordEncoder.matches(dto.newPassword(), entity.getPassword())) {
			throw new ValidationException("New password must be different from current password");
		}

		entity.setPassword(bCryptPasswordEncoder.encode(dto.newPassword()));
		userRepository.save(entity);
	}

	@Transactional
	public BigDecimal balanceTopUp(BigDecimal amount) {
		User user = getAuthenticatedUser();

		// Payment system is out of scope

		addToBalance(amount, user);

		return user.getBalance();
	}

	@Transactional
	public void addToBalance(BigDecimal amount, User user) {
		user.setBalance(user.getBalance().add(amount));
		userRepository.save(user);
	}
}
