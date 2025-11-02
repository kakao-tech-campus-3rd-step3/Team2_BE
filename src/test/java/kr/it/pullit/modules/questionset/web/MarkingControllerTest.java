package kr.it.pullit.modules.questionset.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import kr.it.pullit.modules.questionset.exception.QuestionNotFoundException;
import kr.it.pullit.modules.questionset.service.MarkingService;
import kr.it.pullit.modules.questionset.web.dto.request.MarkingRequest;
import kr.it.pullit.modules.questionset.web.dto.request.MarkingServiceRequest;
import kr.it.pullit.modules.questionset.web.dto.response.MarkQuestionsResponse;
import kr.it.pullit.modules.questionset.web.dto.response.MarkingResultDto;
import kr.it.pullit.support.annotation.AuthenticatedMvcSliceTest;
import kr.it.pullit.support.apidocs.ProblemDetailTestUtils;
import kr.it.pullit.support.security.WithMockMember;
import kr.it.pullit.support.test.ControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@AuthenticatedMvcSliceTest(controllers = MarkingController.class)
@DisplayName("MarkingController 슬라이스 테스트")
class MarkingControllerTest extends ControllerTest {

  @MockitoBean private MarkingService markingService;

  @Test
  @WithMockMember
  @DisplayName("로그인한 사용자는 문제 채점을 성공적으로 요청한다")
  void shouldSuccessfullyMarkQuestions() throws Exception {
    // given
    var markingRequests = List.of(MarkingRequest.of(1L, true), MarkingRequest.of(2L, "객관식답"));

    var requestPayload =
        List.of(
            Map.of("questionId", 1L, "memberAnswer", true, "memberAnswerType", "boolean"),
            Map.of("questionId", 2L, "memberAnswer", "객관식답", "memberAnswerType", "string"));

    var markingServiceRequest = MarkingServiceRequest.of(1L, markingRequests, false);

    var response =
        MarkQuestionsResponse.of(
            List.of(MarkingResultDto.of(1L, true), MarkingResultDto.of(2L, false)), 2, 1);

    given(markingService.markQuestions(markingServiceRequest)).willReturn(response);

    // when & then
    mockMvc
        .perform(
            post("/api/marking")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestPayload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalQuestions").value(2))
        .andExpect(jsonPath("$.correctCount").value(1))
        .andExpect(jsonPath("$.results[0].questionId").value(1L))
        .andExpect(jsonPath("$.results[0].isCorrect").value(true))
        .andExpect(jsonPath("$.results[1].questionId").value(2L))
        .andExpect(jsonPath("$.results[1].isCorrect").value(false));
  }

  @Nested
  @DisplayName("에러 응답 검증")
  class ErrorResponse {

    @Test
    @WithMockMember
    @DisplayName("입력값 유효성 검증 실패 시, ApiDocs의 ExampleObject와 실제 응답이 일치한다")
    void shouldMatchApiDocsWhenValidationFailed() throws Exception {
      // given
      var invalidRequestPayload =
          List.of(Map.of("memberAnswer", true, "memberAnswerType", "boolean"));

      // when & then
      mockMvc
          .perform(
              post("/api/marking")
                  .contentType(APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invalidRequestPayload)))
          .andExpect(ProblemDetailTestUtils.conformToApiDocs("/api/marking", "입력값 유효성 검증 실패"));
    }

    @Test
    @WithMockMember
    @DisplayName("존재하지 않는 문제 채점 시, ApiDocs의 ExampleObject와 실제 응답이 일치한다")
    void shouldMatchApiDocsWhenQuestionNotFound() throws Exception {
      // given
      var markingRequests = List.of(MarkingRequest.of(999L, true));
      var requestPayload =
          List.of(Map.of("questionId", 999L, "memberAnswer", true, "memberAnswerType", "boolean"));
      var markingServiceRequest = MarkingServiceRequest.of(1L, markingRequests, false);

      given(markingService.markQuestions(markingServiceRequest))
          .willThrow(QuestionNotFoundException.byId(999L));

      // when & then
      mockMvc
          .perform(
              post("/api/marking")
                  .contentType(APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestPayload)))
          .andExpect(ProblemDetailTestUtils.conformToApiDocs("/api/marking", "문제 조회 실패"));
    }
  }
}
