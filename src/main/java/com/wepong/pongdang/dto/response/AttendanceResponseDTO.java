package com.wepong.pongdang.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceResponseDTO {
    private int count;
    private List<LocalDate> attendanceDate;
    private boolean bubble;
    private boolean transfer;
}
