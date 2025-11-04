package kr.it.pullit.modules.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@DisplayName("EventData 단위 테스트")
@MockitoUnitTest
class EventDataTest {
  @Test
  @DisplayName("toSseEventBuilder는 ID가 없을 때 ID를 포함하지 않은 빌더를 반환한다")
  void givenWithoutIdReturnsBuilderWithoutId() {
    // given
    EventData eventData = EventData.heartbeat(1L);

    // when
    SseEmitter.SseEventBuilder builder = eventData.toSseEventBuilder();

    // then
    assertThat(builder.build().toString()).doesNotContain("id:");
  }
}
