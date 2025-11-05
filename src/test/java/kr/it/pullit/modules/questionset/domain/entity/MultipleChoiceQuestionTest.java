package kr.it.pullit.modules.questionset.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
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
@DisplayName("MultipleChoiceQuestion 단위 테스트")
class MultipleChoiceQuestionTest {

  @Nested
  @DisplayName("createFromLlm")
  class DescribeCreateFromLlm {

    @Test
    @DisplayName("LLM 응답으로부터 MultipleChoiceQuestion을 생성한다")
    void createsMultipleChoiceQuestionFromLlmResponse() {
      // given
      var questionSet = QuestionSetFixtures.basic();
      var options = List.of("보기1", "보기2", "보기3", "보기4");
      var llmResponse = new LlmGeneratedQuestionResponse(1, "정답은 무엇인가?", options, "보기1", "해설입니다");

      // when
      var question = MultipleChoiceQuestion.createFromLlm(questionSet, llmResponse);

      // then
      assertThat(question.getQuestionText()).isEqualTo("정답은 무엇인가?");
      assertThat(question.getOptions()).isEqualTo(options);
      assertThat(question.getAnswer()).isEqualTo("보기1");
      assertThat(question.getExplanation()).isEqualTo("해설입니다");
      assertThat(question.getQuestionSet()).isEqualTo(questionSet);
    }
  }

  @Nested
  @DisplayName("isCorrect")
  class DescribeIsCorrect {

    @Test
    @DisplayName("정답과 사용자 답이 일치하면 올바른 것으로 판단한다")
    void returnsTrueWhenAnswerMatches() {
      // given
      var question =
          MultipleChoiceQuestion.builder()
              .answer("보기1")
              .questionText("문제")
              .options(List.of("보기1", "보기2"))
              .build();

      // when
      boolean result = question.isCorrect("보기1");

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("대소문자를 구분하지 않고 비교하여 올바른 것으로 판단한다")
    void returnsTrueWhenAnswerMatchesIgnoringCase() {
      // given
      var question =
          MultipleChoiceQuestion.builder()
              .answer("보기1")
              .questionText("문제")
              .options(List.of("보기1", "보기2"))
              .build();

      // when
      boolean result = question.isCorrect("  보기1  ");

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("정답과 사용자 답이 다르면 틀린 것으로 판단한다")
    void returnsFalseWhenAnswerDoesNotMatch() {
      // given
      var question =
          MultipleChoiceQuestion.builder()
              .answer("보기1")
              .questionText("문제")
              .options(List.of("보기1", "보기2"))
              .build();

      // when
      boolean result = question.isCorrect("보기2");

      // then
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("사용자 답이 null이면 틀린 것으로 판단한다")
    void returnsFalseWhenUserAnswerIsNull() {
      // given
      var question =
          MultipleChoiceQuestion.builder()
              .answer("보기1")
              .questionText("문제")
              .options(List.of("보기1", "보기2"))
              .build();

      // when
      boolean result = question.isCorrect(null);

      // then
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("정답이 null이면 틀린 것으로 판단한다")
    void returnsFalseWhenAnswerIsNull() {
      // given
      var question =
          MultipleChoiceQuestion.builder()
              .answer(null)
              .questionText("문제")
              .options(List.of("보기1", "보기2"))
              .build();

      // when
      boolean result = question.isCorrect("보기1");

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
      var question =
          MultipleChoiceQuestion.builder()
              .answer("기존 정답")
              .questionText("기존 문제")
              .options(List.of("기존1", "기존2"))
              .build();
      var newOptions = List.of("새1", "새2", "새3");
      var param = new QuestionUpdateParam("새 문제", "새 해설", newOptions, "새 정답");

      // when
      question.update(param);

      // then
      assertThat(question.getQuestionText()).isEqualTo("새 문제");
      assertThat(question.getExplanation()).isEqualTo("새 해설");
      assertThat(question.getOptions()).isEqualTo(newOptions);
      assertThat(question.getAnswer()).isEqualTo("새 정답");
    }

    @Test
    @DisplayName("options가 비어있으면 예외를 던진다")
    void throwsExceptionWhenOptionsAreEmpty() {
      // given
      var question =
          MultipleChoiceQuestion.builder()
              .answer("정답")
              .questionText("문제")
              .options(List.of("보기1", "보기2"))
              .build();
      var param = new QuestionUpdateParam("문제", "해설", List.of(), "정답");

      // when & then
      assertThatThrownBy(() -> question.update(param)).isInstanceOf(InvalidQuestionException.class);
    }
  }

  @Nested
  @DisplayName("getQuestionType")
  class DescribeGetQuestionType {

    @Test
    @DisplayName("MULTIPLE_CHOICE 타입을 반환한다")
    void returnsMultipleChoiceType() {
      // given
      var question =
          MultipleChoiceQuestion.builder().answer("보기1").options(List.of("보기1", "보기2")).build();

      // when
      QuestionType type = question.getQuestionType();

      // then
      assertThat(type).isEqualTo(QuestionType.MULTIPLE_CHOICE);
    }
  }
}
