package com.wepong.pongdang.repository;

import com.wepong.pongdang.entity.AttendanceEntity;
import com.wepong.pongdang.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {
    boolean existsByUserIdAndAttendanceDate(Long userId, LocalDate attendanceDate);
    int countByUserId(Long userId);
}
