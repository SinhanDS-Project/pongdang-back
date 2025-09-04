package com.wepong.pongdang.service;

import com.wepong.pongdang.dto.request.TransferRequestDTO;
import com.wepong.pongdang.dto.request.UserRegisterDTO;
import com.wepong.pongdang.dto.response.BettingUserResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.wepong.mysql.entity.UserEntity;
import net.wepong.mysql.repository.SUserRepository;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BettingUserService {


    private final AuthService authService;
    private final SUserRepository sUserRepository;

    public BettingUserResponseDTO findUser(String name, String phone) {
        Optional<UserEntity> userOpt = sUserRepository.findByUserNameAndPhoneNumber(name, phone);

        return userOpt.map(user -> BettingUserResponseDTO.builder()
                .uid(user.getUid())
                .userName(user.getUserName())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .birthDate(user.getBirthDate())
                .agreePrivacy(user.isAgreePrivacy())
                .profileImg(user.getProfileImg())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .role(user.getRole())
                .pointBalance(user.getPointBalance())
                .build()
        ).orElse(null);
    }


}
