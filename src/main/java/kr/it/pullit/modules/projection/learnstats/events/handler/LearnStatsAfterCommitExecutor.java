package kr.it.pullit.modules.projection.learnstats.events.handler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** 트랜잭션 커밋 이후에만 디스패치 시작(큐 등록 등). 실제 로직은 환경마다 다르니, 콜백 틀만 유지 예시. */
@Component
public class LearnStatsAfterCommitExecutor {

  public void runAfterCommit(Runnable task) {
    if (!TransactionSynchronizationManager.isSynchronizationActive()) {
      task.run();
      return;
    }

    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            task.run();
          }
        });
  }
}
