package com.wepong.pongdang.util;

import com.wepong.pongdang.entity.enums.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JWTUtil {
	@Value("${spring.security.jwt.secret}")
	private String secretKey;

	@Value("${spring.security.jwt.access-token-expiration}")
	private long accessTokenExpiration;

	@Value("${spring.security.jwt.refresh-token-expiration}")
	private long refreshTokenExpiration;

	private Key key;

	@PostConstruct
	public void init() {
		this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
	}

	public String generateAccessToken(Long userId, Role role) {
		return buildToken(userId, role, accessTokenExpiration);
	}

	public String generateRefreshToken(Long userId, Role role) {
		return buildToken(userId, role, refreshTokenExpiration);
	}

	private String buildToken(Long userId, Role role, long expiration) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + expiration);

		return Jwts.builder()
				.setSubject(String.valueOf(userId))
				.claim("role", role.name())
				.setIssuedAt(now)
				.setExpiration(expiryDate)
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}

	// ✅ 토큰 유효성 검사 (만료 포함)
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (JwtException e) {
			return false;
		}
	}

	//✅ 토큰이 만료되었는지 확인
	public boolean isTokenExpired(String token) {
		try {
			Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

			return claims.getExpiration().before(new Date());
		} catch (ExpiredJwtException e) {
			return true;
		} catch (JwtException e) {
			return false; // 잘못된 토큰이면 만료 확인 불가로 false
		}
	}

	//✅ userId (sub) 가져오기
	public Long getUserIdFromToken(String token) {
		return Long.parseLong(
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject());
	}

	// 토큰에서 role 꺼내는 메서드 추가
	public Role getRoleFromToken(String token) {
		Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
		String role = claims.get("role", String.class);
		return Role.valueOf(role);
	}
}
