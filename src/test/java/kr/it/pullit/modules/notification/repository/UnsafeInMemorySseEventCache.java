package kr.it.pullit.modules.notification.repository;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;
import kr.it.pullit.modules.notification.domain.EventData;
import org.springframework.stereotype.Repository;

@Repository
public class UnsafeInMemorySseEventCache implements SseEventCache {
  private static final int MAX_CACHE_SIZE_PER_USER = 100;

  /**
   * Key: userId, Value: A Deque of events for the user (like a conveyor belt). Both
   * ConcurrentHashMap and LinkedBlockingDeque are thread-safe data structures.
   */
  private final Map<Long, Deque<EventData>> userEventDeques = new ConcurrentHashMap<>();

  @Override
  public void put(Long userId, EventData event) {
    // computeIfAbsent is atomic, so retrieving the Deque itself is thread-safe.
    Deque<EventData> userEvents =
        userEventDeques.computeIfAbsent(
            userId, k -> new LinkedBlockingDeque<>(MAX_CACHE_SIZE_PER_USER));

    // If the queue is full, remove the oldest event.
    if (!userEvents.offerLast(event)) {
      userEvents.pollFirst();
      userEvents.offerLast(event);
    }
  }

  @Override
  public List<EventData> findAllByUserIdAfter(Long userId, long lastEventId) {
    Deque<EventData> userEvents = userEventDeques.get(userId);
    if (userEvents == null) {
      return List.of();
    }

    return userEvents.stream()
        .filter(event -> event.id() > lastEventId)
        .collect(Collectors.toList());
  }
}
