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
import com.wepong.pongdang.exception.FinanceReportException;
import com.wepong.pongdang.exception.UserCannotFoundException;
import com.wepong.pongdang.genai.FinancePrompt;
import com.wepong.pongdang.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Value("${gemini.model}")
    private String model;

    @Value("${ml.api.url}")
    private String ML_API_URL;

    // 타임아웃 설정된 RestTemplate 사용
    private final RestTemplate restTemplate = buildRestTemplate();
    private static RestTemplate buildRestTemplate() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }

    public FinanceResponseDTO getPrediction(FinanceRequestDTO request, Long userId) {
        String name = userRepository.findById(userId)
                .map(u -> u.getUserName())
                .orElseThrow(() -> new UserCannotFoundException());
        request.setName(name);

        try {
            ResponseEntity<FinanceResponseDTO> response =
                    restTemplate.postForEntity(ML_API_URL, request, FinanceResponseDTO.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new FinanceReportException();
            }
            return response.getBody();

        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            // ML 서버가 4xx/5xx를 반환한 경우
            throw new FinanceReportException();
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // 연결 거부, 타임아웃, 네임해결 실패 등 I/O 계열
            throw new FinanceReportException();
        } catch (Exception e) {
            // 기타 예외
            throw new FinanceReportException();
        }
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
            if (json == null || json.isBlank()) {
                throw new FinanceReportException();
            }

            // 3) JSON 파싱 (검증은 최소한만)
            return objectMapper.readTree(json);

        } catch (FinanceReportException e) {
            throw e;
        } catch (Exception e) {
            throw new FinanceReportException();
        }
    }

}
