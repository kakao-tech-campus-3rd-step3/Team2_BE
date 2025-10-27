package kr.it.pullit.modules.questionset.web;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.api.QuestionSetWithStatsFacade;
import kr.it.pullit.modules.questionset.enums.DifficultyType;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetCreateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetUpdateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsResponse;
import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsWithProgressResponse;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;
import kr.it.pullit.shared.paging.dto.CursorPageResponse;
import kr.it.pullit.support.annotation.AuthenticatedMvcSliceTest;
import kr.it.pullit.support.security.WithMockMember;
import kr.it.pullit.support.test.ControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@AuthenticatedMvcSliceTest(controllers = QuestionSetController.class)
@DisplayName("QuestionSetController 슬라이스 테스트(단일 파일)")
class QuestionSetControllerTest extends ControllerTest {

  @MockitoBean private QuestionSetPublicApi questionSetPublicApi;
  @MockitoBean private QuestionSetWithStatsFacade questionSetWithStatsFacade;

  @Nested
  @DisplayName("생성")
  class Create {

    @Test
    @WithMockMember(memberId = 5L)
    @DisplayName("문제집을 생성하면 201과 Location 헤더를 반환한다")
    void createReturns201WithLocation() throws Exception {
      // given
      long newId = 123L;
      QuestionSetResponse response = mock(QuestionSetResponse.class);
      when(response.getId()).thenReturn(newId);
      given(questionSetPublicApi.create(any(QuestionSetCreateRequestDto.class), anyLong()))
          .willReturn(response);

      String body =
          """
              {
                "difficulty": "EASY",
                "questionCount": 3,
                "type": "MULTIPLE_CHOICE",
                "sourceIds": [1, 2]
              }
              """;

      // when & then
      mockMvc
          .perform(post("/api/question-set").contentType(MediaType.APPLICATION_JSON).content(body))
          .andExpect(status().isCreated())
          .andExpect(header().string(LOCATION, endsWith("/api/question-set/" + newId)));

      // verify param binding
      var expectedDto =
          new QuestionSetCreateRequestDto(
              DifficultyType.EASY, 3, QuestionType.MULTIPLE_CHOICE, List.of(1L, 2L), null);
      verify(questionSetPublicApi).create(expectedDto, 5L);
    }
  }

  @Nested
  @DisplayName("단건 조회")
  class FindOne {

    @Test
    @WithMockMember(memberId = 11L)
    @DisplayName("기본(첫 풀이) 조회는 isReviewing=false로 위임된다")
    void getForFirstSolving() throws Exception {
      given(questionSetPublicApi.getQuestionSetForSolving(anyLong(), anyLong(), anyBoolean()))
          .willReturn(mock(QuestionSetResponse.class));

      mockMvc.perform(get("/api/question-set/{id}", 12L)).andExpect(status().isOk());

      ArgumentCaptor<Long> idCap = ArgumentCaptor.forClass(Long.class);
      ArgumentCaptor<Long> memberIdCap = ArgumentCaptor.forClass(Long.class);
      ArgumentCaptor<Boolean> reviewingCap = ArgumentCaptor.forClass(Boolean.class);

      verify(questionSetPublicApi)
          .getQuestionSetForSolving(idCap.capture(), memberIdCap.capture(), reviewingCap.capture());
      assert idCap.getValue() == 12L;
      assert memberIdCap.getValue() == 11L;
      assert reviewingCap.getValue() == Boolean.FALSE;
    }

    @Test
    @WithMockMember(memberId = 60L)
    @DisplayName("오답노트 복습 모드 조회는 isReviewing=true로 위임된다")
    void getForReviewing() throws Exception {
      given(questionSetPublicApi.getQuestionSetForSolving(anyLong(), anyLong(), anyBoolean()))
          .willReturn(mock(QuestionSetResponse.class));

      mockMvc
          .perform(get("/api/question-set/{id}?isReviewing=true", 2L))
          .andExpect(status().isOk());

      ArgumentCaptor<Boolean> reviewingCap = ArgumentCaptor.forClass(Boolean.class);
      verify(questionSetPublicApi)
          .getQuestionSetForSolving(anyLong(), anyLong(), reviewingCap.capture());
      assert reviewingCap.getValue() == Boolean.TRUE;
    }
  }

  @Nested
  @DisplayName("목록 조회")
  class FindList {

    @Test
    @WithMockMember(memberId = 20L)
    @DisplayName("커서 기반 목록을 조회하면 200을 반환한다")
    void getMyQuestionSetsWithCursor() throws Exception {
      @SuppressWarnings("unchecked")
      CursorPageResponse<MyQuestionSetsResponse> page = mock(CursorPageResponse.class);
      given(
              questionSetWithStatsFacade.getMemberQuestionSetsWithProgress(
                  anyLong(), any(), anyInt(), any()))
          .willReturn(new MyQuestionSetsWithProgressResponse(page, 0));

      mockMvc
          .perform(get("/api/question-set").param("cursor", "11").param("size", "2"))
          .andExpect(status().isOk());

      verify(questionSetWithStatsFacade).getMemberQuestionSetsWithProgress(20L, 11L, 2, null);
    }

    @Test
    @WithMockMember(memberId = 21L)
    @DisplayName("전체 목록을 조회하면 200을 반환한다")
    void getAllMyQuestionSets() throws Exception {
      given(questionSetPublicApi.getMemberQuestionSets(anyLong())).willReturn(List.of());

      mockMvc.perform(get("/api/question-set/all")).andExpect(status().isOk());

      verify(questionSetPublicApi).getMemberQuestionSets(21L);
    }
  }

  @Nested
  @DisplayName("수정/삭제")
  class UpdateDelete {

    @Test
    @WithMockMember(memberId = 7L)
    @DisplayName("제목을 수정하면 200을 반환한다")
    void updateTitle() throws Exception {
      String body =
          """
            { "title": "새 제목" }
          """;

      mockMvc
          .perform(
              patch("/api/question-set/{id}", 77L)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(body))
          .andExpect(status().isOk());

      verify(questionSetPublicApi).update(77L, new QuestionSetUpdateRequestDto("새 제목", null), 7L);
    }

    @Test
    @WithMockMember(memberId = 9L)
    @DisplayName("삭제하면 200을 반환한다")
    void deleteQuestionSet() throws Exception {
      mockMvc.perform(delete("/api/question-set/{id}", 88L)).andExpect(status().isOk());

      verify(questionSetPublicApi).delete(88L, 9L);
    }
  }
}
