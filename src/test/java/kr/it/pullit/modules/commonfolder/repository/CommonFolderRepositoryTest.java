package kr.it.pullit.modules.commonfolder.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import kr.it.pullit.modules.commonfolder.domain.enums.FolderScope;
import kr.it.pullit.support.annotation.JpaSliceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@JpaSliceTest
@DisplayName("CommonFolderRepository 슬라이스 테스트")
class CommonFolderRepositoryTest {

  @Autowired private CommonFolderRepository commonFolderRepository;

  @Nested
  @DisplayName("폴더 목록 조회")
  class DescribeFindByTypeOrderBySortOrderAsc {

    @Test
    @DisplayName("타입으로 조회하면 정렬 순서대로 반환한다")
    void findByTypeOrderBySortOrderAsc() {
      CommonFolderType type = CommonFolderType.QUESTION_SET;
      Long ownerId = 1L;
      commonFolderRepository.save(CommonFolder.create("B", type, FolderScope.CUSTOM, 2, ownerId));
      commonFolderRepository.save(CommonFolder.create("A", type, FolderScope.CUSTOM, 0, ownerId));
      commonFolderRepository.save(CommonFolder.create("C", type, FolderScope.CUSTOM, 1, ownerId));

      List<CommonFolder> folders =
          commonFolderRepository.findByOwnerIdAndTypeOrderBySortOrderAsc(ownerId, type);

      assertThat(folders).hasSize(3);
      assertThat(folders).extracting(CommonFolder::getSortOrder).containsExactly(0, 1, 2);
    }
  }

  @Nested
  @DisplayName("정렬 순서 계산")
  class DescribeFindFirstByTypeOrderBySortOrderDesc {

    @Test
    @DisplayName("가장 큰 정렬 순서를 가진 폴더를 반환한다")
    void findFirstByTypeOrderBySortOrderDesc() {
      CommonFolderType type = CommonFolderType.QUESTION_SET;
      Long ownerId = 1L;
      commonFolderRepository.save(
          CommonFolder.create("lowest", type, FolderScope.CUSTOM, 0, ownerId));
      CommonFolder highest =
          commonFolderRepository.save(
              CommonFolder.create("highest", type, FolderScope.CUSTOM, 5, ownerId));

      Optional<CommonFolder> found =
          commonFolderRepository.findFirstByOwnerIdAndTypeOrderBySortOrderDesc(ownerId, type);

      assertThat(found).isPresent();
      assertThat(found.get().getId()).isEqualTo(highest.getId());
    }
  }

  @Nested
  @DisplayName("이름으로 조회")
  class DescribeFindByNameAndType {

    @Test
    @DisplayName("이름과 타입이 일치하면 폴더를 반환한다")
    void findByNameAndType() {
      CommonFolderType type = CommonFolderType.QUESTION_SET;
      Long ownerId = 1L;
      CommonFolder saved =
          commonFolderRepository.save(
              CommonFolder.create("target", type, FolderScope.CUSTOM, 0, ownerId));

      Optional<CommonFolder> found =
          commonFolderRepository.findByNameAndOwnerIdAndType("target", ownerId, type);

      assertThat(found).isPresent();
      assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("일치하는 폴더가 없으면 비어 있는 Optional을 반환한다")
    void findByNameAndType_notFound() {
      Optional<CommonFolder> found =
          commonFolderRepository.findByNameAndOwnerIdAndType(
              "missing", 1L, CommonFolderType.QUESTION_SET);

      assertThat(found).isEmpty();
    }
  }

  @Nested
  @DisplayName("scope으로 조회")
  class DescribeFindByOwnerIdAndTypeAndScope {
    @Test
    @DisplayName("소유자, 타입, scope이 일치하는 폴더를 반환한다")
    void findByOwnerIdAndTypeAndScope() {
      Long ownerId = 1L;
      commonFolderRepository.save(
          CommonFolder.create("전체", CommonFolderType.QUESTION_SET, FolderScope.ALL, 0, ownerId));
      commonFolderRepository.save(
          CommonFolder.create(
              "사용자정의", CommonFolderType.QUESTION_SET, FolderScope.CUSTOM, 1, ownerId));
      commonFolderRepository.save(
          CommonFolder.create("전체", CommonFolderType.WRONG_ANSWER, FolderScope.ALL, 0, ownerId));

      Optional<CommonFolder> found =
          commonFolderRepository.findByOwnerIdAndTypeAndScope(
              ownerId, CommonFolderType.QUESTION_SET, FolderScope.ALL);

      assertThat(found).isPresent();
      assertThat(found.get().getName()).isEqualTo("전체");
      assertThat(found.get().getType()).isEqualTo(CommonFolderType.QUESTION_SET);
      assertThat(found.get().getScope()).isEqualTo(FolderScope.ALL);
    }
  }
}
