package kr.it.pullit.modules.questionset.client.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LlmExceptionTest {

  @Test
  @DisplayName("생성 실패 사유를 포함한 예외를 생성한다")
  void generationFailedIncludesReason() {
    LlmException exception = LlmException.ofTemporary("네트워크 오류");

    assertThat(exception.getErrorCode()).isEqualTo(LlmErrorCode.LLM_GENERATION_FAILED);
    assertThat(exception).hasMessageContaining("네트워크 오류");
  }

  @Test
  @DisplayName("원인 예외를 전달하면 해당 메시지를 포함한다")
  void withCauseWrapsOriginalMessage() {
    IllegalStateException cause = new IllegalStateException("API unavailable");

    LlmException exception = LlmException.from(cause);

    assertThat(exception.getErrorCode()).isEqualTo(LlmErrorCode.LLM_GENERATION_FAILED);
    assertThat(exception).hasMessageContaining("API unavailable");
  }
}
