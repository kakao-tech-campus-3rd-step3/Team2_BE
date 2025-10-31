package kr.it.pullit.modules.commonfolder.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommonFolderTest {

  @Test
  @DisplayName("정렬 순서를 변경하면 새로운 값이 반영된다")
  void updateSortOrder() {
    CommonFolder folder = CommonFolder.create("name", CommonFolderType.QUESTION_SET, 1, 1L);

    folder.updateSortOrder(7);

    assertThat(folder.getSortOrder()).isEqualTo(7);
  }
}
