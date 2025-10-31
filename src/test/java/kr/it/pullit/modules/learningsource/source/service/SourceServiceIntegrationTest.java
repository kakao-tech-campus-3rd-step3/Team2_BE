package kr.it.pullit.modules.learningsource.source.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.learningsource.source.constant.SourceStatus;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.domain.entity.SourceCreationParam;
import kr.it.pullit.modules.learningsource.source.repository.SourceRepository;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadCompleteRequest;
import kr.it.pullit.modules.learningsource.sourcefolder.api.SourceFolderPublicApi;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.platform.storage.api.S3PublicApi;
import kr.it.pullit.support.annotation.IntegrationTest;
import kr.it.pullit.support.fixture.MemberFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@IntegrationTest
@DisplayName("SourceService 통합 테스트")
class SourceServiceIntegrationTest {

  @Autowired private SourceService sourceService;

  @Autowired private SourceRepository sourceRepository;

  @PersistenceContext private EntityManager entityManager;

  @MockitoBean private S3PublicApi s3PublicApi;

  @MockitoBean private MemberPublicApi memberPublicApi;

  @MockitoBean private SourceFolderPublicApi sourceFolderPublicApi;

  private SourceFolder persistDefaultFolder(Long memberId) {
    SourceFolder folder = SourceFolder.createDefaultFolder(memberId);
    entityManager.persist(folder);
    return folder;
  }

  private Source persistSource(
      Long memberId, String filePath, SourceFolder folder, SourceStatus status) {
    SourceCreationParam param =
        new SourceCreationParam(memberId, "기존.pdf", filePath, "application/pdf", 2048L);
    Source source = Source.create(param, memberId, folder);

    if (status == SourceStatus.READY) {
      source.markAsReady();
    } else if (status == SourceStatus.FAILED) {
      source.markAsFailed();
    } else if (status == SourceStatus.PROCESSING) {
      source.startProcessing();
    }

    return sourceRepository.save(source);
  }

  private void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

  @Nested
  @DisplayName("업로드 완료 처리")
  class DescribeProcessUploadComplete {

    @Test
    @DisplayName("새로운 파일이면 소스를 생성하고 READY 상태로 변경한다")
    void createNewSourceWhenNotExists() {
      Long memberId = 101L;
      SourceFolder folder = persistDefaultFolder(memberId);
      SourceUploadCompleteRequest request =
          new SourceUploadCompleteRequest(
              "upload-1", "learning-sources/new.pdf", "new.pdf", "application/pdf", 4096L);
      Member member = MemberFixtures.basicUser();

      given(s3PublicApi.fileExists(request.getFilePath())).willReturn(true);
      given(memberPublicApi.findById(memberId)).willReturn(Optional.of(member));
      given(sourceFolderPublicApi.findOrCreateDefaultFolder(memberId)).willReturn(folder);

      sourceService.processUploadComplete(request, memberId);
      flushAndClear();

      List<Source> sources = sourceRepository.findByMemberIdOrderByCreatedAtDesc(memberId);

      assertThat(sources).hasSize(1);
      Source saved = sources.getFirst();
      assertThat(saved.getOriginalName()).isEqualTo("new.pdf");
      assertThat(saved.getFileSizeBytes()).isEqualTo(4096L);
      assertThat(saved.getStatus()).isEqualTo(SourceStatus.READY);

      then(memberPublicApi).should().findById(memberId);
      then(sourceFolderPublicApi).should().findOrCreateDefaultFolder(memberId);
    }

