package kr.it.pullit.modules.projection.learnstats.events.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import kr.it.pullit.support.annotation.UnitTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@UnitTest
@DisplayName("LearnStatsAfterCommitExecutor 단위 테스트")
class LearnStatsAfterCommitExecutorTest {

  private LearnStatsAfterCommitExecutor sut;
  private MockedStatic<TransactionSynchronizationManager> tsm;

  @BeforeEach
  void setUp() {
    sut = new LearnStatsAfterCommitExecutor();
    // TransactionSynchronizationManager의 정적 메서드를 모킹 시작
    tsm = Mockito.mockStatic(TransactionSynchronizationManager.class);
  }

  @AfterEach
  void tearDown() {
    // 테스트 종료 후 모킹 해제
    tsm.close();
  }

  @Test
  @DisplayName("트랜잭션이 활성 상태가 아닐 때, 작업(Runnable)을 즉시 실행한다")
  void givenNoActiveTransaction_whenRunAfterCommit_thenExecutesTaskImmediately() {
    // given
    Runnable mockTask = mock(Runnable.class);
    tsm.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(false);

    // when
    sut.runAfterCommit(mockTask);

    // then
    verify(mockTask, times(1)).run();
  }

  @Test
  @DisplayName("트랜잭션이 활성 상태일 때, afterCommit 시점에 작업을 실행한다")
  void givenActiveTransaction_whenRunAfterCommit_thenExecutesTaskAfterCommit() {
    // given
    Runnable mockTask = mock(Runnable.class);
    tsm.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);

    ArgumentCaptor<TransactionSynchronization> captor =
        ArgumentCaptor.forClass(TransactionSynchronization.class);

    // when
    sut.runAfterCommit(mockTask);

    // then
    // 1. 작업(task)은 아직 실행되지 않아야 한다.
    verify(mockTask, never()).run();

    // 2. registerSynchronization 메서드가 TransactionSynchronization 객체와 함께 호출되었는지 확인
    tsm.verify(() -> TransactionSynchronizationManager.registerSynchronization(captor.capture()));

    // 3. 캡처된 TransactionSynchronization 객체의 afterCommit을 수동으로 호출
    TransactionSynchronization capturedSync = captor.getValue();
    capturedSync.afterCommit();

    // 4. 이제 작업(task)이 실행되었어야 한다.
    verify(mockTask, times(1)).run();
  }
}
