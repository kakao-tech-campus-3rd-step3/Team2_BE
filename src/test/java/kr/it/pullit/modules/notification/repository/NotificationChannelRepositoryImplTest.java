package kr.it.pullit.modules.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import kr.it.pullit.modules.notification.domain.NotificationChannel;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@MockitoUnitTest
@DisplayName("NotificationChannelRepositoryImpl 단위 테스트")
class NotificationChannelRepositoryImplTest {

  private NotificationChannelRepositoryImpl repository;

  private static final Long USER_ID_1 = 1L;
  private static final Long USER_ID_2 = 2L;

  @BeforeEach
  void setUp() {
    repository = new NotificationChannelRepositoryImpl();
  }

  @Test
  @DisplayName("채널을 저장하고 ID로 조회한다")
  void saveAndFindById() {
    // given
    NotificationChannel channel1 = mock(NotificationChannel.class);
    when(channel1.memberId()).thenReturn(USER_ID_1);

    // when
    repository.save(channel1);
    Optional<NotificationChannel> found = repository.findById(USER_ID_1);

    // then
    assertThat(found).isPresent();
    assertThat(found.get()).isEqualTo(channel1);
  }

  @Test
  @DisplayName("ID로 채널을 삭제한다")
  void deleteById() {
    // given
    NotificationChannel channel1 = mock(NotificationChannel.class);
    when(channel1.memberId()).thenReturn(USER_ID_1);
    repository.save(channel1);

    // when
    repository.deleteById(USER_ID_1);

    // then
    Optional<NotificationChannel> found = repository.findById(USER_ID_1);
    assertThat(found).isNotPresent();
  }

  @Test
  @DisplayName("존재하지 않는 채널 조회 시 Optional.empty()를 반환한다")
  void findById_WhenNotExists_ShouldReturnEmpty() {
    // when
    Optional<NotificationChannel> found = repository.findById(999L);

    // then
    assertThat(found).isNotPresent();
  }

  @Test
  @DisplayName("모든 채널을 조회한다")
  void findAll() {
    // given
    NotificationChannel channel1 = mock(NotificationChannel.class);
    when(channel1.memberId()).thenReturn(USER_ID_1);
    repository.save(channel1);

    NotificationChannel channel2 = mock(NotificationChannel.class);
    when(channel2.memberId()).thenReturn(USER_ID_2);
    repository.save(channel2);

    // when
    Map<Long, NotificationChannel> all = repository.findAll();

    // then
    assertThat(all)
        .hasSize(2)
        .containsEntry(USER_ID_1, channel1)
        .containsEntry(USER_ID_2, channel2);
  }

  @Test
  @DisplayName("ID로 채널 존재 여부를 확인한다 (notExistsById, 존재하지 않을 때)")
  void notExistsById_WhenNotExists() {
    // when
    boolean notExists = repository.notExistsById(USER_ID_1);

    // then
    assertThat(notExists).isTrue();
  }

  @Test
  @DisplayName("ID로 채널 존재 여부를 확인한다 (notExistsById, 존재할 때)")
  void notExistsById_WhenExists() {
    // given
    NotificationChannel channel1 = mock(NotificationChannel.class);
    when(channel1.memberId()).thenReturn(USER_ID_1);
    repository.save(channel1);

    // when
    boolean notExists = repository.notExistsById(USER_ID_1);

    // then
    assertThat(notExists).isFalse();
  }
}
