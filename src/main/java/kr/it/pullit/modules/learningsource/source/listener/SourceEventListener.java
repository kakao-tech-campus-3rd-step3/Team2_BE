package kr.it.pullit.modules.learningsource.source.listener;

import kr.it.pullit.modules.learningsource.source.event.SourceUploadCompleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SourceEventListener {

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleSourceUploadComplete(final SourceUploadCompleteEvent event) {
    log.info(
        "소스 업로드 완료 이벤트 수신 (Source ID: {}). AI 작업을 트리거합니다. S3 경로: {}",
        event.sourceId(),
        event.s3Url());
    // In a real scenario, you would call a service to start the AI processing job.
    // e.g., aiService.startPdfProcessing(event.sourceId(), event.s3Url());
  }
}
