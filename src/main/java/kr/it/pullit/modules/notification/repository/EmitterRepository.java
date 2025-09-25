package kr.it.pullit.modules.notification.repository;

import java.util.Map;
import java.util.Optional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface EmitterRepository {

  void save(Long userId, SseEmitter emitter);

  void deleteById(Long userId);

  Optional<SseEmitter> findById(Long userId);

  Map<Long, SseEmitter> findAll();

  boolean notExistsById(Long userId);
}
