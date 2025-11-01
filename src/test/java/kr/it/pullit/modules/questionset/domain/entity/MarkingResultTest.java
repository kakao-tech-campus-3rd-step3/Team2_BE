package kr.it.pullit.modules.questionset.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.it.pullit.support.annotation.MockitoUnitTest;
import kr.it.pullit.support.fixture.QuestionFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@MockitoUnitTest
@DisplayName("MarkingResult 엔티티 테스트")
class MarkingResultTest {

  @Nested
  @DisplayName("create 메서드는")
  class DescribeCreate {

    @Test
    @DisplayName("유효한 파라미터로 객체를 생성한다")
    void shouldCreateWithValidParameters() {
      // given
      Long memberId = 1L;
      Question question = QuestionFixtures.aCorrectTrueFalseQuestion();
      boolean isCorrect = true;

      // when
      MarkingResult result = MarkingResult.create(memberId, question, isCorrect);

      // then
      assertThat(result.getMemberId()).isEqualTo(memberId);
      assertThat(result.getQuestion()).isEqualTo(question);
      assertThat(result.isCorrect()).isTrue();
    }

    @Test
    @DisplayName("memberId가 null이면 예외를 발생시킨다")
    void shouldThrowExceptionWhenMemberIdIsNull() {
      // given
      Question question = QuestionFixtures.aCorrectTrueFalseQuestion();

      // when & then
      assertThatThrownBy(() -> MarkingResult.create(null, question, true))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("memberId는 null일 수 없습니다");
    }

    @Test
    @DisplayName("question이 null이면 예외를 발생시킨다")
    void shouldThrowExceptionWhenQuestionIsNull() {
      // given
      Long memberId = 1L;

      // when & then
      assertThatThrownBy(() -> MarkingResult.create(memberId, null, true))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("question은 null일 수 없습니다");
    }
  }
}
