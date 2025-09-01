package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.response.UserResponseDTO;
import com.wepong.pongdang.dto.request.UserUpdateRequestDTO;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.entity.enums.WalletType;
import com.wepong.pongdang.exception.EmailNotFoundException;
import com.wepong.pongdang.service.AuthService;
import com.wepong.pongdang.service.WalletService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserRestController {

	@Autowired
	private AuthService authService;
	@Autowired
	private WalletService walletService;

	@GetMapping("/me")
	public UserResponseDTO getMyInfo(@RequestHeader("Authorization") String authHeader) {
		Long userId = authService.validateAndGetUserId(authHeader);
		UserEntity userEntity = authService.findById(userId); // 또는 getUserByUid(userId)

		String baseUrl = "https://bettopia-s3-bucket.s3.ap-northeast-2.amazonaws.com/";
		String profileFullUrl = (userEntity.getProfileImage() != null && !userEntity.getProfileImage().isBlank())
		                        ? baseUrl + userEntity.getProfileImage()
		                        : "";
		
		return UserResponseDTO.from(userEntity, profileFullUrl);
	}

	// 회원정보 수정
	@PostMapping("/update")
	public ResponseEntity<?> updateMyInfo(@ModelAttribute UserUpdateRequestDTO userRequest,
							 			  @RequestHeader("Authorization") String authHeader) {
		Long userId = authService.validateAndGetUserId(authHeader);
		authService.updateUser(userRequest, userId);
	    return ResponseEntity.ok().build();
	}

	// 포인트 충전
	@PutMapping("/get")
	public ResponseEntity<?> addPoint(@RequestHeader("Authorization") String authHeader,
							@RequestBody Map<String, Integer> request) {
		int point = request.get("point");
		Long userId = authService.validateAndGetUserId(authHeader);
		walletService.add(point, userId, WalletType.PONG);

		return ResponseEntity.ok("퐁이 충전되었습니다.");
	}

	// 포인트 차감
	@PutMapping("/lose")
	public ResponseEntity<?> losePoint(@RequestHeader("Authorization") String authHeader,
								@RequestBody Map<String, Integer> request) {
		int point = request.get("point");
		Long userId = authService.validateAndGetUserId(authHeader);
		walletService.lose(point, userId, WalletType.PONG);

		return ResponseEntity.ok("퐁이 차감되었습니다.");
	}

	// 이메일 찾기
	@GetMapping("/findEmail")
	public String findEmail(@RequestParam("user_name") String userName,
		@RequestParam("phone_number") String phoneNumber) {
		String email = authService.getUserEmail(userName, phoneNumber);

		if(email != null) {
			return email;
		} else {
			throw new EmailNotFoundException();
		}
	}
}