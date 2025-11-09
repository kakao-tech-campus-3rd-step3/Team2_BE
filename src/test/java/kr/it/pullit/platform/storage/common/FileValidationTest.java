package kr.it.pullit.platform.storage.common;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import kr.it.pullit.support.annotation.SpringUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@SpringUnitTest
@DisplayName("FileValidation 단위 테스트")
class FileValidationTest {

  private final FileValidation fileValidation = new FileValidation();

  private static final String VALID_CONTENT_TYPE = "application/pdf";
  private static final long VALID_FILE_SIZE = 10 * 1024 * 1024; // 10MB

  @Test
  @DisplayName("유효한 PDF 파일은 예외를 던지지 않는다")
  void validatePdfFile_WithValidFile_DoesNotThrowException() {
    // when & then
    assertDoesNotThrow(() -> fileValidation.validatePdfFile(VALID_CONTENT_TYPE, VALID_FILE_SIZE));
  }

  @Nested
  @DisplayName("파일 크기 검증")
  class FileSizeValidationTest {

    @Test
    @DisplayName("파일 크기가 50MB를 초과하면 IllegalArgumentException을 던진다")
    void validatePdfFile_WhenFileSizeExceedsMax_ThrowsIllegalArgumentException() {
      // given
      long tooLargeFileSize = 50 * 1024 * 1024 + 1;

      // when & then
      assertThatThrownBy(() -> fileValidation.validatePdfFile(VALID_CONTENT_TYPE, tooLargeFileSize))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("파일 크기가 너무 큽니다. 최대 50MB까지 업로드 가능합니다.");
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    @DisplayName("파일 크기가 0 이하이면 IllegalArgumentException을 던진다")
    void validatePdfFile_WhenFileSizeIsZeroOrLess_ThrowsIllegalArgumentException(long invalidSize) {
      // when & then
      assertThatThrownBy(() -> fileValidation.validatePdfFile(VALID_CONTENT_TYPE, invalidSize))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("유효하지 않은 파일 크기입니다.");
    }
  }

  @Nested
  @DisplayName("콘텐츠 타입 검증")
  class ContentTypeValidationTest {

    @Test
    @DisplayName("콘텐츠 타입이 null이면 IllegalArgumentException을 던진다")
    void validatePdfFile_WhenContentTypeIsNull_ThrowsIllegalArgumentException() {
      // when & then
      assertThatThrownBy(() -> fileValidation.validatePdfFile(null, VALID_FILE_SIZE))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("파일 타입이 지정되지 않았습니다.");
    }

    @Test
    @DisplayName("허용되지 않은 콘텐츠 타입이면 IllegalArgumentException을 던진다")
    void validatePdfFile_WhenContentTypeIsInvalid_ThrowsIllegalArgumentException() {
      // given
      String invalidContentType = "image/png";

      // when & then
      assertThatThrownBy(() -> fileValidation.validatePdfFile(invalidContentType, VALID_FILE_SIZE))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("PDF 파일만 업로드 가능합니다.");
    }
  }
}
