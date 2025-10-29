package kr.it.pullit.modules.learningsource.source.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.learningsource.source.constant.SourceStatus;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.domain.entity.SourceCreationParam;
import kr.it.pullit.modules.learningsource.source.exception.SourceNotFoundException;
import kr.it.pullit.modules.learningsource.source.repository.SourceRepository;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadCompleteRequest;
import kr.it.pullit.modules.learningsource.sourcefolder.api.SourceFolderPublicApi;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.exception.MemberNotFoundException;
import kr.it.pullit.platform.storage.api.S3PublicApi;
import kr.it.pullit.platform.storage.s3.dto.PresignedUrlResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("SourceService - 학습 소스 서비스 테스트")
class SourceServiceTest {

  @InjectMocks private SourceService sourceService;
  @Mock private SourceRepository sourceRepository;
  @Mock private S3PublicApi s3PublicApi;
  @Mock private MemberPublicApi memberPublicApi;
  @Mock private SourceFolderPublicApi sourceFolderPublicApi;
  @Mock private ApplicationEventPublisher applicationEventPublisher;

  private Source createSource(Long memberId, String filePath, SourceStatus status) {
    SourceFolder folder = SourceFolder.create(memberId, "폴더", null, "#000000");
    SourceCreationParam param =
        new SourceCreationParam(memberId, "test.pdf", filePath, "application/pdf", 1024L);
    Source source = Source.create(param, memberId, folder);
    if (status == SourceStatus.READY) {
      source.markAsReady();
    } else if (status == SourceStatus.PROCESSING) {
      source.startProcessing();
    } else if (status == SourceStatus.FAILED) {
      source.markAsFailed();
    }
    return source;
  }

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

    final Long memberIdForMock = 1L; // assuming memberId 1 is used for mock creation
    final SourceFolder mockSourceFolder =
        SourceFolder.create(memberIdForMock, "default", null, "#000000");
    final SourceCreationParam creationParam =
        new SourceCreationParam(memberId, "test.pdf", filePath, "application/pdf", 1024L);

    Source mockSource = Source.create(creationParam, memberIdForMock, mockSourceFolder);

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

  @Test
  @DisplayName("성공 - Presigned URL 요청 시 S3 서비스 응답을 그대로 반환한다")
  void generateUploadUrlSuccess() {
    Long memberId = 7L;
    given(s3PublicApi.generateUploadUrl("file.pdf", "application/pdf", 2048L, memberId))
        .willReturn(new PresignedUrlResponse("https://upload", "learning-sources/file.pdf"));

    var response = sourceService.generateUploadUrl("file.pdf", "application/pdf", 2048L, memberId);

    assertThat(response.getUploadUrl()).isEqualTo("https://upload");
    assertThat(response.getFilePath()).isEqualTo("learning-sources/file.pdf");
    assertThat(response.getOriginalName()).isEqualTo("file.pdf");
    assertThat(response.getFileSizeBytes()).isEqualTo(2048L);
    assertThat(response.getContentType()).isEqualTo("application/pdf");
    verify(s3PublicApi).generateUploadUrl("file.pdf", "application/pdf", 2048L, memberId);
  }

