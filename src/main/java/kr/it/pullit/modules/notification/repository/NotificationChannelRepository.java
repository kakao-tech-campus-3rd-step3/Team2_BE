package kr.it.pullit.modules.notification.repository;

import java.util.Map;
import java.util.Optional;
import kr.it.pullit.modules.notification.domain.NotificationChannel;

public interface NotificationChannelRepository {

  void save(NotificationChannel channel);

  void deleteById(Long userId);

  Optional<NotificationChannel> findById(Long userId);

  Map<Long, NotificationChannel> findAll();

  boolean notExistsById(Long userId);
}
