package com.wepong.pongdang;

import com.wepong.pongdang.entity.QuizEntity;
import com.wepong.pongdang.repository.QuizRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@SpringBootTest
public class GenAITest {

    @Autowired
    private QuizRepository quizRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Test
    public void f1(){
        LocalDate today = LocalDate.now(KST);
//        List<String> bannedQuestions = quizRepository.findByQuizDateBefore(today)
        List<String> bannedQuestions = quizRepository.findAll()
                .stream()
                .map(QuizEntity::getQuestion)
                .toList();
        System.out.println(bannedQuestions);
    }

}
