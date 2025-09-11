package com.wepong.pongdang.filter;

import com.wepong.pongdang.entity.enums.Role;
import com.wepong.pongdang.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

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
				Role role = null;
				try {
					role = jwtUtil.getRoleFromToken(token);
				} catch (Exception e) {
					role = Role.USER; // 기본값
				}
				setAuthentication(userId, role);
			} else if (jwtUtil.isTokenExpired(token)) {
				String refreshToken = getRefreshTokenFromCookie(httpReq);

				if (refreshToken != null && jwtUtil.validateToken(refreshToken)) {
					Long userId = jwtUtil.getUserIdFromToken(refreshToken);
					Role role = jwtUtil.getRoleFromToken(refreshToken);
					String newAccessToken = jwtUtil.generateAccessToken(userId, role);
					httpRes.setHeader("New-Access-Token", "Bearer " + newAccessToken);
					setAuthentication(userId, role);
				}
			}
		}

		chain.doFilter(httpReq, httpRes);
	}

	private void setAuthentication(Long userId, Role role) {
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name());
		UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(userId, null, Collections.singletonList(authority));
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