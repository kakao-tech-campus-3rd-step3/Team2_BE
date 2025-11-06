package kr.it.pullit.platform.security.jwt.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

@MockitoUnitTest
@DisplayName("HeaderHidingRequestWrapper 단위 테스트")
class HeaderHidingRequestWrapperTest {

  private static final String HEADER_TO_HIDE = "Authorization";
  private static final String VISIBLE_HEADER = "X-Visible-Header";

  @Mock private HttpServletRequest mockRequest;

  private HeaderHidingRequestWrapper wrapper;

  @BeforeEach
  void setUp() {
    wrapper = new HeaderHidingRequestWrapper(mockRequest, HEADER_TO_HIDE);
  }

  @DisplayName("지정된 헤더를 조회하면 null을 반환한다")
  @Test
  void getHeader_shouldReturnNullForHiddenHeader() {
    // then
    assertThat(wrapper.getHeader(HEADER_TO_HIDE)).isNull();
  }

  @DisplayName("지정되지 않은 헤더를 조회하면 원래 값을 반환한다")
  @Test
  void getHeader_shouldReturnOriginalValueForVisibleHeader() {
    // given
    when(mockRequest.getHeader(VISIBLE_HEADER)).thenReturn("visible_value");

    // then
    assertThat(wrapper.getHeader(VISIBLE_HEADER)).isEqualTo("visible_value");
  }

  @DisplayName("지정된 헤더의 목록을 조회하면 빈 Enumeration을 반환한다")
  @Test
  void getHeaders_shouldReturnEmptyEnumerationForHiddenHeader() {
    // then
    assertThat(wrapper.getHeaders(HEADER_TO_HIDE).hasMoreElements()).isFalse();
  }

  @DisplayName("지정되지 않은 헤더의 목록을 조회하면 원래 값을 반환한다")
  @Test
  void getHeaders_shouldReturnOriginalValuesForVisibleHeader() {
    // given
    when(mockRequest.getHeaders(VISIBLE_HEADER))
        .thenReturn(Collections.enumeration(Collections.singletonList("visible_value")));

    // then
    assertThat(Collections.list(wrapper.getHeaders(VISIBLE_HEADER)))
        .containsExactly("visible_value");
  }

  @DisplayName("전체 헤더 이름 목록에서 지정된 헤더가 제외된다")
  @Test
  void getHeaderNames_shouldExcludeHiddenHeader() {
    // given
    when(mockRequest.getHeaderNames())
        .thenReturn(Collections.enumeration(Arrays.asList(HEADER_TO_HIDE, VISIBLE_HEADER)));

    // then
    assertThat(Collections.list(wrapper.getHeaderNames())).containsExactly(VISIBLE_HEADER);
  }
}
