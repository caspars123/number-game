package com.example.game.rest.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

	private final JwtAuthenticationService jwtAuthenticationService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
	                                @NonNull FilterChain chain) throws ServletException, IOException {

		final String requestTokenHeader = request.getHeader(AUTHORIZATION);
		log.debug("Authorization token header value: {}", requestTokenHeader);

		jwtAuthenticationService.authenticate(requestTokenHeader, false);

		chain.doFilter(request, response);
	}
}
