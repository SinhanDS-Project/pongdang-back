package com.wepong.pongdang.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wepong.pongdang.dto.request.FinanceRequestDTO;
import com.wepong.pongdang.dto.response.FinanceResponseDTO;
import com.wepong.pongdang.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceRestController {
    private final FinanceService financeService;
    private final ObjectMapper jacksonObjectMapper;

    @PostMapping("/report")
    public ResponseEntity<JsonNode> generateReport(@RequestBody FinanceRequestDTO request) {
        FinanceResponseDTO mlResultDto = financeService.getPrediction(request);
        Map<String, Object> mlResult = jacksonObjectMapper.convertValue(mlResultDto, Map.class);// Python API 호출
        JsonNode report = financeService.generateFinanceReport(request, mlResult);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/predict")
    public FinanceResponseDTO predict(@RequestBody FinanceRequestDTO request) {
        return financeService.getPrediction(request);
    }

}
