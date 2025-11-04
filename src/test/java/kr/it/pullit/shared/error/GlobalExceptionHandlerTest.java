package kr.it.pullit.shared.error;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import kr.it.pullit.modules.questionset.exception.QuestionSetNotReadyException;
import kr.it.pullit.shared.error.dto.TestEnum;
import kr.it.pullit.support.config.PermitAllSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = TestController.class,
    excludeFilters = {
      @ComponentScan.Filter(
          type = FilterType.ASSIGNABLE_TYPE,
          classes = {
            kr.it.pullit.platform.security.jwt.filter.DevAuthenticationFilter.class,
            kr.it.pullit.platform.security.jwt.filter.JwtAuthenticationFilter.class
          })
    })
@Import({PermitAllSecurityConfig.class, GlobalExceptionHandler.class})
class GlobalExceptionHandlerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @DisplayName("정의되지 않은 Enum 타입으로 요청 시, 400 Bad Request와 함께 상세 메시지를 반환한다")
  @Test
  void handleHttpMessageNotReadable_withInvalidEnumValue() throws Exception {
    // given
    String invalidRequest =
        """
            { "testEnum" : "INVALID_VALUE" }
            """;

    // when & then
    mockMvc
        .perform(
            post("/api/test/enum").contentType(MediaType.APPLICATION_JSON).content(invalidRequest))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(CommonErrorCode.INVALID_INPUT_VALUE.getCode()))
        .andExpect(
            jsonPath("$.detail").value("정의되지 않은 enum 타입입니다. 지원되는 타입: " + TestEnum.VALID_VALUE));
  }

  @DisplayName("InvalidRefreshTokenException 발생 시, 401 Unauthorized와 함께 지정된 에러 코드를 반환한다")
  @Test
  void handleInvalidRefreshTokenException() throws Exception {
    // when & then
    mockMvc
        .perform(get("/api/test/invalid-refresh-token"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("AUTH006"));
  }

  @DisplayName("BusinessException 발생 시, 예외에 정의된 상태 코드와 에러 코드를 반환한다")
  @Test
  void handleBusinessException() throws Exception {
    // when & then
    mockMvc
        .perform(get("/api/test/business-exception"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("T001"));
  }

  static Stream<BusinessException> questionSetExceptions() {
    return Stream.of(QuestionSetNotReadyException.byId(1L));
  }

  @DisplayName("QuestionSet 관련 예외 발생 시, 정의된 상태 코드와 에러 코드를 반환한다")
  @MethodSource("questionSetExceptions")
  @ParameterizedTest
  void handleQuestionSetException(BusinessException exception) throws Exception {
    // given
    String url = "/api/test/question-set-not-ready";
    ErrorCode errorCode = exception.getErrorCode();

    // when & then
    mockMvc
        .perform(get(url))
        .andExpect(status().is(errorCode.getStatus().value()))
        .andExpect(jsonPath("$.code").value(errorCode.getCode()));
  }

  @DisplayName("IllegalArgumentException 발생 시, 400 Bad Request와 함께 지정된 코드를 반환한다")
  @Test
  void handleIllegalArgumentException() throws Exception {
    // when & then
    mockMvc
        .perform(get("/api/test/illegal-argument"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("C_001"))
        .andExpect(jsonPath("$.detail").value("Test Illegal Argument"));
  }
}
