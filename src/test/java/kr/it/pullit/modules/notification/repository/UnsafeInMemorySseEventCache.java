package kr.it.pullit.modules.notification.repository;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;
import kr.it.pullit.modules.notification.domain.EventData;

/**
 * A non-thread-safe implementation of SseEventCache for testing concurrency issues. This version
 * lacks the synchronized blocks, making it susceptible to race conditions.
 */
public class UnsafeInMemorySseEventCache implements SseEventCache {
  private static final int MAX_CACHE_SIZE_PER_USER = 10000; // Increase size for high-volume tests
  private final Map<Long, Deque<EventData>> userEventDeques = new ConcurrentHashMap<>();

  @Override
  public void put(Long userId, EventData event) {
    Deque<EventData> userEvents =
        userEventDeques.computeIfAbsent(
            userId, k -> new LinkedBlockingDeque<>(MAX_CACHE_SIZE_PER_USER));

    // This block is NOT synchronized, creating a race condition with the read operation.
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

    // The stream operation here is NOT synchronized with the put operation.
    // The iterator created by stream() is weakly consistent and may not reflect
    // concurrent modifications, leading to event loss.
    return userEvents.stream()
        .filter(event -> event.id() > lastEventId)
        .collect(Collectors.toList());
  }
}
