package com.wepong.pongdang.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.wepong.pongdang.dto.request.FinanceRequestDTO;
import com.wepong.pongdang.dto.response.FinanceResponseDTO;
import com.wepong.pongdang.genai.FinancePrompt;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final Client client;                       // GeminiConfig에서 만든 @Bean
    private final GenerateContentConfig defaultGenConfig;
    private final ObjectMapper objectMapper;

    @Value("${gemini.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String ML_API_URL = "http://localhost:8000/predict"; // FastAPI 서버 주소

    public FinanceResponseDTO getPrediction(FinanceRequestDTO request) {
        ResponseEntity<FinanceResponseDTO> response =
                restTemplate.postForEntity(ML_API_URL, request, FinanceResponseDTO.class);

        return response.getBody();
    }

    public JsonNode generateFinanceReport(FinanceRequestDTO request, Map<String, Object> mlResult) {
        try {
            // 1) 프롬프트 구성
            String prompt = FinancePrompt.buildPrompt(request, mlResult);

            Content user = Content.builder()
                    .role("user")
                    .parts(List.of(Part.fromText(prompt)))
                    .build();

            // 2) 생성형 AI 호출
            GenerateContentResponse res = client.models.generateContent(
                    model,
                    List.of(user),
                    defaultGenConfig
            );

            String json = res.text(); // 모델이 생성한 JSON

            // 3) JSON 파싱 (검증은 최소한만)
            return objectMapper.readTree(json);

        } catch (Exception e) {
            throw new IllegalStateException("금융 리포트 생성 실패", e);
        }
    }

}
