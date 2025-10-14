package kr.it.pullit.modules.learningsource.source.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.domain.entity.SourceCreationParam;
import kr.it.pullit.modules.learningsource.source.exception.SourceNotFoundException;
import kr.it.pullit.modules.learningsource.source.repository.SourceRepository;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.platform.storage.api.S3PublicApi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SourceService - 학습 소스 서비스 테스트")
class SourceServiceTest {

  @InjectMocks private SourceService sourceService;
  @Mock private SourceRepository sourceRepository;
  @Mock private S3PublicApi s3PublicApi;

  @Test
  @DisplayName("성공 - 존재하는 소스 ID로 요청 시 파일 내용을 byte 배열로 반환한다")
  void getContentBytesSuccess() {
    // given
    final Long memberId = 1L;
    final Long sourceId = 1L;
    final String filePath = "path/to/file.pdf";
    final InputStream expectedContent =
        new ByteArrayInputStream("test file content".getBytes()); // stream으로
    // 반
    // 환

    final Member mockMember = Member.create(1L, "test@example.com", "테스트사용자");
    final SourceFolder mockSourceFolder =
        SourceFolder.builder().member(mockMember).name("default").color("#000000").build();
    final SourceCreationParam creationParam =
        new SourceCreationParam(memberId, "test.pdf", filePath, "application/pdf", 1024L);

    Source mockSource = Source.create(creationParam, mockMember, mockSourceFolder);

    given(sourceRepository.findByIdAndMemberId(sourceId, memberId))
        .willReturn(Optional.of(mockSource));
    given(s3PublicApi.downloadFileAsStream(filePath)).willReturn(expectedContent);
    // when
    InputStream actualContent = sourceService.getContentStream(sourceId, memberId);
    // then
    assertThat(actualContent).isEqualTo(expectedContent);
    verify(sourceRepository).findByIdAndMemberId(sourceId, memberId);
    verify(s3PublicApi).downloadFileAsStream(filePath);
  }

  @Test
  @DisplayName("실패 - 존재하지 않는 소스 ID로 요청 시 예외가 발생한다")
  void getContentBytesFailSourceNotFound() {
    // given
    final Long memberId = 1L;
    final Long nonExistentSourceId = 999L;

    given(sourceRepository.findByIdAndMemberId(nonExistentSourceId, memberId))
        .willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> sourceService.getContentStream(nonExistentSourceId, memberId))
        .isInstanceOf(SourceNotFoundException.class)
        .hasMessageContaining("소스를 찾을 수 없습니다.");
  }
}
