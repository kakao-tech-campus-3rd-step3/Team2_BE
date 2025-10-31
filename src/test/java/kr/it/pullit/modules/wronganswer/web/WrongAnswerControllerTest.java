package kr.it.pullit.modules.wronganswer.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import kr.it.pullit.modules.wronganswer.api.WrongAnswerPublicApi;
import kr.it.pullit.modules.wronganswer.exception.WrongAnswerNotFoundException;
import kr.it.pullit.modules.wronganswer.web.dto.WrongAnswerSetResponse;
import kr.it.pullit.shared.paging.dto.CursorPageResponse;
import kr.it.pullit.support.annotation.AuthenticatedMvcSliceTest;
import kr.it.pullit.support.apidocs.ProblemDetailTestUtils;
import kr.it.pullit.support.security.WithMockMember;
import kr.it.pullit.support.test.ControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@AuthenticatedMvcSliceTest(controllers = WrongAnswerController.class)
@DisplayName("WrongAnswerController 슬라이스 테스트")
class WrongAnswerControllerTest extends ControllerTest {

  @MockitoBean private WrongAnswerPublicApi wrongAnswerPublicApi;

  @Test
  @WithMockMember(memberId = 55L)
  @DisplayName("오답집 커서 조회 API는 페이징 응답을 반환한다")
  void shouldReturnPagedWrongAnswers() throws Exception {
    WrongAnswerSetResponse response =
        WrongAnswerSetResponse.of(1L, "자료구조 연습", List.of("교재"), null, "자료구조", 3L, null, 10L);
    CursorPageResponse<WrongAnswerSetResponse> page =
        CursorPageResponse.<WrongAnswerSetResponse>builder()
            .content(List.of(response))
            .nextCursor(20L)
            .hasNext(true)
            .size(1)
            .build();

    given(wrongAnswerPublicApi.getMyWrongAnswers(55L, 5L, 1)).willReturn(page);

    mockMvc
        .perform(get("/api/wrong-answers").param("cursor", "5").param("size", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].questionSetId").value(1L))
        .andExpect(jsonPath("$.content[0].sourceNames[0]").value("교재"))
        .andExpect(jsonPath("$.nextCursor").value(20L))
        .andExpect(jsonPath("$.hasNext").value(true));
  }

  @Test
  @WithMockMember(memberId = 77L)
  @DisplayName("전체 오답 조회 API는 모든 오답 문제집을 반환한다")
  void shouldReturnAllWrongAnswers() throws Exception {
    List<WrongAnswerSetResponse> responses =
        List.of(WrongAnswerSetResponse.of(2L, "OS 정리", List.of("강의"), null, "운영체제", 5L, null, 30L));
    given(wrongAnswerPublicApi.getAllMyWrongAnswers(77L)).willReturn(responses);

    mockMvc
        .perform(get("/api/wrong-answers/all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].questionSetTitle").value("OS 정리"))
        .andExpect(jsonPath("$[0].incorrectCount").value(5));
  }

  @Test
  @WithMockMember(memberId = 91L)
  @DisplayName("오답집 커서 조회 API는 존재하지 않는 오답일 때 ApiDocs 예시와 일치한다")
  void shouldMatchApiDocsWhenWrongAnswerNotFound() throws Exception {
    given(wrongAnswerPublicApi.getMyWrongAnswers(91L, null, 20))
        .willThrow(WrongAnswerNotFoundException.byMemberAndQuestion(91L, 100L));

    mockMvc
        .perform(get("/api/wrong-answers"))
        .andExpect(
            ProblemDetailTestUtils.conformToApiDocs(
                "/api/wrong-answers", "WRONG_ANSWER_NOT_FOUND"));
  }

  @Test
  @WithMockMember(memberId = 92L)
  @DisplayName("오답집 커서 조회 API는 복습할 오답이 없을 때 ApiDocs 예시와 일치한다")
  void shouldMatchApiDocsWhenNoWrongAnswersToReview() throws Exception {
    given(wrongAnswerPublicApi.getMyWrongAnswers(92L, null, 20))
        .willThrow(WrongAnswerNotFoundException.noWrongAnswersToReview());

    mockMvc
        .perform(get("/api/wrong-answers"))
        .andExpect(
            ProblemDetailTestUtils.conformToApiDocs(
                "/api/wrong-answers", "NO_WRONG_ANSWERS_TO_REVIEW"));
  }
}