    @Test
    @DisplayName("이미 존재하는 파일이면 기존 소스를 갱신한다")
    void updateExistingSourceWhenAlreadyExists() {
      Long memberId = 102L;
      SourceFolder folder = persistDefaultFolder(memberId);
      // [Checkstyle] 'final' 키워드를 추가하여 경고 해결
      final Source existing =
          persistSource(memberId, "learning-sources/existing.pdf", folder, SourceStatus.UPLOADED);
      flushAndClear();

      SourceUploadCompleteRequest request =
          new SourceUploadCompleteRequest(
              "upload-2", "learning-sources/existing.pdf", "updated.pdf", "application/pdf", 8192L);

      given(s3PublicApi.fileExists(request.getFilePath())).willReturn(true);

      sourceService.processUploadComplete(request, memberId);
      flushAndClear();

      List<Source> sources = sourceRepository.findByMemberIdOrderByCreatedAtDesc(memberId);

      assertThat(sources).hasSize(1);
      Source saved = sources.getFirst();
      assertThat(saved.getId()).isEqualTo(existing.getId());
      assertThat(saved.getOriginalName()).isEqualTo("updated.pdf");
      assertThat(saved.getFileSizeBytes()).isEqualTo(8192L);
      assertThat(saved.getStatus()).isEqualTo(SourceStatus.READY);

      then(memberPublicApi).shouldHaveNoInteractions();
      then(sourceFolderPublicApi).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("S3에 파일이 없으면 예외를 발생시킨다")
    void throwWhenFileDoesNotExist() {
      Long memberId = 103L;
      SourceUploadCompleteRequest request =
          new SourceUploadCompleteRequest(
              "upload-3", "learning-sources/missing.pdf", "missing.pdf", "application/pdf", 1024L);

      given(s3PublicApi.fileExists(request.getFilePath())).willReturn(false);

      assertThatThrownBy(() -> sourceService.processUploadComplete(request, memberId))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("S3에 해당 파일이 존재하지 않습니다.");
    }
  }

  @Test
  @DisplayName("소스를 삭제하면 데이터와 S3 파일이 함께 정리된다")
  void deleteSourceAlsoDeletesFileInS3() {
    Long memberId = 104L;
    SourceFolder folder = persistDefaultFolder(memberId);
    Source source =
        persistSource(memberId, "learning-sources/delete.pdf", folder, SourceStatus.READY);
    flushAndClear();

    willDoNothing().given(s3PublicApi).deleteFile(source.getFilePath());

    sourceService.deleteSource(source.getId(), memberId);
    flushAndClear();

    Optional<Source> found = sourceRepository.findById(source.getId());

    assertThat(found).isEmpty();
    then(s3PublicApi).should().deleteFile(source.getFilePath());
  }

  @Test
  @DisplayName("UPLOADED 상태의 소스 중 S3 파일이 존재하면 READY로 마이그레이션한다")
  void migrateUploadedSourcesToReady() {
    Long memberId = 105L;
    SourceFolder folder = persistDefaultFolder(memberId);
    final Source shouldBeMigrated =
        persistSource(
            memberId, "learning-sources/migrate-ready.pdf", folder, SourceStatus.UPLOADED);
    final Source shouldStayUploaded =
        persistSource(memberId, "learning-sources/migrate-skip.pdf", folder, SourceStatus.UPLOADED);
    persistSource(memberId, "learning-sources/already-ready.pdf", folder, SourceStatus.READY);
    flushAndClear();

    given(s3PublicApi.fileExists("learning-sources/migrate-ready.pdf")).willReturn(true);
    given(s3PublicApi.fileExists("learning-sources/migrate-skip.pdf")).willReturn(false);

    sourceService.migrateUploadedSourcesToReady();
    flushAndClear();

    Source migrated = sourceRepository.findById(shouldBeMigrated.getId()).orElseThrow();
    Source skipped = sourceRepository.findById(shouldStayUploaded.getId()).orElseThrow();

    assertThat(migrated.getStatus()).isEqualTo(SourceStatus.READY);
    assertThat(skipped.getStatus()).isEqualTo(SourceStatus.UPLOADED);

    then(s3PublicApi).should().fileExists("learning-sources/migrate-ready.pdf");
    then(s3PublicApi).should().fileExists("learning-sources/migrate-skip.pdf");
  }
}
