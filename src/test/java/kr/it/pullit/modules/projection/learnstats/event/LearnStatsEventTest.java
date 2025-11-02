package kr.it.pullit.modules.projection.learnstats.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.it.pullit.modules.projection.learnstats.event.dto.MemberIdPayload;
import kr.it.pullit.modules.projection.learnstats.event.dto.QuestionSetSolvedPayload;
import kr.it.pullit.modules.projection.learnstats.event.handler.LearnStatsEventDispatcher;
import kr.it.pullit.modules.projection.learnstats.event.publisher.LearnStatsEventPublisher;
import kr.it.pullit.modules.projection.learnstats.service.LearnStatsService;
import kr.it.pullit.modules.projection.outbox.domain.OutboxEvent;
import kr.it.pullit.modules.projection.outbox.publisher.OutboxEventPublisher;
import kr.it.pullit.support.annotation.SpringUnitTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringUnitTest
@DisplayName("LearnStatsEvent 단위 테스트")
@Import(LearnStatsEventTest.Config.class)
class LearnStatsEventTest {

  @Autowired private LearnStatsEventPublisher publisher;
  @Autowired private LearnStatsEventDispatcher dispatcher;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private OutboxEventPublisher outboxPublisher;
  @MockitoBean private LearnStatsService learnStatsService;

  @AfterEach
  void tearDown() {
    reset(outboxPublisher, learnStatsService);
  }

  @TestConfiguration
  static class Config {
    @Bean
    public LearnStatsEventPublisher learnStatsEventPublisher(
        OutboxEventPublisher outboxPublisher, ObjectMapper objectMapper) {
      return new LearnStatsEventPublisher(outboxPublisher, objectMapper);
    }

    @Bean
    public LearnStatsEventDispatcher learnStatsEventDispatcher(
        LearnStatsService learnStatsService, ObjectMapper objectMapper) {
      return new LearnStatsEventDispatcher(learnStatsService, objectMapper);
    }

    @Bean
    public ObjectMapper objectMapper() {
      return new ObjectMapper();
    }
  }

  @Nested
  @DisplayName("Publisher")
  class Publisher {
    @Test
    @DisplayName("주간 초기화 이벤트를 발행한다")
    @SneakyThrows
    void publishWeeklyResetEvent() {
      // given
      Long memberId = 1L;

      // when
      publisher.publishWeeklyReset(memberId);

      // then
      ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
      verify(outboxPublisher).publish(captor.capture());

      OutboxEvent publishedEvent = captor.getValue();
      assertThat(publishedEvent.getEventType())
          .isEqualTo(LearnStatsEventType.WEEKLY_RESET.getEventType());

      MemberIdPayload payload =
          objectMapper.readValue(publishedEvent.getPayload(), MemberIdPayload.class);
      assertThat(payload.memberId()).isEqualTo(memberId);
    }

    @Test
    @DisplayName("문제집 풀이 완료 이벤트를 발행한다")
    @SneakyThrows
    void publishQuestionSetSolvedEvent() {
      // given
      Long memberId = 1L;
      int solvedCount = 15;

      // when
      publisher.publishQuestionSetSolved(memberId, solvedCount);

      // then
      ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
      verify(outboxPublisher).publish(captor.capture());

      OutboxEvent publishedEvent = captor.getValue();
      assertThat(publishedEvent.getEventType())
          .isEqualTo(LearnStatsEventType.QUESTION_SET_SOLVED.getEventType());

      QuestionSetSolvedPayload payload =
          objectMapper.readValue(publishedEvent.getPayload(), QuestionSetSolvedPayload.class);
      assertThat(payload.memberId()).isEqualTo(memberId);
      assertThat(payload.solvedQuestionCount()).isEqualTo(solvedCount);
    }
  }

  @Nested
  @DisplayName("Dispatcher")
  class Dispatcher {
    @Test
    @DisplayName("주간 초기화 이벤트를 처리한다")
    @SneakyThrows
    void dispatchWeeklyResetEvent() {
      // given
      MemberIdPayload payload = new MemberIdPayload(1L);
      OutboxEvent event =
          OutboxEvent.of(
              LearnStatsEventType.WEEKLY_RESET.getEventType(),
              objectMapper.writeValueAsString(payload));

      // when
      boolean dispatched = dispatcher.dispatch(event);

      // then
      assertThat(dispatched).isTrue();
      verify(learnStatsService).applyWeeklyReset(1L);
    }

    @Test
    @DisplayName("문제집 풀이 완료 이벤트를 처리한다")
    @SneakyThrows
    void dispatchQuestionSetSolvedEvent() {
      // given
      QuestionSetSolvedPayload payload = new QuestionSetSolvedPayload(1L, 15);
      OutboxEvent event =
          OutboxEvent.of(
              LearnStatsEventType.QUESTION_SET_SOLVED.getEventType(),
              objectMapper.writeValueAsString(payload));

      // when
      boolean dispatched = dispatcher.dispatch(event);

      // then
      assertThat(dispatched).isTrue();
      verify(learnStatsService).applyQuestionSetSolved(1L, 15);
    }

    @Test
    @DisplayName("관련 없는 이벤트는 무시한다")
    void ignoreUnrelatedEvent() {
      // given
      OutboxEvent event = OutboxEvent.of("UNRELATED_EVENT", "{}");

      // when
      boolean dispatched = dispatcher.dispatch(event);

      // then
      assertThat(dispatched).isFalse();
      verify(learnStatsService, never()).applyWeeklyReset(anyLong());
      verify(learnStatsService, never()).applyQuestionSetSolved(anyLong(), anyInt());
    }
  }
}
