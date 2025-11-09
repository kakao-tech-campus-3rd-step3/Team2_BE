package kr.it.pullit.modules.questionset.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.constant.SourceStatus;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.domain.entity.SourceCreationParam;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.modules.questionset.exception.SourceNotExistOnS3Exception;
import kr.it.pullit.modules.questionset.exception.SourceNotReadyException;
import kr.it.pullit.modules.questionset.exception.SourceProcessingFailedException;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

@MockitoUnitTest
@DisplayName("SourceValidator 단위 테스트")
class SourceValidatorTest {

  @Mock private SourcePublicApi sourcePublicApi;

  private SourceValidator sourceValidator;

  @BeforeEach
  void setUp() {
    sourceValidator = new SourceValidator(sourcePublicApi);
  }

  @Test
  @DisplayName("소스 ID가 없으면 검증을 수행하지 않는다")
  void skipsValidationWhenIdsEmpty() {
    sourceValidator.validateSourcesAreReady(List.of(), 1L);

    verify(sourcePublicApi, never()).findByIdIn(anyList());
  }

  @Test
  @DisplayName("모든 소스가 준비되어 있으면 통과한다")
  void passesWhenAllSourcesReady() {
    Source readySource = createSourceWithStatus(1L, SourceStatus.READY);
    when(sourcePublicApi.findByIdIn(List.of(1L))).thenReturn(List.of(readySource));

    assertThatCode(() -> sourceValidator.validateSourcesAreReady(List.of(1L), 10L))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("일부 소스 상태를 찾지 못해도 준비된 소스만 있으면 통과한다")
  void passesWhenDatabaseMissingEntries() {
    Source readySource = createSourceWithStatus(1L, SourceStatus.READY);
    when(sourcePublicApi.findByIdIn(List.of(1L, 2L))).thenReturn(List.of(readySource));

    assertThatCode(() -> sourceValidator.validateSourcesAreReady(List.of(1L, 2L), 11L))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("NOT_EXIST 상태의 소스가 있으면 SourceNotExistOnS3Exception 예외를 던진다")
  void throwsSourceNotExistException_whenSourceStatusIsNotExist() {
    Source notExistSource = createSourceWithStatus(2L, SourceStatus.NOT_EXIST);
    when(sourcePublicApi.findByIdIn(List.of(2L))).thenReturn(List.of(notExistSource));

    assertThatThrownBy(() -> sourceValidator.validateSourcesAreReady(List.of(2L), 12L))
        .isInstanceOf(SourceNotExistOnS3Exception.class);
  }

  @Test
  @DisplayName("FAILED 상태의 소스가 있으면 SourceProcessingFailedException 예외를 던진다")
  void throwsSourceProcessingFailedException_whenSourceStatusIsFailed() {
    Source failedSource = createSourceWithStatus(3L, SourceStatus.FAILED);
    when(sourcePublicApi.findByIdIn(List.of(3L))).thenReturn(List.of(failedSource));

    assertThatThrownBy(() -> sourceValidator.validateSourcesAreReady(List.of(3L), 13L))
        .isInstanceOf(SourceProcessingFailedException.class);
  }

  @Test
  @DisplayName("PROCESSING 상태의 소스가 있으면 SourceNotReadyException 예외를 던진다")
  void throwsSourceNotReadyException_whenSourceStatusIsProcessing() {
    Source processingSource = createSourceWithStatus(4L, SourceStatus.PROCESSING);
    when(sourcePublicApi.findByIdIn(List.of(4L))).thenReturn(List.of(processingSource));

    assertThatThrownBy(() -> sourceValidator.validateSourcesAreReady(List.of(4L), 14L))
        .isInstanceOf(SourceNotReadyException.class);
  }

  @Test
  @DisplayName("NOT_EXIST와 FAILED 상태가 섞여있으면 SourceNotExistOnS3Exception을 우선적으로 던진다")
  void throwsSourceNotExistException_whenNotExistAndFailedSourcesAreMixed() {
    Source notExistSource = createSourceWithStatus(5L, SourceStatus.NOT_EXIST);
    Source failedSource = createSourceWithStatus(6L, SourceStatus.FAILED);
    when(sourcePublicApi.findByIdIn(List.of(5L, 6L)))
        .thenReturn(List.of(notExistSource, failedSource));

    assertThatThrownBy(() -> sourceValidator.validateSourcesAreReady(List.of(5L, 6L), 15L))
        .isInstanceOf(SourceNotExistOnS3Exception.class);
  }

  @Test
  @DisplayName("FAILED와 PROCESSING 상태가 섞여있으면 SourceProcessingFailedException을 우선적으로 던진다")
  void throwsSourceProcessingFailedException_whenFailedAndProcessingSourcesAreMixed() {
    Source failedSource = createSourceWithStatus(7L, SourceStatus.FAILED);
    Source processingSource = createSourceWithStatus(8L, SourceStatus.PROCESSING);
    when(sourcePublicApi.findByIdIn(List.of(7L, 8L)))
        .thenReturn(List.of(failedSource, processingSource));

    assertThatThrownBy(() -> sourceValidator.validateSourcesAreReady(List.of(7L, 8L), 16L))
        .isInstanceOf(SourceProcessingFailedException.class);
  }

  private Source createSourceWithStatus(Long id, SourceStatus status) {
    SourceFolder folder = SourceFolder.createDefaultFolder(1L);
    SourceCreationParam param = new SourceCreationParam(1L, "자료.pdf", "path", "type", 10L);
    Source source = Source.create(param, 1L, folder);
    ReflectionTestUtils.setField(source, "id", id);
    ReflectionTestUtils.setField(source, "status", status);
    return source;
  }
}
