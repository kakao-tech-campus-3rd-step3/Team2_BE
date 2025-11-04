package kr.it.pullit.modules.questionset.service.creationstrategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import kr.it.pullit.modules.questionset.exception.InvalidQuestionException;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@MockitoUnitTest
@DisplayName("QuestionCreationStrategyFactory 단위 테스트")
class QuestionCreationStrategyFactoryTest {

  private QuestionCreationStrategyFactory factory;

  @BeforeEach
  void setUp() {
    factory =
        new QuestionCreationStrategyFactory(
            List.of(
                new MultipleChoiceQuestionCreationStrategy(),
                new TrueFalseQuestionCreationStrategy(),
                new ShortAnswerQuestionCreationStrategy()));
    factory.init();
  }

  @Test
  @DisplayName("문제 유형에 맞는 전략을 반환한다")
  void returnsStrategyForType() {
    assertThat(factory.getStrategy(QuestionType.MULTIPLE_CHOICE))
        .isInstanceOf(MultipleChoiceQuestionCreationStrategy.class);
    assertThat(factory.getStrategy(QuestionType.TRUE_FALSE))
        .isInstanceOf(TrueFalseQuestionCreationStrategy.class);
    assertThat(factory.getStrategy(QuestionType.SHORT_ANSWER))
        .isInstanceOf(ShortAnswerQuestionCreationStrategy.class);
  }

  @Test
  @DisplayName("유형이 없으면 예외를 던진다")
  void throwsWhenTypeNull() {
    assertThatThrownBy(() -> factory.getStrategy(null))
        .isInstanceOf(InvalidQuestionException.class);
  }

  @Test
  @DisplayName("지원하지 않는 유형이면 예외를 던진다")
  void throwsWhenUnsupportedType() {
    assertThatThrownBy(() -> factory.getStrategy(QuestionType.SUBJECTIVE))
        .isInstanceOf(InvalidQuestionException.class);
  }
}
