package kr.it.pullit.modules.commonfolder.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
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
      commonFolderRepository.save(CommonFolder.create("B", type, 2));
      commonFolderRepository.save(CommonFolder.create("A", type, 0));
      commonFolderRepository.save(CommonFolder.create("C", type, 1));

      List<CommonFolder> folders = commonFolderRepository.findByTypeOrderBySortOrderAsc(type);

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
      commonFolderRepository.save(CommonFolder.create("lowest", type, 0));
      CommonFolder highest = commonFolderRepository.save(CommonFolder.create("highest", type, 5));

      Optional<CommonFolder> found =
          commonFolderRepository.findFirstByTypeOrderBySortOrderDesc(type);

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
      CommonFolder saved = commonFolderRepository.save(CommonFolder.create("target", type, 0));

      Optional<CommonFolder> found = commonFolderRepository.findByNameAndType("target", type);

      assertThat(found).isPresent();
      assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("일치하는 폴더가 없으면 비어 있는 Optional을 반환한다")
    void findByNameAndType_notFound() {
      Optional<CommonFolder> found =
          commonFolderRepository.findByNameAndType("missing", CommonFolderType.QUESTION_SET);

      assertThat(found).isEmpty();
    }
  }
}
