package kr.it.pullit.modules.questionset.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import kr.it.pullit.modules.questionset.service.MarkingService;
import kr.it.pullit.modules.questionset.web.dto.request.MarkingRequest;
import kr.it.pullit.modules.questionset.web.dto.request.MarkingServiceRequest;
import kr.it.pullit.modules.questionset.web.dto.response.MarkQuestionsResponse;
import kr.it.pullit.modules.questionset.web.dto.response.MarkingResultDto;
import kr.it.pullit.support.annotation.AuthenticatedMvcSliceTest;
import kr.it.pullit.support.security.WithMockMember;
import kr.it.pullit.support.test.ControllerTest;
import org.junit.jupiter.api.DisplayName;
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
}
