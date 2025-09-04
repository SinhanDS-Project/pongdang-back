package com.wepong.pongdang.filter;

import com.wepong.pongdang.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JWTUtil jwtUtil;

	public JwtAuthenticationFilter(JWTUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest httpReq, HttpServletResponse httpRes, FilterChain chain)
			throws ServletException, IOException {

		String uri = httpReq.getRequestURI();

		if (isExcludedPath(uri)) {
			chain.doFilter(httpReq, httpRes);
			return;
		}

		String authHeader = httpReq.getHeader("Authorization");
		String token = null;

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			token = authHeader.substring(7);
		}

		if (token != null) {
			if (jwtUtil.validateToken(token)) {
				Long userId = jwtUtil.getUserIdFromToken(token);
				setAuthentication(userId);
			} else if (jwtUtil.isTokenExpired(token)) {
				String refreshToken = getRefreshTokenFromCookie(httpReq);

				if (refreshToken != null && jwtUtil.validateToken(refreshToken)) {
					Long userId = jwtUtil.getUserIdFromToken(refreshToken);
					String newAccessToken = jwtUtil.generateAccessToken(userId);
					httpRes.setHeader("New-Access-Token", "Bearer " + newAccessToken);
					setAuthentication(userId);
				}
			}
		}

		chain.doFilter(httpReq, httpRes);
	}

	private void setAuthentication(Long userId) {
		UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(userId, null, null);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private String getRefreshTokenFromCookie(HttpServletRequest request) {
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if ("refreshToken".equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	private boolean isExcludedPath(String uri) {
		return uri.equals("/") || uri.startsWith("/auth") || uri.startsWith("/resources") || uri.startsWith("/ws");
	}
}