package kr.it.pullit.modules.learningsource.source.exception;

import static org.assertj.core.api.Assertions.assertThat;

import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@MockitoUnitTest
@DisplayName("SourceExceptions - 예외 유틸리티")
class SourceExceptionTest {

  @Test
  @DisplayName("SourceNotFoundException.byId - 메시지와 에러 코드를 생성한다")
  void sourceNotFoundById() {
    SourceNotFoundException exception = SourceNotFoundException.byId(42L);

    assertThat(exception.getErrorCode()).isEqualTo(SourceErrorCode.SOURCE_NOT_FOUND);
    assertThat(exception).hasMessage("소스를 찾을 수 없습니다. (ID: 42)");
  }

  @Test
  @DisplayName("SourceNotFoundException.withMessage - 커스텀 메시지를 포함한다")
  void sourceNotFoundWithMessage() {
    SourceNotFoundException exception = SourceNotFoundException.withMessage("조건");

    assertThat(exception.getErrorCode()).isEqualTo(SourceErrorCode.SOURCE_NOT_FOUND);
    assertThat(exception).hasMessage("소스를 찾을 수 없습니다. (조건: 조건)");
  }

  @Test
  @DisplayName("SourceAccessDeniedException.byMember - 포맷된 메시지를 반환한다")
  void sourceAccessDeniedByMember() {
    SourceAccessDeniedException exception = SourceAccessDeniedException.byMember(7L);

    assertThat(exception.getErrorCode()).isEqualTo(SourceErrorCode.SOURCE_FORBIDDEN);
    assertThat(exception).hasMessage("사용자 7는 해당 소스를 삭제할 권한이 없습니다.");
  }
}
