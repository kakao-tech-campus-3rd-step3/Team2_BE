package kr.it.pullit.modules.learningsource.source.listener;

import kr.it.pullit.modules.learningsource.source.event.SourceExtractionCompleteEvent;
import kr.it.pullit.modules.learningsource.source.event.SourceExtractionFailureEvent;
import kr.it.pullit.modules.learningsource.source.event.SourceExtractionStartEvent;
import kr.it.pullit.modules.learningsource.source.repository.SourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SourceStatusEventHandler {

  private final SourceRepository sourceRepository;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleSourceExtractionStart(final SourceExtractionStartEvent event) {
    sourceRepository
        .findById(event.sourceId())
        .ifPresent(
            source -> {
              source.startProcessing();
              log.info("Source[id={}] 상태를 PROCESSING으로 변경했습니다.", source.getId());
            });
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleSourceExtractionComplete(final SourceExtractionCompleteEvent event) {
    sourceRepository
        .findById(event.sourceId())
        .ifPresent(
            source -> {
              source.markAsReady();
              log.info("Source[id={}] 상태를 READY로 변경했습니다.", source.getId());
            });
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleSourceExtractionFailure(final SourceExtractionFailureEvent event) {
    sourceRepository
        .findById(event.sourceId())
        .ifPresent(
            source -> {
              source.markAsFailed();
              log.error(
                  "Source[id={}] 상태를 FAILED로 변경했습니다. 사유: {}",
                  source.getId(),
                  event.cause().getMessage(),
                  event.cause());
            });
  }
}
