package kr.it.pullit.modules.questionset.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.it.pullit.modules.questionset.enums.DifficultyType;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LlmPrompt 생성 테스트")
class LlmPromptTest {

  @Test
  @DisplayName("난이도와 유형에 따라 프롬프트를 생성한다")
  void composesPrompt() {
    LlmPrompt prompt = LlmPrompt.compose(DifficultyType.EASY, QuestionType.TRUE_FALSE);

    assertThat(prompt.value()).contains(DifficultyType.EASY.getPrompt());
    assertThat(prompt.value()).contains(QuestionType.TRUE_FALSE.getType());
  }

  @Test
  @DisplayName("값이 null이면 예외를 던진다")
  void throwsWhenNullValue() {
    assertThatThrownBy(() -> new LlmPrompt(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("길이가 너무 길면 예외를 던진다")
  void throwsWhenTooLong() {
    String tooLong = "a".repeat(10_001);

    assertThatThrownBy(() -> new LlmPrompt(tooLong)).isInstanceOf(IllegalArgumentException.class);
  }
}
