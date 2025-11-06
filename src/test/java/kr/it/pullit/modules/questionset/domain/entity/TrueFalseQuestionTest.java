package kr.it.pullit.modules.questionset.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionResponse;
import kr.it.pullit.modules.questionset.domain.dto.QuestionUpdateParam;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import kr.it.pullit.modules.questionset.exception.InvalidQuestionException;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import kr.it.pullit.support.fixture.QuestionSetFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@MockitoUnitTest
@DisplayName("TrueFalseQuestion 단위 테스트")
class TrueFalseQuestionTest {

  @Nested
  @DisplayName("createFromLlm")
  class DescribeCreateFromLlm {

    @Test
    @DisplayName("LLM 응답으로부터 TrueFalseQuestion을 생성한다")
    void createsTrueFalseQuestionFromLlmResponse() {
      // given
      var questionSet = QuestionSetFixtures.basic();
      var llmResponse = new LlmGeneratedQuestionResponse(1, "정답은 참이다", null, "true", "해설입니다");

      // when
      var question = TrueFalseQuestion.createFromLlm(questionSet, llmResponse);

      // then
      assertThat(question.getQuestionText()).isEqualTo("정답은 참이다");
      assertThat(question.isAnswer()).isTrue();
      assertThat(question.getExplanation()).isEqualTo("해설입니다");
      assertThat(question.getQuestionSet()).isEqualTo(questionSet);
    }

    @Test
    @DisplayName("options가 있으면 예외를 던진다")
    void throwsExceptionWhenOptionsExist() {
      // given
      var questionSet = QuestionSetFixtures.basic();
      var llmResponse =
          new LlmGeneratedQuestionResponse(1, "문제", java.util.List.of("옵션1"), "true", "해설");

      // when & then
      assertThatThrownBy(() -> TrueFalseQuestion.createFromLlm(questionSet, llmResponse))
          .isInstanceOf(InvalidQuestionException.class);
    }

    @Test
    @DisplayName("answer가 true/false가 아니면 예외를 던진다")
    void throwsExceptionWhenAnswerIsInvalid() {
      // given
      var questionSet = QuestionSetFixtures.basic();
      var llmResponse = new LlmGeneratedQuestionResponse(1, "문제", null, "yes", "해설");

      // when & then
      assertThatThrownBy(() -> TrueFalseQuestion.createFromLlm(questionSet, llmResponse))
          .isInstanceOf(InvalidQuestionException.class);
    }
  }

  @Nested
  @DisplayName("isCorrect")
  class DescribeIsCorrect {

    @Test
    @DisplayName("정답이 true이고 사용자 답이 true이면 올바른 것으로 판단한다")
    void returnsTrueWhenAnswerMatches() {
      // given
      var question = TrueFalseQuestion.builder().answer(true).questionText("문제").build();

      // when
      boolean result = question.isCorrect("true");

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("정답이 false이고 사용자 답이 false이면 올바른 것으로 판단한다")
    void returnsTrueWhenAnswerIsFalse() {
      // given
      var question = TrueFalseQuestion.builder().answer(false).questionText("문제").build();

      // when
      boolean result = question.isCorrect("false");

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("정답과 사용자 답이 다르면 틀린 것으로 판단한다")
    void returnsFalseWhenAnswerDoesNotMatch() {
      // given
      var question = TrueFalseQuestion.builder().answer(true).questionText("문제").build();

      // when
      boolean result = question.isCorrect("false");

      // then
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("사용자 답이 null이면 틀린 것으로 판단한다")
    void returnsFalseWhenUserAnswerIsNull() {
      // given
      var question = TrueFalseQuestion.builder().answer(true).questionText("문제").build();

      // when
      boolean result = question.isCorrect(null);

      // then
      assertThat(result).isFalse();
    }
  }

  @Nested
  @DisplayName("update")
  class DescribeUpdate {

    @Test
    @DisplayName("문제를 업데이트한다")
    void updatesQuestion() {
      // given
      var question = TrueFalseQuestion.builder().answer(true).questionText("기존 문제").build();
      var param = new QuestionUpdateParam("새 문제", "새 해설", null, "false");

      // when
      question.update(param);

      // then
      assertThat(question.getQuestionText()).isEqualTo("새 문제");
      assertThat(question.getExplanation()).isEqualTo("새 해설");
      assertThat(question.isAnswer()).isFalse();
    }
  }

  @Nested
  @DisplayName("getQuestionType")
  class DescribeGetQuestionType {

    @Test
    @DisplayName("TRUE_FALSE 타입을 반환한다")
    void returnsTrueFalseType() {
      // given
      var question = TrueFalseQuestion.builder().answer(true).build();

      // when
      QuestionType type = question.getQuestionType();

      // then
      assertThat(type).isEqualTo(QuestionType.TRUE_FALSE);
    }
  }
}
