package dev.studycompanion.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class QuizServiceTest {
    @Test
    void scoresOnlyTheSelectedCorrectOption() {
        assertThat(QuizService.isCorrect(2, 2)).isTrue();
        assertThat(QuizService.isCorrect(1, 2)).isFalse();
        assertThat(QuizService.isCorrect(null, 2)).isFalse();
    }
}
