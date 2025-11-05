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
@DisplayName("ShortAnswerQuestion 단위 테스트")
class ShortAnswerQuestionTest {

  @Nested
  @DisplayName("createFromLlm")
  class DescribeCreateFromLlm {

    @Test
    @DisplayName("LLM 응답으로부터 ShortAnswerQuestion을 생성한다")
    void createsShortAnswerQuestionFromLlmResponse() {
      // given
      var questionSet = QuestionSetFixtures.basic();
      var llmResponse = new LlmGeneratedQuestionResponse(1, "정답은 무엇인가?", null, "정답입니다", "해설입니다");

      // when
      var question = ShortAnswerQuestion.createFromLlm(questionSet, llmResponse);

      // then
      assertThat(question.getQuestionText()).isEqualTo("정답은 무엇인가?");
      assertThat(question.getAnswer()).isEqualTo("정답입니다");
      assertThat(question.getExplanation()).isEqualTo("해설입니다");
      assertThat(question.getQuestionSet()).isEqualTo(questionSet);
    }

    @Test
    @DisplayName("options가 있으면 예외를 던진다")
    void throwsExceptionWhenOptionsExist() {
      // given
      var questionSet = QuestionSetFixtures.basic();
      var llmResponse =
          new LlmGeneratedQuestionResponse(1, "문제", java.util.List.of("옵션1"), "정답", "해설");

      // when & then
      assertThatThrownBy(() -> ShortAnswerQuestion.createFromLlm(questionSet, llmResponse))
          .isInstanceOf(InvalidQuestionException.class);
    }
  }

  @Nested
  @DisplayName("isCorrect")
  class DescribeIsCorrect {

    @Test
    @DisplayName("정답과 사용자 답이 일치하면 올바른 것으로 판단한다")
    void returnsTrueWhenAnswerMatches() {
      // given
      var question = ShortAnswerQuestion.builder().answer("정답").questionText("문제").build();

      // when
      boolean result = question.isCorrect("정답");

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("공백을 제거하고 비교하여 올바른 것으로 판단한다")
    void returnsTrueWhenAnswerMatchesAfterRemovingWhitespace() {
      // given
      var question = ShortAnswerQuestion.builder().answer("정답입니다").questionText("문제").build();

      // when
      boolean result = question.isCorrect("  정답입니다  ");

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("대소문자를 구분하지 않고 비교하여 올바른 것으로 판단한다")
    void returnsTrueWhenAnswerMatchesIgnoringCase() {
      // given
      var question = ShortAnswerQuestion.builder().answer("정답").questionText("문제").build();

      // when
      boolean result = question.isCorrect("정답");

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("정답과 사용자 답이 다르면 틀린 것으로 판단한다")
    void returnsFalseWhenAnswerDoesNotMatch() {
      // given
      var question = ShortAnswerQuestion.builder().answer("정답").questionText("문제").build();

      // when
      boolean result = question.isCorrect("오답");

      // then
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("사용자 답이 null이면 틀린 것으로 판단한다")
    void returnsFalseWhenUserAnswerIsNull() {
      // given
      var question = ShortAnswerQuestion.builder().answer("정답").questionText("문제").build();

      // when
      boolean result = question.isCorrect(null);

      // then
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("정답이 null이면 틀린 것으로 판단한다")
    void returnsFalseWhenAnswerIsNull() {
      // given
      var question = ShortAnswerQuestion.builder().answer(null).questionText("문제").build();

      // when
      boolean result = question.isCorrect("정답");

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
      var question = ShortAnswerQuestion.builder().answer("기존 정답").questionText("기존 문제").build();
      var param = new QuestionUpdateParam("새 문제", "새 해설", null, "새 정답");

      // when
      question.update(param);

      // then
      assertThat(question.getQuestionText()).isEqualTo("새 문제");
      assertThat(question.getExplanation()).isEqualTo("새 해설");
      assertThat(question.getAnswer()).isEqualTo("새 정답");
    }
  }

  @Nested
  @DisplayName("getQuestionType")
  class DescribeGetQuestionType {

    @Test
    @DisplayName("SHORT_ANSWER 타입을 반환한다")
    void returnsShortAnswerType() {
      // given
      var question = ShortAnswerQuestion.builder().answer("정답").build();

      // when
      QuestionType type = question.getQuestionType();

      // then
      assertThat(type).isEqualTo(QuestionType.SHORT_ANSWER);
    }
  }
}
