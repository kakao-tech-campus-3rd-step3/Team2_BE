package kr.it.pullit.support.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.it.pullit.platform.web.cookie.CookieManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 컨트롤러 슬라이스 테스트를 위한 추상 기본 클래스입니다.
 *
 * <p>모든 컨트롤러 테스트에서 공통으로 사용되는 MockMvc, ObjectMapper 필드와 자주 필요한 CookieManager Mock Bean을 제공하여 중복을
 * 제거합니다.
 */
public abstract class ControllerTest {

  @Autowired protected MockMvc mockMvc;

  @Autowired protected ObjectMapper objectMapper;

  @MockitoBean protected CookieManager cookieManager;
}
