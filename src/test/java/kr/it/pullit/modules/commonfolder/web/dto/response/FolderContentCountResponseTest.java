package kr.it.pullit.modules.commonfolder.web.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FolderContentCountResponseTest {

  @Test
  @DisplayName("questionSetCount 값을 반환한다")
  void questionSetCount() {
    FolderContentCountResponse response = new FolderContentCountResponse(5L);

    assertThat(response.questionSetCount()).isEqualTo(5L);
  }
}
