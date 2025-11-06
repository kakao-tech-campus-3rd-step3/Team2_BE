package kr.it.pullit.modules.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import kr.it.pullit.modules.notification.config.NotificationProperties;
import kr.it.pullit.modules.notification.domain.EventData;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

@MockitoUnitTest
@DisplayName("InMemorySseEventCache 단위 테스트")
class InMemorySseEventCacheTest {

  @Mock private NotificationProperties notificationProperties;

  private InMemorySseEventCache sseEventCache;

  private static final Long USER_ID = 1L;

  @BeforeEach
  void setUp() {
    when(notificationProperties.getSseCacheSize()).thenReturn(3);
    sseEventCache = new InMemorySseEventCache(notificationProperties);
  }

  @Test
  @DisplayName("이벤트를 캐시에 저장한다")
  void put_ShouldStoreEvent() {
    // given
    EventData event = new EventData(1L, USER_ID, "test event", "test data");

    // when
    sseEventCache.put(event);

    // then
    List<EventData> events = sseEventCache.pollAllByUserIdAfter(USER_ID, 0L);
    assertThat(events).hasSize(1).contains(event);
  }

  @Test
  @DisplayName("캐시 크기를 초과하면 가장 오래된 이벤트가 제거된다")
  void put_WhenCacheSizeExceeded_ShouldEvictOldestEvent() {
    // given
    EventData event1 = new EventData(1L, USER_ID, "event1", "data1");
    EventData event2 = new EventData(2L, USER_ID, "event2", "data2");
    EventData event3 = new EventData(3L, USER_ID, "event3", "data3");
    EventData event4 = new EventData(4L, USER_ID, "event4", "data4");

    sseEventCache.put(event1);
    sseEventCache.put(event2);
    sseEventCache.put(event3);

    // when
    sseEventCache.put(event4);

    // then
    List<EventData> events = sseEventCache.pollAllByUserIdAfter(USER_ID, 0L);
    assertThat(events).hasSize(3).extracting(EventData::id).containsExactly(2L, 3L, 4L);
  }

  @Test
  @DisplayName("lastEventId 이후의 모든 이벤트를 조회한다")
  void pollAllByUserIdAfter_ShouldReturnEventsAfterLastEventId() {
    // given
    sseEventCache.put(new EventData(1L, USER_ID, "event1", "data1"));
    sseEventCache.put(new EventData(2L, USER_ID, "event2", "data2"));
    sseEventCache.put(new EventData(3L, USER_ID, "event3", "data3"));

    // when
    List<EventData> events = sseEventCache.pollAllByUserIdAfter(USER_ID, 1L);

    // then
    assertThat(events).hasSize(2).extracting(EventData::id).containsExactly(2L, 3L);
  }

  @Test
  @DisplayName("사용자에 대한 캐시가 없으면 빈 리스트를 반환한다")
  void pollAllByUserIdAfter_WhenNoCacheForUser_ShouldReturnEmptyList() {
    // when
    List<EventData> events = sseEventCache.pollAllByUserIdAfter(USER_ID, 0L);

    // then
    assertThat(events).isEmpty();
  }

  @Test
  @DisplayName("조회된 이벤트는 다시 캐시에 저장되어야 한다")
  void pollAllByUserIdAfter_ShouldKeepPolledEventsInCache() {
    // given
    EventData event = new EventData(1L, USER_ID, "event1", "data1");
    sseEventCache.put(event);

    // when
    sseEventCache.pollAllByUserIdAfter(USER_ID, 0L);
    List<EventData> secondPoll = sseEventCache.pollAllByUserIdAfter(USER_ID, 0L);

    // then
    assertThat(secondPoll).hasSize(1).contains(event);
  }

  @Test
  @DisplayName("clear는 모든 캐시를 비운다")
  void clear_ShouldRemoveAllEvents() {
    // given
    sseEventCache.put(new EventData(1L, USER_ID, "test event", "test data"));

    // when
    sseEventCache.clear();

    // then
    List<EventData> events = sseEventCache.pollAllByUserIdAfter(USER_ID, 0L);
    assertThat(events).isEmpty();
  }
}
