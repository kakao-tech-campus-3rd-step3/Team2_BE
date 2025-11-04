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
import kr.it.pullit.modules.questionset.exception.SourceNotReadyException;
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
    Source readySource = readySource(1L);
    when(sourcePublicApi.findByIdIn(List.of(1L))).thenReturn(List.of(readySource));

    assertThatCode(() -> sourceValidator.validateSourcesAreReady(List.of(1L), 10L))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("일부 소스 상태를 찾지 못해도 준비된 소스만 있으면 통과한다")
  void passesWhenDatabaseMissingEntries() {
    Source readySource = readySource(1L);
    when(sourcePublicApi.findByIdIn(List.of(1L, 2L))).thenReturn(List.of(readySource));

    assertThatCode(() -> sourceValidator.validateSourcesAreReady(List.of(1L, 2L), 11L))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("준비되지 않은 소스가 있으면 예외를 던진다")
  void throwsWhenNotReady() {
    Source notReady = readySource(2L);
    ReflectionTestUtils.setField(notReady, "status", SourceStatus.FAILED);

    when(sourcePublicApi.findByIdIn(List.of(2L))).thenReturn(List.of(notReady));

    assertThatThrownBy(() -> sourceValidator.validateSourcesAreReady(List.of(2L), 12L))
        .isInstanceOf(SourceNotReadyException.class);
  }

  private Source readySource(Long id) {
    SourceFolder folder = SourceFolder.createDefaultFolder(1L);
    SourceCreationParam param = new SourceCreationParam(1L, "자료.pdf", "path", "type", 10L);
    Source source = Source.create(param, 1L, folder);
    source.markAsReady();
    ReflectionTestUtils.setField(source, "id", id);
    return source;
  }
}
