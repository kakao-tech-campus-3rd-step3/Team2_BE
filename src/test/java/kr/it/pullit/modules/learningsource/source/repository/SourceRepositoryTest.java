package kr.it.pullit.modules.learningsource.source.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.learningsource.source.constant.SourceStatus;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.domain.entity.SourceCreationParam;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.support.annotation.JpaSliceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@JpaSliceTest
@Import(SourceRepositoryImpl.class)
@DisplayName("SourceRepository 슬라이스 테스트")
class SourceRepositoryTest {

  @Autowired private SourceRepository sourceRepository;

  @Autowired private EntityManager entityManager;

  private SourceFolder persistDefaultFolder(Long memberId) {
    SourceFolder folder = SourceFolder.createDefaultFolder(memberId);
    entityManager.persist(folder);
    return folder;
  }

  private Source saveSource(
      Long memberId, String filePath, SourceFolder folder, SourceStatus status) {
    SourceCreationParam param =
        new SourceCreationParam(memberId, "자료.pdf", filePath, "application/pdf", 1024L);

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
  @DisplayName("단건 조회")
  class DescribeFindOne {

    @Test
    @DisplayName("회원 ID와 파일 경로로 소스를 조회한다")
    void findByMemberIdAndFilePath() {
      Long memberId = 10L;
      SourceFolder folder = persistDefaultFolder(memberId);
      Source saved =
          saveSource(memberId, "learning-sources/path.pdf", folder, SourceStatus.UPLOADED);
      flushAndClear();

      Optional<Source> found =
          sourceRepository.findByMemberIdAndFilePath(memberId, "learning-sources/path.pdf");

      assertThat(found).isPresent();
      assertThat(found.get().getId()).isEqualTo(saved.getId());
      assertThat(found.get().getSourceFolder().getId()).isEqualTo(folder.getId());
    }

    @Test
    @DisplayName("다른 회원의 소스는 조회되지 않는다")
    void findByIdAndMemberIdWithDifferentMember() {
      Long ownerId = 20L;
      SourceFolder folder = persistDefaultFolder(ownerId);
      Source saved =
          saveSource(ownerId, "learning-sources/owner.pdf", folder, SourceStatus.UPLOADED);
      flushAndClear();

      Optional<Source> result = sourceRepository.findByIdAndMemberId(saved.getId(), 999L);

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("목록 조회")
  class DescribeFindList {

    @Test
    @DisplayName("폴더 정보를 포함한 내 소스 목록을 최신순으로 조회한다")
    void findSourcesByMemberIdWithDetails() {
      Long memberId = 30L;
      SourceFolder folder = persistDefaultFolder(memberId);
      saveSource(memberId, "learning-sources/a.pdf", folder, SourceStatus.READY);
      saveSource(memberId, "learning-sources/b.pdf", folder, SourceStatus.UPLOADED);
      flushAndClear();

      List<Source> sources = sourceRepository.findSourcesByMemberIdWithDetails(memberId);

      assertThat(sources).hasSize(2);
      assertThat(sources)
          .allSatisfy(
              source -> {
                assertThat(source.getMemberId()).isEqualTo(memberId);
                assertThat(source.getSourceFolder().getName()).isEqualTo(folder.getName());
              });
      assertThat(sources)
          .isSortedAccordingTo(
              (left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()));
    }

    @Test
    @DisplayName("상태별로 소스를 조회한다")
    void findByStatus() {
      Long memberId = 40L;
      SourceFolder folder = persistDefaultFolder(memberId);
      saveSource(memberId, "learning-sources/uploaded.pdf", folder, SourceStatus.UPLOADED);
      saveSource(memberId, "learning-sources/ready.pdf", folder, SourceStatus.READY);
      saveSource(memberId, "learning-sources/failed.pdf", folder, SourceStatus.FAILED);
      flushAndClear();

      List<Source> uploadedSources = sourceRepository.findByStatus(SourceStatus.UPLOADED);

      assertThat(uploadedSources)
          .hasSize(1)
          .first()
          .extracting(Source::getStatus)
          .isEqualTo(SourceStatus.UPLOADED);
    }

    @Test
    @DisplayName("여러 ID로 소스를 조회한다")
    void findByIdIn() {
      Long memberId = 50L;
      SourceFolder folder = persistDefaultFolder(memberId);
      Source first = saveSource(memberId, "learning-sources/first.pdf", folder, SourceStatus.READY);
      Source second =
          saveSource(memberId, "learning-sources/second.pdf", folder, SourceStatus.READY);
      flushAndClear();

      List<Source> found = sourceRepository.findByIdIn(List.of(first.getId(), second.getId()));

      assertThat(found)
          .extracting(Source::getId)
          .containsExactlyInAnyOrder(first.getId(), second.getId());
    }
  }
}
