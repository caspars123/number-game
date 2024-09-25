package com.example.game;

import com.example.game.rest.jwt.JwtUserDetailsService;
import com.example.game.util.DateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import static com.example.game.DbHelper.NOW;
import static com.example.game.DbHelper.USER_1_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRED;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
@Rollback
@DirtiesContext
public abstract class BaseTest {

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private JwtUserDetailsService jwtUserDetailsService;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@BeforeEach
	public void setUp() {

		freezeClockAt(NOW);

		setUser(USER_1_NAME);

		// Turn off multi transactions to database.
		transactionTemplate.setPropagationBehavior(PROPAGATION_REQUIRED);
	}

	@AfterEach
	public void cleanUp() {
		useSystemClock();
	}

	protected void freezeClockAt(OffsetDateTime offsetDateTime) {
		setClock(Clock.fixed(offsetDateTime.toInstant(), ZoneOffset.UTC));
	}

	protected void useSystemClock() {
		setClock(Clock.systemDefaultZone());
	}

	protected void setClock(Clock clock) {
		try {
			Field field = DateUtil.class.getDeclaredField("CLOCK");
			field.setAccessible(true);

			field.set(null, clock);

		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to fix time for tests", e);
		}
	}

	public void setUser(String username) {
		UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);

		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
			new UsernamePasswordAuthenticationToken(
				userDetails, null, userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
	}

	protected void clearHibernateFirstLevelCache() {
		entityManager.flush();
		entityManager.clear();
	}

	protected void executeJpql(String jpql, Map<String, Object> params) {
		Query query = entityManager.createQuery(jpql);
		if (params != null) {
			params.forEach((name, value) -> {
				query.setParameter(name, value);
			});
		}

		query.executeUpdate();
	}

	protected static <T extends Exception> void validateException(Executable executable, Class<T> clazz, String msg) {

		T exception = assertThrows(clazz, executable);
		assertThat(exception.getMessage(), is(msg));
	}
}
