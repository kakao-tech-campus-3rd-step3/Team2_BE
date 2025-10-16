package kr.it.pullit.modules.notification.repository;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import kr.it.pullit.modules.notification.domain.EventData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class InMemorySseEventCache implements SseEventCache {

  private static final int DEFAULT_CACHE_SIZE = 10000;
  private final Deque<EventData> cache = new ConcurrentLinkedDeque<>();
  private final AtomicLong idCounter = new AtomicLong(0);

  @Override
  public EventData put(EventData event) {
    long id = idCounter.incrementAndGet();
    EventData newEventData = event.withId(id);

    cache.add(newEventData);
    if (cache.size() > DEFAULT_CACHE_SIZE) {
      cache.pollFirst();
    }
    log.debug("Event cached: {}", newEventData);
    return newEventData;
  }

  @Override
  public List<EventData> findAllByUserIdAfter(Long userId, long lastEventId) {
    return cache.stream()
        .filter(
            event ->
                event.userId() != null && event.userId().equals(userId) && event.id() > lastEventId)
        .collect(Collectors.toList());
  }

  @Override
  public void clear() {
    cache.clear();
    idCounter.set(0);
  }
}