  @Test
  @DisplayName("성공 - 업로드 완료 시 신규 소스를 생성하고 READY 상태로 변경한다")
  void processUploadCompleteCreate() {
    Long memberId = 9L;
    SourceUploadCompleteRequest request =
        new SourceUploadCompleteRequest(
            "upload-1", "learning-sources/new.pdf", "new.pdf", "application/pdf", 4096L);

    given(s3PublicApi.fileExists(request.getFilePath())).willReturn(true);
    given(sourceRepository.findByMemberIdAndFilePath(memberId, request.getFilePath()))
        .willReturn(Optional.empty());
    given(memberPublicApi.findById(memberId))
        .willReturn(
            Optional.of(
                org.mockito.Mockito.mock(kr.it.pullit.modules.member.domain.entity.Member.class)));
    SourceFolder folder = SourceFolder.createDefaultFolder(memberId);
    given(sourceFolderPublicApi.findOrCreateDefaultFolder(memberId)).willReturn(folder);
    given(sourceRepository.save(any(Source.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    sourceService.processUploadComplete(request, memberId);

    ArgumentCaptor<Source> captor = ArgumentCaptor.forClass(Source.class);
    verify(sourceRepository).save(captor.capture());
    Source saved = captor.getValue();
    assertThat(saved.getOriginalName()).isEqualTo("new.pdf");
    assertThat(saved.getFileSizeBytes()).isEqualTo(4096L);
    assertThat(saved.getStatus()).isEqualTo(SourceStatus.READY);
    verify(memberPublicApi).findById(memberId);
    verify(sourceFolderPublicApi).findOrCreateDefaultFolder(memberId);
  }

  @Test
  @DisplayName("성공 - 기존 소스가 있으면 파일 정보를 갱신하고 READY 상태로 만든다")
  void processUploadCompleteUpdate() {
    Long memberId = 11L;
    Source existing = createSource(memberId, "learning-sources/exist.pdf", SourceStatus.UPLOADED);
    SourceUploadCompleteRequest request =
        new SourceUploadCompleteRequest(
            "upload-2", "learning-sources/exist.pdf", "updated.pdf", "application/pdf", 8192L);

    given(s3PublicApi.fileExists(request.getFilePath())).willReturn(true);
    given(sourceRepository.findByMemberIdAndFilePath(memberId, request.getFilePath()))
        .willReturn(Optional.of(existing));
    given(sourceRepository.save(existing)).willReturn(existing);

    sourceService.processUploadComplete(request, memberId);

    assertThat(existing.getOriginalName()).isEqualTo("updated.pdf");
    assertThat(existing.getFileSizeBytes()).isEqualTo(8192L);
    assertThat(existing.getStatus()).isEqualTo(SourceStatus.READY);
    verify(memberPublicApi, never()).findById(any());
    verify(sourceFolderPublicApi, never()).findOrCreateDefaultFolder(any());
  }

  @Test
  @DisplayName("실패 - S3에 파일이 없으면 업로드 완료 처리 시 예외가 발생한다")
  void processUploadCompleteMissingFile() {
    Long memberId = 13L;
    SourceUploadCompleteRequest request =
        new SourceUploadCompleteRequest(
            "upload-3", "learning-sources/missing.pdf", "missing.pdf", "application/pdf", 1024L);

    given(s3PublicApi.fileExists(request.getFilePath())).willReturn(false);

    assertThatThrownBy(() -> sourceService.processUploadComplete(request, memberId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("S3에 해당 파일이 존재하지 않습니다.");

    verifyNoInteractions(memberPublicApi, sourceFolderPublicApi);
  }

  @Test
  @DisplayName("실패 - 회원이 존재하지 않으면 업로드 완료 처리 시 예외가 발생한다")
  void processUploadCompleteWhenMemberMissing() {
    Long memberId = 14L;
    SourceUploadCompleteRequest request =
        new SourceUploadCompleteRequest(
            "upload-4",
            "learning-sources/missing-member.pdf",
            "missing.pdf",
            "application/pdf",
            512L);

    given(s3PublicApi.fileExists(request.getFilePath())).willReturn(true);
    given(sourceRepository.findByMemberIdAndFilePath(memberId, request.getFilePath()))
        .willReturn(Optional.empty());
    given(memberPublicApi.findById(memberId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> sourceService.processUploadComplete(request, memberId))
        .isInstanceOf(MemberNotFoundException.class)
        .hasMessageContaining("회원");

    verify(memberPublicApi).findById(memberId);
    verifyNoInteractions(sourceFolderPublicApi);
    verify(sourceRepository, never()).save(any());
  }

  @Test
  @DisplayName("성공 - 내 학습 소스 목록 조회 시 DTO로 매핑한다")
  void getMySourcesSuccess() {
    Long memberId = 15L;
    Source source1 = createSource(memberId, "learning-sources/one.pdf", SourceStatus.READY);
    Source source2 = createSource(memberId, "learning-sources/two.pdf", SourceStatus.UPLOADED);
    given(sourceRepository.findSourcesByMemberIdWithDetails(memberId))
        .willReturn(List.of(source1, source2));

    var responses = sourceService.getMySources(memberId);

    assertThat(responses).hasSize(2);
    assertThat(responses.get(0).id()).isNull();
    assertThat(responses.get(0).originalName()).isEqualTo("test.pdf");
    verify(sourceRepository).findSourcesByMemberIdWithDetails(memberId);
  }

  @Test
  @DisplayName("성공 - 소스 ID로 조회한다")
  void findById() {
    Source source = createSource(17L, "learning-sources/find.pdf", SourceStatus.READY);
    given(sourceRepository.findById(1L)).willReturn(Optional.of(source));

    assertThat(sourceService.findById(1L)).contains(source);
  }

  @Test
  @DisplayName("성공 - 소스 ID와 회원 ID로 조회한다")
  void findByIdAndMemberId() {
    Source source = createSource(19L, "learning-sources/owner.pdf", SourceStatus.READY);
    given(sourceRepository.findByIdAndMemberId(5L, 5L)).willReturn(Optional.of(source));

    assertThat(sourceService.findByIdAndMemberId(5L)).contains(source);
    verify(sourceRepository).findByIdAndMemberId(5L, 5L);
  }

  @Test
  @DisplayName("성공 - 여러 ID로 소스를 조회한다")
  void findByIdIn() {
    Source source = createSource(21L, "learning-sources/list.pdf", SourceStatus.READY);
    given(sourceRepository.findByIdIn(List.of(1L, 2L))).willReturn(List.of(source));

    assertThat(sourceService.findByIdIn(List.of(1L, 2L))).containsExactly(source);
  }

  @Test
  @DisplayName("성공 - 소스를 삭제하면 저장소와 S3에서 함께 제거된다")
  void deleteSource() {
    Long memberId = 23L;
    Source source = createSource(memberId, "learning-sources/delete.pdf", SourceStatus.READY);
    given(sourceRepository.findByIdAndMemberId(7L, memberId)).willReturn(Optional.of(source));

    sourceService.deleteSource(7L, memberId);

    verify(sourceRepository).delete(source);
    verify(s3PublicApi).deleteFile("learning-sources/delete.pdf");
  }

  @Test
  @DisplayName("성공 - S3 삭제가 실패해도 예외를 전파하지 않는다")
  void deleteSourceWhenS3Fails() {
    Long memberId = 25L;
    Source source = createSource(memberId, "learning-sources/fail.pdf", SourceStatus.READY);
    given(sourceRepository.findByIdAndMemberId(8L, memberId)).willReturn(Optional.of(source));
    doThrow(new RuntimeException("boom")).when(s3PublicApi).deleteFile("learning-sources/fail.pdf");

    sourceService.deleteSource(8L, memberId);

    verify(sourceRepository).delete(source);
    verify(s3PublicApi).deleteFile("learning-sources/fail.pdf");
  }

  @Test
  @DisplayName("성공 - UPLOADED 상태의 소스만 READY로 마이그레이션한다")
  void migrateUploadedSourcesToReady() {
    Source needsMigration =
        createSource(27L, "learning-sources/migrate.pdf", SourceStatus.UPLOADED);
    Source staysUploaded = createSource(27L, "learning-sources/skip.pdf", SourceStatus.UPLOADED);
    given(sourceRepository.findByStatus(SourceStatus.UPLOADED))
        .willReturn(List.of(needsMigration, staysUploaded));
    given(s3PublicApi.fileExists("learning-sources/migrate.pdf")).willReturn(true);
    given(s3PublicApi.fileExists("learning-sources/skip.pdf")).willReturn(false);

    sourceService.migrateUploadedSourcesToReady();

    assertThat(needsMigration.getStatus()).isEqualTo(SourceStatus.READY);
    assertThat(staysUploaded.getStatus()).isEqualTo(SourceStatus.UPLOADED);
    verify(sourceRepository).findByStatus(SourceStatus.UPLOADED);
    verify(sourceRepository).save(needsMigration);
    verify(sourceRepository, never()).save(staysUploaded);
    verifyNoMoreInteractions(sourceRepository);
  }

  @Test
  @DisplayName("성공 - 마이그레이션 중 예외가 발생해도 다음 소스를 계속 처리한다")
  void migrateUploadedSourcesContinuesWhenS3LookupFails() {
    Source problematic =
        createSource(31L, "learning-sources/problematic.pdf", SourceStatus.UPLOADED);
    given(sourceRepository.findByStatus(SourceStatus.UPLOADED)).willReturn(List.of(problematic));
    given(s3PublicApi.fileExists("learning-sources/problematic.pdf"))
        .willThrow(new RuntimeException("s3 down"));

    sourceService.migrateUploadedSourcesToReady();

    assertThat(problematic.getStatus()).isEqualTo(SourceStatus.UPLOADED);
    verify(sourceRepository).findByStatus(SourceStatus.UPLOADED);
    verify(sourceRepository, never()).save(any());
  }
}
