package com.wepong.pongdang.service;

import com.wepong.pongdang.dto.response.AttendanceResponseDTO;
import com.wepong.pongdang.entity.AttendanceEntity;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.entity.enums.EventType;
import com.wepong.pongdang.exception.AlreadyAttendanceException;
import com.wepong.pongdang.exception.AlreadyBubbleException;
import com.wepong.pongdang.exception.AlreadyTransferException;
import com.wepong.pongdang.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AuthService authService;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    LocalDate today = LocalDate.now(KST);

    // user별 출석일수 조회
    public AttendanceResponseDTO countAttendance(Long userId) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1); // 이번 달 1일
        LocalDate endOfMonth = currentMonth.atEndOfMonth(); // 이번 달 말일

        // 이번 달 출석 날짜 list
        List<LocalDate> attendanceDates = attendanceRepository
                .findAllByUserIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
                        userId, startOfMonth, endOfMonth
                )
                .stream()
                .filter(AttendanceEntity::isAttended)
                .map(AttendanceEntity::getAttendanceDate)
                .toList();

        // 이번 달 출석일수
        int countAttendance = attendanceDates.size();

        return AttendanceResponseDTO.builder()
                    .count(countAttendance)
                    .attendanceDate(attendanceDates)
                    .build();
    }

    @Transactional
    public void eventCheck(EventType event, Long userId) {
        AttendanceEntity attendance = attendanceRepository.findByUserIdAndAttendanceDate(userId, today);
        UserEntity user = authService.findById(userId);

        if (attendance == null) {
            attendance = AttendanceEntity.builder()
                .user(user)
                .attendanceDate(today)
                .build();
        }

        switch (event) {
            case ATTENDANCE:
                if (attendance.isAttended()) {
                    throw new AlreadyAttendanceException();
                }
                attendance.setAttended(true);
                break;

            case BUBBLE:
                if (attendance.isBubble()) {
                    throw new AlreadyBubbleException();
                }
                attendance.setBubble(true);
                break;

            case TRANSFER:
                if (attendance.isTransfer()) {
                    throw new AlreadyTransferException();
                }
                attendance.setTransfer(true);
                break;
        }

        attendanceRepository.save(attendance);
    }
}
