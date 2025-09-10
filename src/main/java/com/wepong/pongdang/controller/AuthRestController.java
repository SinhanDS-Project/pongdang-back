package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.request.LoginRequestDTO;
import com.wepong.pongdang.dto.request.UserRegisterDTO;
import com.wepong.pongdang.dto.response.BettingUserResponseDTO;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.exception.*;
import com.wepong.pongdang.service.AuthService;
import com.wepong.pongdang.service.BettingUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;
import com.wepong.pongdang.dto.response.UserInfoResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import com.wepong.pongdang.dto.response.LoginResponseDTO;

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

            UserEntity user = authService.findByEmail(loginRequest.getEmail());

			// HttpOnly 쿠키 생성
			ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken).httpOnly(true).secure(false) // HTTPS 사용하는 경우 true
					.path("/") // 모든 경로에 대해 쿠키 적용
					.maxAge(14 * 24 * 60 * 60) // 14일 (초 단위)
					.sameSite("Strict") // 또는 "Lax" / "None" (크로스 도메인 필요 시)
					.build();

			return ResponseEntity.ok().header("Set-Cookie", cookie.toString())
					.body(new LoginResponseDTO(accessToken, "로그인 성공", UserInfoResponseDTO.from(user)));
		} catch (AuthException error) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.getMessage());
		}
	}

	@GetMapping("/check-nickname")
	public ResponseEntity<?> checkNicknameDuplicate(@RequestParam("nickname") String nickname) {
		boolean isDuplicate = authService.isNicknameExists(nickname);

		return ResponseEntity.ok(Map.of("duplicate", isDuplicate));
	}

	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@RequestBody UserRegisterDTO dto) throws ParseException {
		// 필수 항목 공백 검사
		if (isBlank(dto.getEmail())
				|| isBlank(dto.getPassword())
				|| isBlank(dto.getUserName())
				|| isBlank(dto.getNickname())
				|| isBlank(dto.getBirthDate())
				|| isBlank(dto.getPhoneNumber())) {
			throw new MissingRequiredFieldsException();
		}

		// 이메일 중복 검사
		if (authService.isEmailExists(dto.getEmail())) {
			throw new EmailAlreadyExistsException();
		}

		// 닉네임 중복 검사
		if (authService.isNicknameExists(dto.getNickname())) {
			throw new NicknameAlreadyExist();
		}

		// 비밀번호 일치 검사
		if (!dto.getPassword().equals(dto.getPasswordCheck())) {
			throw new PasswordMismatchException();
		}

		// 비밀번호 정규식 검사 (6~8자, 대문자, 소문자, 숫자, 특수문자)
		String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{6,}$";
		
		if (!Pattern.matches(passwordPattern, dto.getPassword())) {
			throw new InvalidPasswordFormatException();
		}

		// 전화번호 형식 검사
		String phonePattern = "^010-\\d{4}-\\d{4}$";
		
		if (!Pattern.matches(phonePattern, dto.getPhoneNumber())) {
			throw new InvalidPhoneNumberFormatException();
		}

		// 생년월일로 만 19세 이상인지 검사
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
	        throw new UnderAgeException();
	    }

		// 개인정보 수집 동의
		if (!dto.isAgreePrivacy()) {
			throw new PrivacyAgreementRequiredException();
		}

		authService.register(dto);

		return ResponseEntity.ok(Map.of("message", "회원가입이 완료되었습니다."));
	}


    @PostMapping("/find-betting-user")
    public ResponseEntity<?> findBettingUser(@RequestBody Map<String, String> req) {
        String name = req.get("name");
        String phone = req.get("phone");

        BettingUserResponseDTO dto = bettingUserService.findUser(name, phone);

        if (dto == null) {
            throw new BettingUserNotFoundException();
        }

        return ResponseEntity.ok(dto);
    }

    // HttpOnly 쿠키에 저장된 Refresh Token을 이용해 Access Token 재발급 API
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        // 1. 쿠키에서 refreshToken 꺼내기
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            throw new RefreshTokenNotFoundException();
        }

        try {
            // 2. refreshToken 검증 후 새 accessToken 발급
            String newAccessToken = authService.reissue(refreshToken);
            Long userId = authService.validateAndGetUserId("Bearer " + refreshToken);
            UserEntity user = authService.findById(userId);

            // 3. 새 accessToken 반환
            return ResponseEntity.ok(Map.of(
                    "access_token", newAccessToken,
                    "message", "토큰 재발급 성공",
                    "user", UserInfoResponseDTO.from(user)
            ));
        } catch (AuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
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

		return ResponseEntity.ok(Map.of("message", "튜토리얼이 완료되었습니다."));
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
                .body(Map.of("message", "회원 탈퇴가 완료되었습니다."));
    }
}
