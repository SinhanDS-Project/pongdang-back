package com.wepong.pongdang.controller;

import com.wepong.pongdang.exception.UserNotFoundException;
import com.wepong.pongdang.service.AuthService;
import com.wepong.pongdang.service.VerificationService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class VerificationRestController {
	@Autowired
	private AuthService authService;

	@Autowired
	private VerificationService verificationService;

	@PostMapping(value = "/request")
	public ResponseEntity<?> requestVerification(@RequestBody Map<String, String> request) throws MessagingException {
		String email = request.get("email");
		
		if(!authService.isEmailExists(email)) {
			verificationService.requestVerification(email);
			return ResponseEntity.ok(Map.of("message", "인증번호가 이메일로 발송되었습니다."));
		}

		return ResponseEntity.ok(Map.of("message", "이미 가입된 이메일입니다."));
	}

	@PostMapping(value = "/find/request")
	public ResponseEntity<?> findVerification(@RequestBody Map<String, String> request) throws MessagingException {
		String email = request.get("email");

		if(!authService.isEmailExists(email)) {
			throw new UserNotFoundException();
		} else {
			verificationService.requestVerification(email);
			return ResponseEntity.ok(Map.of("message", "인증번호가 이메일로 발송되었습니다."));
		}
	}

	@PostMapping(value = "/verify")
	public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
		String email = request.get("email");
		String code = request.get("code");
		verificationService.verifyCode(email, code);
		return ResponseEntity.ok(Map.of("message", "이메일 인증이 완료되었습니다."));
	}

	@PostMapping(value = "/password")
	public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> request) throws MessagingException {
		String email = request.get("email");
		Long userId = authService.findByEmail(email).getId();
		verificationService.updatePassword(email, userId);
		return ResponseEntity.ok(Map.of("message", "임시 비밀번호가 이메일로 발송되었습니다."));
	}
}