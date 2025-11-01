package kr.it.pullit.modules.learningsource.source.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.domain.entity.SourceCreationParam;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@MockitoUnitTest
class SourceResponseTest {

  @Test
  @DisplayName("폴더 정보가 없으면 응답의 폴더명이 null이다")
  void fromReturnsNullFolderNameWhenSourceHasNoFolder() {
    Source source =
        Source.create(
            new SourceCreationParam(
                1L, "sample.pdf", "learning-sources/sample.pdf", "application/pdf", 2048L),
            1L,
            null);

    SourceResponse response = SourceResponse.from(source);

    assertThat(response.sourceFolderName()).isNull();
  }

  @Test
  @DisplayName("폴더가 있으면 응답에 폴더명이 채워진다")
  void fromReturnsFolderNameWhenSourceHasFolder() {
    SourceFolder folder = SourceFolder.createDefaultFolder(2L);
    Source source =
        Source.create(
            new SourceCreationParam(
                2L, "other.pdf", "learning-sources/other.pdf", "application/pdf", 1024L),
            2L,
            folder);

    SourceResponse response = SourceResponse.from(source);

    assertThat(response.sourceFolderName()).isEqualTo(folder.getName());
  }
}
