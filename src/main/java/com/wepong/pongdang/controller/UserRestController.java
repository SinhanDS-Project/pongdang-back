package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.response.ConvertResponseDTO;
import com.wepong.pongdang.dto.response.UserResponseDTO;
import com.wepong.pongdang.dto.request.UserUpdateRequestDTO;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.entity.WalletEntity;
import com.wepong.pongdang.entity.enums.WalletType;
import com.wepong.pongdang.dto.response.UserInfoResponseDTO;
import com.wepong.pongdang.exception.BettingUserNotFoundException;
import com.wepong.pongdang.exception.EmailNotFoundException;
import com.wepong.pongdang.exception.UnauthorizedAccessException;
import com.wepong.pongdang.exception.UserAlreadyLinkedException;
import com.wepong.pongdang.repository.UserRepository;
import com.wepong.pongdang.service.AuthService;
import com.wepong.pongdang.service.BettingUserService;
import com.wepong.pongdang.service.WalletService;
import com.wepong.pongdang.dto.request.ConvertRequestDTO;
import com.wepong.pongdang.dto.response.BettingUserResponseDTO;
import com.wepong.pongdang.service.PointConvertService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private PointConvertService pointConvertService;

	@GetMapping("/me")
	public UserResponseDTO getMyInfo(@RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
            if (authHeader == null || authHeader.isBlank()) {
                throw new UnauthorizedAccessException(); // "로그인 후 이용이 가능한 서비스입니다"
            }
		Long userId = authService.validateAndGetUserId(authHeader);
		UserEntity userEntity = authService.findById(userId); // 또는 getUserByUid(userId)

		String profileFullUrl = (userEntity.getProfileImage() != null && !userEntity.getProfileImage().isBlank())
		                        ? userEntity.getProfileImage()
		                        : "";

		WalletEntity pong = walletService.findByIdAndType(userEntity.getId(), WalletType.PONG);
		WalletEntity dona = walletService.findByIdAndType(userEntity.getId(), WalletType.DONA);

		return UserResponseDTO.from(userEntity, profileFullUrl, pong, dona);
	}

	// 회원정보 수정
	@PutMapping("/update")
	public ResponseEntity<?> updateMyInfo(@RequestPart UserUpdateRequestDTO userRequest,
                                          @RequestPart(required = false) MultipartFile profileImage,
							 			  @RequestHeader("Authorization") String authHeader) {
		Long userId = authService.validateAndGetUserId(authHeader);
		authService.updateUser(userRequest, userId, profileImage);
		return ResponseEntity.ok(Map.of("message", "회원 정보가 수정되었습니다."));
	}

	// 이메일 찾기
	@GetMapping("/findEmail")
	public ResponseEntity<?> findEmail(@RequestParam("user_name") String userName,
		@RequestParam("phone_number") String phoneNumber) {
		String email = authService.getUserEmail(userName, phoneNumber);

		if(email != null) {
			Map<String, String> response = Map.of("email", email);
			return ResponseEntity.ok(response);
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
            throw new UserAlreadyLinkedException();
        }

        // 연동 처리
        user.setLinkedWithBetting(true);
        userRepository.save(user);

        // 업데이트된 유저 DTO 반환
        return ResponseEntity.ok(UserInfoResponseDTO.from(user));
    }

    //  마이페이지 연동, 전환하기 버튼 눌렀을때 데이터 전송
    @GetMapping("/find-betting")
    public ResponseEntity<?> getMyBettingUser(
            @RequestHeader("Authorization") String authHeader) {

        Long pongUserId = authService.validateAndGetUserId(authHeader);
        UserEntity user = authService.findById(pongUserId);

        // Pongdang(010xxxxxxxx) → Betting(010-xxxx-xxxx) 포맷 맞추기
        String rawPhone = user.getPhoneNumber(); // 010xxxxxxxx
        String formattedPhone = rawPhone.replaceAll("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3"); // 010-4738-7321

        BettingUserResponseDTO dto = bettingUserService.findUser(user.getUserName(), formattedPhone);

        if (dto == null) {
            throw new BettingUserNotFoundException();
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/betting/convert")
    public ResponseEntity<?> convertBettingPoint(
            @RequestBody ConvertRequestDTO req,
            @RequestHeader("Authorization") String authHeader) {

        Long pongUserId = authService.validateAndGetUserId(authHeader);
        UserEntity user = authService.findById(pongUserId); // 로그인 사용자 정보

        //  Pongdang(010xxxxxxxx) → Betting(010-xxxx-xxxx) 포맷 맞추기
        String rawPhone = user.getPhoneNumber(); // 010xxxxxxxx
        String formattedPhone = rawPhone.replaceAll("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3"); // 010-4738-7321

        // 이름+전화번호로 betting 유저 찾기
        BettingUserResponseDTO bettingUser = bettingUserService.findUser(user.getUserName(), formattedPhone);
        if (bettingUser == null) {
            throw new BettingUserNotFoundException();
        }

        // uid로 convert 실행
        int converted = pointConvertService.convert(bettingUser.getUid(), req.getAmount(), pongUserId);

        // 전환 후 최신 잔액 내려주기
        BettingUserResponseDTO bettingAfter = bettingUserService.findUser(user.getUserName(), formattedPhone);
        WalletEntity pongWallet = walletService.findByIdAndType(pongUserId, WalletType.PONG);

        ConvertResponseDTO response = ConvertResponseDTO.builder()
                .message("포인트가 전환 되었습니다.")
                .converted(converted)
                .bettingPointAfter(bettingAfter != null ? bettingAfter.getPointBalance() : null)
                .pongBalanceAfter(pongWallet != null ? pongWallet.getPongBalance() : null)
                .build();

        return ResponseEntity.ok(response);
    }

}