package kr.it.pullit.modules.notification.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import kr.it.pullit.modules.notification.domain.NotificationChannel;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationChannelRepositoryImpl implements NotificationChannelRepository {

  private final Map<Long, NotificationChannel> emitters = new ConcurrentHashMap<>();

  public void save(NotificationChannel channel) {
    emitters.put(channel.memberId(), channel);
  }

  public void deleteById(Long userId) {
    emitters.remove(userId);
  }

  public Optional<NotificationChannel> findById(Long userId) {
    return Optional.ofNullable(emitters.get(userId));
  }

  @Override
  public Map<Long, NotificationChannel> findAll() {
    return Map.copyOf(emitters);
  }

  @Override
  public boolean notExistsById(Long userId) {
    return !emitters.containsKey(userId);
  }
}
