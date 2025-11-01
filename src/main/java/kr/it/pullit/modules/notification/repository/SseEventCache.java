package kr.it.pullit.modules.notification.repository;

import java.util.List;
import kr.it.pullit.modules.notification.domain.EventData;

public interface SseEventCache {

  EventData put(EventData event);

  void clear();

  void clearByUserId(Long userId);

  /**
   * 이 사용자를 위해 저장된 이벤트들 중에서, 클라이언트가 마지막으로 받았던 이벤트 ID보다 더 최신인 것들을 모두 찾아서 목록으로 반환한다. 재연결한 클라이언트에게, 연결이
   * 끊어져 있는 동안 놓쳤던 이벤트들만 정확히 골라서 다시 보내주기 위한 메서드
   *
   * @param userId 사용자 ID
   * @param lastEventId 클라이언트가 마지막으로 받은 이벤트 ID
   * @return 유실된 이벤트들의 목록
   */
  List<EventData> findAllByUserIdAfter(Long userId, long lastEventId);
}
