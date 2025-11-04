package kr.it.pullit.modules.questionset.api;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SseDataCallbackTest {

  @Test
  @DisplayName("기본 onComplete와 onError 구현은 예외를 던지지 않는다")
  void defaultCallbacksDoNothing() {
    SseDataCallback callback = json -> {};

    assertThatCode(callback::onComplete).doesNotThrowAnyException();
    assertThatCode(() -> callback.onError(new RuntimeException("boom"))).doesNotThrowAnyException();
  }
}
