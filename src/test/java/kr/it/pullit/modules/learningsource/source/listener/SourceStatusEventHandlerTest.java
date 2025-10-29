package kr.it.pullit.modules.learningsource.source.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.it.pullit.modules.learningsource.source.constant.SourceStatus;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.domain.entity.SourceCreationParam;
import kr.it.pullit.modules.learningsource.source.event.SourceExtractionCompleteEvent;
import kr.it.pullit.modules.learningsource.source.event.SourceExtractionFailureEvent;
import kr.it.pullit.modules.learningsource.source.event.SourceExtractionStartEvent;
import kr.it.pullit.modules.learningsource.source.repository.SourceRepository;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SourceStatusEventHandler - 소스 상태 이벤트 핸들러")
class SourceStatusEventHandlerTest {

  @Mock private SourceRepository sourceRepository;
  @InjectMocks private SourceStatusEventHandler eventHandler;

  private Source createSource(SourceStatus status) {
    SourceFolder folder = SourceFolder.create(1L, "폴더", null, "#fff");
    SourceCreationParam param =
        new SourceCreationParam(1L, "소스.pdf", "learning-sources/test.pdf", "application/pdf", 128L);
    Source source = Source.create(param, 1L, folder);
    switch (status) {
      case PROCESSING -> source.startProcessing();
      case READY -> source.markAsReady();
      case FAILED -> source.markAsFailed();
      default -> {}
    }
    return source;
  }

  @Nested
  @DisplayName("handleSourceExtractionStart")
  class HandleSourceExtractionStart {

    @Test
    @DisplayName("성공 - 이벤트가 수신되면 상태를 PROCESSING으로 변경한다")
    void markProcessingWhenSourceExists() {
      Source source = createSource(SourceStatus.UPLOADED);
      when(sourceRepository.findById(eq(99L))).thenReturn(Optional.of(source));

      eventHandler.handleSourceExtractionStart(new SourceExtractionStartEvent(99L));

      assertThat(source.getStatus()).isEqualTo(SourceStatus.PROCESSING);
      verify(sourceRepository).findById(99L);
      verifyNoMoreInteractions(sourceRepository);
    }

    @Test
    @DisplayName("성공 - 존재하지 않는 소스 ID라도 예외 없이 처리한다")
    void ignoreWhenSourceMissing() {
      when(sourceRepository.findById(eq(1L))).thenReturn(Optional.empty());

      eventHandler.handleSourceExtractionStart(new SourceExtractionStartEvent(1L));

      verify(sourceRepository).findById(1L);
      verifyNoMoreInteractions(sourceRepository);
    }
  }

  @Nested
  @DisplayName("handleSourceExtractionComplete")
  class HandleSourceExtractionComplete {

    @Test
    @DisplayName("성공 - 상태를 READY로 변경한다")
    void markReady() {
      Source source = createSource(SourceStatus.PROCESSING);
      when(sourceRepository.findById(eq(5L))).thenReturn(Optional.of(source));

      eventHandler.handleSourceExtractionComplete(new SourceExtractionCompleteEvent(5L, null));

      assertThat(source.getStatus()).isEqualTo(SourceStatus.READY);
      verify(sourceRepository).findById(5L);
      verifyNoMoreInteractions(sourceRepository);
    }
  }

  @Nested
  @DisplayName("handleSourceExtractionFailure")
  class HandleSourceExtractionFailure {

    @Test
    @DisplayName("성공 - 상태를 FAILED로 변경한다")
    void markFailed() {
      Source source = createSource(SourceStatus.PROCESSING);
      when(sourceRepository.findById(eq(7L))).thenReturn(Optional.of(source));
      RuntimeException cause = new RuntimeException("boom");

      eventHandler.handleSourceExtractionFailure(new SourceExtractionFailureEvent(7L, cause));

      assertThat(source.getStatus()).isEqualTo(SourceStatus.FAILED);
      verify(sourceRepository).findById(7L);
      verifyNoMoreInteractions(sourceRepository);
    }

    @Test
    @DisplayName("성공 - 소스를 찾지 못해도 추가 작업 없이 종료한다")
    void ignoreMissingSourceOnFailure() {
      when(sourceRepository.findById(eq(11L))).thenReturn(Optional.empty());

      eventHandler.handleSourceExtractionFailure(new SourceExtractionFailureEvent(11L, new RuntimeException("boom")));

      verify(sourceRepository).findById(11L);
      verifyNoMoreInteractions(sourceRepository);
    }
  }
}
