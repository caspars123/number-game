package com.example.game.rest.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationService {

	public static final String BEARER_PREFIX = "Bearer";

	private final JwtTokenUtil jwtTokenUtil;
	private final JwtUserDetailsService jwtUserDetailsService;

	public void authenticate(String requestTokenHeader, boolean isWebsocketRequest) {
		String username = null;
		String jwtToken = null;

		if (requestTokenHeader != null && requestTokenHeader.startsWith(BEARER_PREFIX + " ")) {
			jwtToken = requestTokenHeader.substring(7);
			try {
				username = jwtTokenUtil.getUsernameFromToken(jwtToken);
			} catch (IllegalArgumentException e) {
				log.warn("Unable to get JWT Token");
			} catch (ExpiredJwtException e) {
				log.warn("JWT Token has expired");
			}
		} else {
			log.debug("JWT Token does not begin with Bearer String");
		}

		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);

			if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
				UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
					new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());

				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			}
		}

		if (SecurityContextHolder.getContext().getAuthentication() == null && isWebsocketRequest) {
			throw new AccessDeniedException("Authentication required");
		}
	}
}