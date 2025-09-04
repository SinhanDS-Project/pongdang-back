package com.wepong.pongdang.entity;

import com.wepong.pongdang.dto.response.QuizResponseDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity(name = "quiz")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class QuizEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quiz_date", nullable = false)
    private LocalDate quizDate;               // 오늘의 퀴즈 날짜

    @Column(nullable = false)
    private Integer position;                 // 1,2,3

    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;                  // 문제

    @Column(columnDefinition = "TEXT", nullable = false)
    private String choice1;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String choice2;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String choice3;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String choice4;

    @Column(name = "answer_idx", nullable = false)
    private Integer answerIdx;                // 0~3

    @Column(columnDefinition = "TEXT", nullable = false)
    private String explanation;               // 해설

    public QuizResponseDTO.QuizView toDto() {
        return QuizResponseDTO.QuizView.builder()
                .position(position)
                .question(question)
                .choice1(choice1)
                .choice2(choice2)
                .choice3(choice3)
                .choice4(choice4)
                .answerIdx(answerIdx)
                .explanation(explanation)
                .build();
    }
}
