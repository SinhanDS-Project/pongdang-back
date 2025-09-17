package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.response.AttendanceResponseDTO;
import com.wepong.pongdang.exception.UnauthorizedAccessException;
import com.wepong.pongdang.service.AttendanceService;
import com.wepong.pongdang.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attendance")
public class AttendanceRestController {

    private final AttendanceService attendanceService;
    private final AuthService authService;

    @GetMapping("")
    public ResponseEntity<AttendanceResponseDTO> countAttendance(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new UnauthorizedAccessException(); // "로그인 후 이용이 가능한 서비스입니다"
        }
        Long userId = authService.validateAndGetUserId(authHeader);
        AttendanceResponseDTO dto = attendanceService.countAttendance(userId);
        return ResponseEntity.ok(dto);
    }

}
