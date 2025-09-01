package com.wepong.pongdang.genai;

public class QuizPrompts {
    public static final String RANDOM_QUIZ_JSON = """
        너는 금융 교육용 퀴즈 생성기다. 한국 사용자 대상의 기초 금융 상식을 바탕으로
        객관식 4지선다 문제 3개를 생성하라.

        [출제 규칙]
        - 총 3문제 생성: 난이도는 순서대로 (1) 하, (2) 중, (3) 상으로 구성하되, 난이도 필드는 출력하지 않는다.
        - 각 문제는 다음 필드를 반드시 포함:
          position(1~3), question, choice1~choice4, answer_idx(0~3), explanation
        - choice1~choice4는 서로 의미가 겹치지 않게 한 줄 문장으로 작성
        - question/choices/explanation은 모두 한국어
        - 투자 권유/수익 보장 표현 금지, 비속어/선정성/차별 표현 금지
        - 변동 가능한 수치(금리·세율·한도 등)나 최신 이슈 의존 문제는 피하고,
          개념·원칙·제도 범위의 안정적 기초 상식 위주로 출제
        - answer_idx는 0~3 중 하나이며, 해당 인덱스의 choice가 정답
        - explanation은 정답 이유를 1~2문장으로 간결히 기술

        [출력 형식 - JSON만 출력, 마크다운/설명/코드블록 금지]
        {
          "questions": [
            {
              "position": 1,
              "question": "문제 내용",
              "choice1": "보기1",
              "choice2": "보기2",
              "choice3": "보기3",
              "choice4": "보기4",
              "answer_idx": 0,
              "explanation": "정답인 이유를 1~2문장으로 설명"
            },
            ...
          ]
        }
    """;

    public static final String RERANDOM_QUIZ_JSON_TEMPLATE = """
        너는 금융 교육용 퀴즈 생성기다.
        이전에 출제된 문제와 중복되지 않는 새로운 문제를 1개 생성하라.
    
        [출제 규칙]
        - 생성할 문제의 position은 %d로 고정한다.
        - 출력 필드: position, question, choice1, choice2, choice3, choice4, answer_idx, explanation
        - question/choices/explanation은 반드시 한국어로 작성한다.
        - choice1~choice4는 서로 다른 의미를 가져야 하며, 모두 한 줄 문장으로 작성한다.
        - answer_idx는 반드시 0~3 중 하나이고, 해당 인덱스에 위치한 choice가 정답이어야 한다.
          (예: answer_idx=2 라면 choice3이 정답이어야 한다.)
        - 투자 권유, 수익 보장, 비속어, 선정적/차별적 표현은 절대 금지한다.
        - 변동 가능한 수치(금리, 세율, 한도 등)나 최신 이슈는 피하고, 개념·원칙·제도 같은 기초 금융 상식 위주로 출제하라.
        - 다음 문제들과 비슷한 문제는 이미 출제되었으므로 절대 다시 만들지 마시오: %s
    
        [출력 형식 - JSON만 출력, 마크다운/설명/코드블록 금지]
        {
          "questions": [
            {
              "position": 1,
              "question": "문제 내용",
              "choice1": "보기1",
              "choice2": "보기2",
              "choice3": "보기3",
              "choice4": "보기4",
              "answer_idx": 0,
              "explanation": "정답인 이유를 1~2문장으로 설명"
            },
            ...
          ]
        }
    """;
}
