package com.wepong.pongdang.config;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class GeminiConfig {

    @Value("${gemini.apiKey}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    @Bean
    public Client genaiClient() {
        // AI Studio 키 그대로 사용
        return Client.builder().apiKey(apiKey).build(); // GenAI Java SDK의 Client. :contentReference[oaicite:1]{index=1}
    }

    @Bean
    public GenerateContentConfig defaultGenConfig(
            @Value("${gemini.temperature}") float temperature,
            @Value("${gemini.responseMimeType}") String responseMimeType
    ) {
        return GenerateContentConfig.builder()
                .temperature(temperature)
                .responseMimeType(responseMimeType) // JSON 강제 (Structured Output 가이드) :contentReference[oaicite:2]{index=2}
                .build();
    }

}
