package com.example.game.service;

import com.example.game.BaseTest;
import com.example.game.db.model.User;
import com.example.game.db.repository.UserRepository;
import com.example.game.dto.model.JwtTokenDto;
import com.example.game.dto.model.UpdatePasswordDto;
import com.example.game.dto.model.UpdateUserDto;
import com.example.game.dto.model.UserDto;
import com.example.game.exception.ValidationException;
import com.example.game.rest.jwt.JwtAuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.UUID;

import static com.example.game.DbHelper.NON_EXISTENT_USER_EMAIL;
import static com.example.game.DbHelper.USER_1_BALANCE;
import static com.example.game.DbHelper.USER_1_EMAIL;
import static com.example.game.DbHelper.USER_1_ID;
import static com.example.game.DbHelper.USER_1_NAME;
import static com.example.game.DbHelper.USER_1_PASSWORD_TEXT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Sql({
	"/sql/users.sql",
})
public class UserServiceTest extends BaseTest {

	@Autowired
	private UserRepository repository;

	@Autowired
	private UserService service;

	@Autowired
	private JwtAuthenticationService jwtAuthenticationService;

	@Autowired
	private PasswordEncoder bCryptPasswordEncoder;

	@Test
	public void test_get_user() {
		String email = " newUser1@gmail.com  ";
		String password = "password123";
		String name = "New User 1";
		UUID id = service.create(new UserDto(null, email, password, name, null));

		assertUser(repository.findById(id).get(), email.trim().toLowerCase(), name, BigDecimal.ZERO);
	}

	@Test
	public void test_register_user() {
		String email = " newUser1@gmail.com  ";
		String password = "password";
		String name = "New User 1";
		UUID id = service.create(new UserDto(null, email, password, name, null));

		assertUser(repository.findById(id).get(), email.trim().toLowerCase(), name, BigDecimal.ZERO);
	}

	@Test
	public void test_authenticate_user() {
		JwtTokenDto jwtTokenDto = service.authenticate(USER_1_EMAIL, USER_1_PASSWORD_TEXT);

		jwtAuthenticationService.authenticate(jwtTokenDto.token(), false);

		assertEquals(SecurityContextHolder.getContext().getAuthentication().getName(), USER_1_NAME);
	}

	@Test
	public void test_authenticate_user_not_found() {
		validateException(
			() -> service.authenticate(NON_EXISTENT_USER_EMAIL, USER_1_PASSWORD_TEXT),
			UsernameNotFoundException.class,
			"User not found"
		);
	}

	@Test
	public void test_authenticate_wrong_password() {
		validateException(
			() -> service.authenticate(USER_1_EMAIL, USER_1_PASSWORD_TEXT + "123"),
			AccessDeniedException.class,
			"Wrong password"
		);
	}

	@Test
	public void test_update_user() {

		String updatedPassword = "updatedPassword";
		String name = "Updated User 1";
		service.update(new UpdateUserDto(name, updatedPassword));

		assertUser(repository.findById(USER_1_ID).get(), USER_1_EMAIL, name, USER_1_BALANCE, true, USER_1_PASSWORD_TEXT);
	}

	@Test
	public void test_update_user_same_password() {

		String updatedPassword = "password";
		String name = "Updated User 1";
		service.update(new UpdateUserDto(name, updatedPassword));

		assertUser(repository.findById(USER_1_ID).get(), USER_1_EMAIL, name, USER_1_BALANCE, false, USER_1_PASSWORD_TEXT);
	}

	@Test
	public void test_update_user_password() {

		String updatedPassword = "updatedPassword";
		service.update(new UpdatePasswordDto(USER_1_PASSWORD_TEXT, updatedPassword));

		assertUser(repository.findById(USER_1_ID).get(), USER_1_EMAIL, USER_1_NAME, USER_1_BALANCE, true, USER_1_PASSWORD_TEXT);
	}

	@Test
	public void test_update_user_password_incorrect_current_password_assert_throws() {

		validateException(
			() -> service.update(new UpdatePasswordDto(USER_1_PASSWORD_TEXT + "123", USER_1_PASSWORD_TEXT)),
			ValidationException.class,
			"Request validation failed: Current password is incorrect"
		);
	}

	@Test
	public void test_update_user_password_use_same_password_assert_throws() {

		validateException(
			() -> service.update(new UpdatePasswordDto(USER_1_PASSWORD_TEXT, USER_1_PASSWORD_TEXT)),
			ValidationException.class,
			"Request validation failed: New password must be different from current password"
		);
	}

	@Test
	public void test_update_user_balance_top_up() {

		BigDecimal amount = BigDecimal.valueOf(500);
		service.balanceTopUp(amount);

		assertUser(repository.findById(USER_1_ID).get(), USER_1_EMAIL, USER_1_NAME, USER_1_BALANCE.add(amount));
	}

	public void assertUser(User user, String email, String name, BigDecimal balance) {
		assertUser(user, email, name, balance, null, null);
	}

	public void assertUser(User user, String email, String name, BigDecimal balance, Boolean passwordChanged, String oldPassword) {

		assertThat(user, is(notNullValue()));
		assertThat(user.getEmail(), is(email));
		assertThat(user.getName(), is(name));
		assertThat(user.getBalance(), is(balance));

		if (passwordChanged != null) {
			assertEquals(passwordChanged, !bCryptPasswordEncoder.matches(oldPassword, user.getPassword()));
		}
	}
}
