package kr.it.pullit.modules.notification.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import kr.it.pullit.modules.notification.config.NotificationProperties;
import kr.it.pullit.modules.notification.domain.EventData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class InMemorySseEventCache implements SseEventCache {

  private final int cacheSize;
  private final Map<Long, Queue<EventData>> cache = new ConcurrentHashMap<>();
  private final AtomicLong idCounter = new AtomicLong(0);

  public InMemorySseEventCache(NotificationProperties notificationProperties) {
    this.cacheSize = notificationProperties.getSseCacheSize();
  }

  @Override
  public void put(EventData event) {
    cache.putIfAbsent(event.userId(), new ConcurrentLinkedDeque<>());

    Queue<EventData> userCache = cache.get(event.userId());
    userCache.add(event);

    if (userCache.size() > cacheSize) {
      userCache.poll();
    }

    log.debug("Event cached: {}", event);
  }

  public List<EventData> pollAllByUserIdAfter(Long userId, long lastEventId) {
    List<EventData> result = new ArrayList<>();

    Queue<EventData> userCache = cache.getOrDefault(userId, null);
    if (userCache == null) {
      return result;
    }

    if (userCache.isEmpty()) {
      return result;
    }

    int initialSize = userCache.size();

    while (initialSize-- > 0) {
      EventData event = userCache.poll();
      if (event == null) {
        break;
      }

      if (event.id() > lastEventId) {
        result.add(event);
        userCache.add(event);
      }
    }

    return result;
  }

  @Override
  public void clear() {
    cache.clear();
    idCounter.set(0);
  }
}
