package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.response.BettingUserResponseDTO;
import com.wepong.pongdang.dto.response.UserResponseDTO;
import com.wepong.pongdang.dto.request.UserUpdateRequestDTO;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.entity.WalletEntity;
import com.wepong.pongdang.entity.enums.WalletType;
import com.wepong.pongdang.exception.EmailNotFoundException;
import com.wepong.pongdang.repository.UserRepository;
import com.wepong.pongdang.service.AuthService;
import com.wepong.pongdang.service.BettingUserService;
import com.wepong.pongdang.service.WalletService;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BettingUserService bettingUserService;

	@GetMapping("/me")
	public UserResponseDTO getMyInfo(@RequestHeader("Authorization") String authHeader) {
		Long userId = authService.validateAndGetUserId(authHeader);
		UserEntity userEntity = authService.findById(userId); // 또는 getUserByUid(userId)

		String baseUrl = "https://bettopia-s3-bucket.s3.ap-northeast-2.amazonaws.com/";
		String profileFullUrl = (userEntity.getProfileImage() != null && !userEntity.getProfileImage().isBlank())
		                        ? baseUrl + userEntity.getProfileImage()
		                        : "";

		WalletEntity pong = walletService.findByIdAndType(userEntity.getId(), WalletType.PONG);
		WalletEntity dona = walletService.findByIdAndType(userEntity.getId(), WalletType.DONA);

		return UserResponseDTO.from(userEntity, profileFullUrl, pong, dona);
	}

	// 회원정보 수정
	@PutMapping("/update")
	public ResponseEntity<?> updateMyInfo(@RequestBody UserUpdateRequestDTO userRequest,
							 			  @RequestHeader("Authorization") String authHeader) {
		Long userId = authService.validateAndGetUserId(authHeader);
		authService.updateUser(userRequest, userId);
	    return ResponseEntity.ok("회원 정보 수정이 완료되었습니다.");
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

    //마이페이지에서 연동하기
    @PutMapping("/link-betting")
    public ResponseEntity<?> linkBetting(@RequestHeader("Authorization") String authHeader) {
        Long userId = authService.validateAndGetUserId(authHeader);
        UserEntity user = authService.findById(userId);

        if (Boolean.TRUE.equals(user.getLinkedWithBetting())) {
            return ResponseEntity.badRequest().body("이미 연동된 사용자입니다.");
        }

        // 연동 처리
        user.setLinkedWithBetting(true);
        authService.saveUser(user); // UserEntity 저장

        // 업데이트된 유저 DTO 반환
        return ResponseEntity.ok(AuthRestController.UserInfo.from(user));
    }



}