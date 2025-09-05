package com.wepong.pongdang.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wepong.pongdang.dto.request.FinanceRequestDTO;
import com.wepong.pongdang.dto.response.FinanceResponseDTO;
import com.wepong.pongdang.exception.UnauthorizedAccessException;
import com.wepong.pongdang.service.AuthService;
import com.wepong.pongdang.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceRestController {
    private final FinanceService financeService;
    private final ObjectMapper jacksonObjectMapper;
    private final AuthService authService;

    @PostMapping("/report")
    public ResponseEntity<JsonNode> generateReport(
            @RequestBody FinanceRequestDTO request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new UnauthorizedAccessException(); // "로그인 후 이용이 가능한 서비스입니다"
        }
        Long userId = authService.validateAndGetUserId(authHeader);
        FinanceResponseDTO mlResultDto = financeService.getPrediction(request, userId);
        Map<String, Object> mlResult = jacksonObjectMapper.convertValue(mlResultDto, Map.class);// Python API 호출
        JsonNode report = financeService.generateFinanceReport(request, mlResult);
        return ResponseEntity.ok(report);
    }

}
