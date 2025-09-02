package com.wepong.pongdang.genai;

import com.wepong.pongdang.dto.request.FinanceRequestDTO;

import java.util.Map;

public class FinancePrompt {
    public static String buildPrompt(FinanceRequestDTO request, Map<String, Object> mlResult) {
        return """
            당신은 금융 데이터 분석 전문가입니다.
            아래 사용자의 금융 입력 데이터와 머신러닝 예측 결과를 바탕으로 개인 맞춤 금융 리포트를 작성하세요.
        
            출력 규칙:
            1. 반드시 JSON 형식으로만 출력합니다. (마크다운, HTML, 텍스트 불가)
            2. JSON은 아래 구조를 따라야 합니다:
            {
            "title": "리포트 제목",
            "report": {
                "summary": {
                  "title": "기본 현황 요약",
                  "content": "..."
                },
                "analysis": {
                  "title": "소비/저축/투자 분석",
                  "content": "...",
                  "consumption_analysis": { "title": "...", "content": "..." },
                  "saving_analysis": { "title": "...", "content": "..." },
                  "investment_analysis": { "title": "...", "content": "..." }
                },
                "strategy": {
                  "title": "권장 전략",
                  "content": "...",
                  "debt": { "title": "...", "content": "..." },
                  "saving": { "title": "...", "content": "..." },
                  "invest": { "title": "...", "content": "..." },
                  "products": [
                    { "name": "...", "institution": "...", "recommendation": "..." }
                  ]
                },
                "conclusion": {
                  "title": "종합 평가",
                  "content": "..."
                }
                }
            }

            3. `title`과 `content`는 모든 섹션에 필수 포함.

            ---
 
            [사용자 입력 데이터]
            나이: %d세
            월 소득: %,d원
            월 소비: %,d원
            주요 소비 항목: %s
            저축 목표액: %,d원
            현재 저축액: %,d원
            부채 여부: %s
            투자 성향: %s
            목표 기간: %s

            [분석 결과]
            소비유형: %d (%s)
            소비율: %.1f%%
            저축률: %.1f%%
            목표 달성률: %.1f%%
            소비전략: %s

            ---

            위 데이터를 기반으로, 구체적인 수치(%%)와 금액을 활용해 설명하고, 마지막에는 핵심 권장 전략을 한 줄 요약으로 포함하세요.

        """.formatted(
                request.getAge(),
                request.getIncome(),
                request.getSpend(),
                request.getMain_category(),
                request.getSaving_goal(),
                request.getCurrent_saving(),
                request.getLoan(),
                request.getInvest_type(),
                request.getGoal_term(),
                (int) mlResult.get("cluster"),
                mlResult.get("cluster_label"),
                mlResult.get("spend_rate"),
                mlResult.get("saving_rate"),
                mlResult.get("goal_achieve"),
                mlResult.get("strategy")
        );
    }
}
