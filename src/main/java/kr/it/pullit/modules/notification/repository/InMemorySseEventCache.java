package kr.it.pullit.modules.notification.repository;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;
import kr.it.pullit.modules.notification.domain.EventData;
import org.springframework.stereotype.Repository;

/** 사용자별 SSE 이벤트를 메모리에 저장하는 스레드 안전 캐시. 재연결 시 누락된 이벤트를 클라이언트에게 재전송하는 데 사용된다. */
@Repository
public class InMemorySseEventCache implements SseEventCache {
  private static final int MAX_CACHE_SIZE_PER_USER = 1300;

  private final Map<Long, Deque<EventData>> userEventDeques = new ConcurrentHashMap<>();

  /**
   * 특정 사용자의 이벤트 큐에 새 이벤트를 추가한다.
   *
   * <p>큐가 가득 차면, 가장 오래된 이벤트를 제거하고 새 이벤트를 추가한다.
   *
   * @param userId 이벤트를 수신할 사용자 ID
   * @param event 저장할 이벤트 데이터
   */
  @Override
  public void put(Long userId, EventData event) {
    Deque<EventData> userEvents =
        userEventDeques.computeIfAbsent(
            userId, k -> new LinkedBlockingDeque<>(MAX_CACHE_SIZE_PER_USER));

    synchronized (userEvents) {
      if (!userEvents.offerLast(event)) {
        userEvents.pollFirst();
        userEvents.offerLast(event);
      }
    }
  }

  /**
   * 특정 사용자의 이벤트 큐에서 마지막으로 수신한 이벤트 ID 이후의 모든 이벤트를 조회한다.
   *
   * <p>읽기 작업 중 발생할 수 있는 동시성 문제를 피하기 위해, 동기화 블록 안에서 큐의 스냅샷을 생성하여 사용한다. 이를 통해 락(lock) 유지 시간을 최소화하고
   * 안전하게 데이터를 조회한다.
   *
   * @param userId 이벤트를 조회할 사용자 ID
   * @param lastEventId 클라이언트가 마지막으로 수신한 이벤트의 ID
   * @return 마지막 이벤트 ID 이후에 발생한 이벤트 목록
   */
  @Override
  public List<EventData> findAllByUserIdAfter(Long userId, long lastEventId) {
    Deque<EventData> userEvents = userEventDeques.get(userId);
    if (userEvents == null) {
      return List.of();
    }

    List<EventData> snapshot;
    synchronized (userEvents) {
      snapshot = new ArrayList<>(userEvents);
    }

    return snapshot.stream().filter(event -> event.id() > lastEventId).collect(Collectors.toList());
  }
}
