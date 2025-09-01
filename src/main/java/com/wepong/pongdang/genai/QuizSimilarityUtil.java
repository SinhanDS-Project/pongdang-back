package com.wepong.pongdang.genai;

import java.util.*;
import java.util.stream.Collectors;

public class QuizSimilarityUtil {

    // ===== 문자열 전처리 (불필요한 기호 제거 + 소문자 통일) =====
    private static List<String> tokenize(String text) {
        return Arrays.stream(text
                        .toLowerCase()
                        .replaceAll("[^a-z0-9가-힣\\s]", "") // 특수문자 제거
                        .split("\\s+"))
                .filter(token -> !token.isBlank())
                .collect(Collectors.toList());
    }

    // ===== Jaccard 유사도 =====
    public static double jaccardSimilarity(String s1, String s2) {
        Set<String> set1 = new HashSet<>(tokenize(s1));
        Set<String> set2 = new HashSet<>(tokenize(s2));
        System.out.println(set1);
        System.out.println(set2);

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        if (union.isEmpty()) return 0.0;
        return (double) intersection.size() / union.size();
    }

    // ===== Cosine 유사도 =====
    public static double cosineSimilarity(String s1, String s2) {
        List<String> tokens1 = tokenize(s1);
        List<String> tokens2 = tokenize(s2);

        Set<String> allTokens = new HashSet<>();
        allTokens.addAll(tokens1);
        allTokens.addAll(tokens2);

        Map<String, Integer> freq1 = new HashMap<>();
        Map<String, Integer> freq2 = new HashMap<>();

        for (String t : tokens1) freq1.put(t, freq1.getOrDefault(t, 0) + 1);
        for (String t : tokens2) freq2.put(t, freq2.getOrDefault(t, 0) + 1);

        // 벡터 내적 / (각 벡터 크기)
        double dot = 0.0, norm1 = 0.0, norm2 = 0.0;
        for (String t : allTokens) {
            int v1 = freq1.getOrDefault(t, 0);
            int v2 = freq2.getOrDefault(t, 0);
            dot += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }
        if (norm1 == 0 || norm2 == 0) return 0.0;
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    // ===== 중복 여부 판단 =====
    public static boolean isDuplicate(String q1, String q2) {
        double jac = jaccardSimilarity(q1, q2);
        double cos = cosineSimilarity(q1, q2);

        // 기준치: Jaccard >= 0.6 또는 Cosine >= 0.75
        return jac >= 0.4 || cos >= 0.45;
    }

    // ===== 테스트 =====
    public static void main(String[] args) {
        String q1 = "예금자보호법에 따라 예금보험공사가 보호하는 예금의 최고 한도는 얼마인가요?";
        String q2 = "다음 중 예금자보호법에 따라 예금보험공사가 보호하는 예금의 최고 한도는 얼마인가요?";

        System.out.println("Jaccard: " + jaccardSimilarity(q1, q2));
        System.out.println("Cosine: " + cosineSimilarity(q1, q2));
        System.out.println("Duplicate? " + isDuplicate(q1, q2));
    }
}
