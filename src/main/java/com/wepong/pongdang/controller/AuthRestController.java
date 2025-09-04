package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.request.LoginRequestDTO;
import com.wepong.pongdang.dto.request.TransferRequestDTO;
import com.wepong.pongdang.dto.request.UserRegisterDTO;
import com.wepong.pongdang.dto.response.BettingUserResponseDTO;
import com.wepong.pongdang.exception.AuthException;
import com.wepong.pongdang.service.AuthService;
import com.wepong.pongdang.service.BettingUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

	@Autowired
	private AuthService authService;

    @Autowired
    private BettingUserService bettingUserService;

	// 로그인 API
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
		try {
			Map<String, String> responseToken = authService.login(loginRequest);
			String accessToken = responseToken.get("accessToken");
			String refreshToken = responseToken.get("refreshToken");

			// HttpOnly 쿠키 생성
			ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken).httpOnly(true).secure(false) // HTTPS 사용하는 경우 true
					.path("/") // 모든 경로에 대해 쿠키 적용
					.maxAge(14 * 24 * 60 * 60) // 14일 (초 단위)
					.sameSite("Strict") // 또는 "Lax" / "None" (크로스 도메인 필요 시)
					.build();

			return ResponseEntity.ok().header("Set-Cookie", cookie.toString()).body(new LoginResponse(accessToken, "로그인 성공"));
		} catch (AuthException error) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).contentType(MediaType.valueOf("text/plain;charset=UTF-8")).body(error.getMessage());
		}
	}

	@GetMapping("/check-nickname")
	public ResponseEntity<?> checkNicknameDuplicate(@RequestParam("nickname") String nickname) {
		boolean isDuplicate = authService.isNicknameExists(nickname);

		return ResponseEntity.ok(Map.of("duplicate", isDuplicate));
	}

	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@RequestBody UserRegisterDTO dto) {
		// 필수 항목 공백 검사
		if (isBlank(dto.getEmail())
				|| isBlank(dto.getPassword())
				|| isBlank(dto.getUserName())
				|| isBlank(dto.getNickname())
				|| isBlank(dto.getBirthDate())
				|| isBlank(dto.getPhoneNumber())) {
			return ResponseEntity.badRequest().contentType(MediaType.valueOf("text/plain;charset=UTF-8")).body("모든 필수 항목을 입력해주세요.");
		}

		// 이메일 중복 검사
		if (authService.isEmailExists(dto.getEmail())) {
			return ResponseEntity.badRequest().contentType(MediaType.valueOf("text/plain;charset=UTF-8")).body("이미 사용 중인 이메일입니다.");
		}

		// 닉네임 중복 검사
		if (authService.isNicknameExists(dto.getNickname())) {
			return ResponseEntity.badRequest().contentType(MediaType.valueOf("text/plain;charset=UTF-8")).body("이미 사용 중인 닉네임입니다.");
		}

		// 비밀번호 일치 검사
		if (!dto.getPassword().equals(dto.getPasswordCheck())) {
			return ResponseEntity.badRequest().contentType(MediaType.valueOf("text/plain;charset=UTF-8")).body("비밀번호가 일치하지 않습니다.");
		}

		// 비밀번호 정규식 검사 (6~8자, 대문자, 소문자, 숫자, 특수문자)
		String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{6,}$";
		
		if (!Pattern.matches(passwordPattern, dto.getPassword())) {
			return ResponseEntity.badRequest().contentType(MediaType.valueOf("text/plain;charset=UTF-8")).body("비밀번호는 6~8자의 대소문자, 숫자, 특수문자를 포함해야 합니다.");
		}

		// 전화번호 형식 검사
		String phonePattern = "^010-\\d{4}-\\d{4}$";
		
		if (!Pattern.matches(phonePattern, dto.getPhoneNumber())) {
			return ResponseEntity.badRequest().contentType(MediaType.valueOf("text/plain;charset=UTF-8")).body("전화번호 형식이 올바르지 않습니다. (예: 010-0000-0000)");
		}


		// 생년월일로 만 19세 이상인지 검사
		try {
		// 생년월일 문자열을 Date로 파싱
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    Date birthDate = sdf.parse(dto.getBirthDate());

	    // 현재 날짜와 비교
	    Calendar birthCal = Calendar.getInstance();
	    birthCal.setTime(birthDate);

	    Calendar today = Calendar.getInstance();

	    int age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);

	    // 생일이 아직 지나지 않았으면 한 살 빼기
	    if (today.get(Calendar.MONTH) < birthCal.get(Calendar.MONTH) ||
	        (today.get(Calendar.MONTH) == birthCal.get(Calendar.MONTH) && today.get(Calendar.DAY_OF_MONTH) < birthCal.get(Calendar.DAY_OF_MONTH))) {
	        age--;
	    }

	    if (age < 15) {
	        return ResponseEntity.badRequest().contentType(MediaType.valueOf("text/plain;charset=UTF-8")).body("만 19세 이상만 가입할 수 있습니다.");
	    }
		} catch (Exception e) {
			return ResponseEntity.badRequest().contentType(MediaType.valueOf("text/plain;charset=UTF-8")).body("유효한 생년월일을 입력해주세요.");
		}

		// 개인정보 수집 동의
		if (!dto.isAgreePrivacy()) {
			return ResponseEntity.badRequest().contentType(MediaType.valueOf("text/plain;charset=UTF-8")).body("개인정보 수집 및 이용에 동의해야 가입할 수 있습니다.");
		}

		authService.register(dto);

		return ResponseEntity.ok("회원가입이 완료되었습니다.");
	}


    @PostMapping("/find-betting-user")
    public ResponseEntity<?> findBettingUser(@RequestBody Map<String, String> req) {
        String name = req.get("name");
        String phone = req.get("phone");

        BettingUserResponseDTO dto = bettingUserService.findUser(name, phone);

        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("해당 회원을 찾을 수 없습니다.");
        }
        return ResponseEntity.ok(dto);
    }
	// 리프레시 토큰을 통한 액세스 토큰 재발급 API
	@PostMapping("/reissue")
	public ResponseEntity<?> reissue(@RequestHeader("Authorization") String authHeader) {
		try {
			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				ResponseCookie cookie = ResponseCookie.from("refreshToken", "").path("/").httpOnly(true).maxAge(0) // 삭제
						.build();

				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("Set-Cookie", cookie.toString()).contentType(MediaType.valueOf("text/plain;charset=UTF-8")).body("토큰이 없습니다.");
			}

			String refreshToken = authHeader.substring(7);
			String accessToken = authService.reissue(refreshToken);

			return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.valueOf("text/plain;charset=UTF-8")).body(new LoginResponse(accessToken, "토큰 재발급"));
		} catch (AuthException error) {
			return ResponseEntity.status(401).contentType(MediaType.valueOf("text/plain;charset=UTF-8")).body(error.getMessage());
		}
	}

	// 응답 DTO
	static class LoginResponse {
		private String accessToken;
		private String message;

		public LoginResponse(String accessToken, String message) {
			this.accessToken = accessToken;
			this.message = message;
		}

		public String getAccessToken() {
			return accessToken;
		}

		public String getMessage() {
			return message;
		}
	}

	private boolean isBlank(String str) {
		return str == null || str.trim().isEmpty();
	}

	@DeleteMapping("/logout")
	public void logout(HttpServletResponse response, @RequestHeader("Authorization") String authHeader) {
		Long userId = authService.validateAndGetUserId(authHeader);
		authService.logout(userId);

		ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
				.path("/")
				.maxAge(0)
				.httpOnly(true)
				.secure(false)
				.build();

		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

    // 튜토리얼 완료
    @PutMapping("/tutorial/complete")
    public ResponseEntity<?> completeTutorial(@RequestHeader("Authorization") String authHeader) {
        Long userId = authService.validateAndGetUserId(authHeader);
        authService.completeTutorial(userId);

        return ResponseEntity.ok("튜토리얼 완료 처리되었습니다.");
    }

    // 회원탈퇴
    @DeleteMapping("/unregister")
    public ResponseEntity<?> unregister(@RequestHeader("Authorization") String authHeader) {
        Long userId = authService.validateAndGetUserId(authHeader); // 토큰에서 userId 추출
        authService.unregister(userId);

        // refreshToken 쿠키 제거
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(false)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("회원 탈퇴가 완료되었습니다.");
    }


}
