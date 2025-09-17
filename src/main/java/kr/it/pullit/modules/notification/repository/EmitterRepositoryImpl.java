package kr.it.pullit.modules.notification.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class EmitterRepositoryImpl implements EmitterRepository {

  private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

  public void save(Long userId, SseEmitter emitter) {
    emitters.put(userId, emitter);
  }

  public void deleteById(Long userId) {
    emitters.remove(userId);
  }

  public Optional<SseEmitter> findById(Long userId) {
    return Optional.ofNullable(emitters.get(userId));
  }
}
