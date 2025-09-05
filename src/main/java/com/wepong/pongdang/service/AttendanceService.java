package com.wepong.pongdang.service;

import com.wepong.pongdang.dto.response.AttendanceResponseDTO;
import com.wepong.pongdang.entity.AttendanceEntity;
import com.wepong.pongdang.entity.PongHistoryEntity;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.entity.enums.PongHistoryType;
import com.wepong.pongdang.entity.enums.WalletType;
import com.wepong.pongdang.exception.AlreadyAttendanceException;
import com.wepong.pongdang.repository.AttendanceRepository;
import com.wepong.pongdang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final HistoryService historyService;
    private final WalletService walletService;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // 출석체크시 출석테이블에 표시 및 포인트 지급
    @Transactional
    public String attendanceInsert(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now(KST);

        if(attendanceRepository.existsByUserIdAndAttendanceDate(userId, today)) {
            throw new AlreadyAttendanceException(); // 이미 오늘의 출석이 완료되었습니다.
        }
        AttendanceEntity attendance = AttendanceEntity.builder()
                .user(user)
                .attendanceDate(today)
                .build();
        attendanceRepository.save(attendance);

        PongHistoryEntity history = PongHistoryEntity.builder()
                .type(PongHistoryType.ADD)
                .amount(1)
                .build();

        historyService.insertPointHistory(history, userId);
        walletService.add(1, userId, WalletType.PONG);

        return "출석이 완료되었습니다.";
    }

    // user별 출석일수 조회
    public AttendanceResponseDTO countAttendance(Long userId) {
        int countAttendance = attendanceRepository.countByUserId(userId);
        return AttendanceResponseDTO.builder().count(countAttendance).build();
    }
}
