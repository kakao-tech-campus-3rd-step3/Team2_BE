package kr.it.pullit.modules.projection.learnstats.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsDailyPublicApi;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsProjectionPublicApi;
import kr.it.pullit.modules.projection.learnstats.web.dto.DailyStatsResponse;
import kr.it.pullit.modules.projection.learnstats.web.dto.LearnStatsResponse;
import kr.it.pullit.support.annotation.AuthenticatedMvcSliceTest;
import kr.it.pullit.support.security.WithMockMember;
import kr.it.pullit.support.test.ControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@AuthenticatedMvcSliceTest(controllers = LearnStatsController.class)
class LearnStatsControllerTest extends ControllerTest {

  @MockitoBean private LearnStatsProjectionPublicApi learnStatePublicApi;

  @MockitoBean private LearnStatsDailyPublicApi learnStatsDailyPublicApi;

  @Test
  @WithMockMember
  @DisplayName("사용자의 학습 통계를 조회한다")
  void shouldReturnLearnStats() throws Exception {
    // given
    var memberId = 1L;
    var response =
        LearnStatsResponse.builder()
            .totalQuestionSetCount(10)
            .totalSolvedQuestionSetCount(5)
            .totalSolvedQuestionCount(50)
            .weeklySolvedQuestionCount(15)
            .consecutiveLearningDays(3)
            .build();

    given(learnStatePublicApi.getLearnStats(memberId)).willReturn(response);

    // when & then
    mockMvc
        .perform(get("/api/members/{memberId}/learn-stats", memberId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalQuestionSetCount").value(10))
        .andExpect(jsonPath("$.totalSolvedQuestionSetCount").value(5))
        .andExpect(jsonPath("$.totalSolvedQuestionCount").value(50))
        .andExpect(jsonPath("$.weeklySolvedQuestionCount").value(15))
        .andExpect(jsonPath("$.consecutiveLearningDays").value(3));
  }

  @Test
  @WithMockMember
  @DisplayName("신규 사용자의 학습 통계를 조회하면 모든 값이 0으로 초기화된 통계를 반환한다")
  void shouldReturnZeroedStatsForNewUser() throws Exception {
    // given
    var memberId = 2L; // New user
    var emptyResponse = new LearnStatsResponse(); // All fields are 0 by default
    given(learnStatePublicApi.getLearnStats(memberId)).willReturn(emptyResponse);

    // when & then
    mockMvc
        .perform(get("/api/members/{memberId}/learn-stats", memberId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalQuestionSetCount").value(0))
        .andExpect(jsonPath("$.totalSolvedQuestionSetCount").value(0))
        .andExpect(jsonPath("$.totalSolvedQuestionCount").value(0))
        .andExpect(jsonPath("$.weeklySolvedQuestionCount").value(0))
        .andExpect(jsonPath("$.consecutiveLearningDays").value(0));
  }

  @Test
  @WithMockMember
  @DisplayName("사용자의 일일 학습 통계(잔디)를 조회한다")
  void shouldReturnDailyStats() throws Exception {
    // given
    var memberId = 1L;
    var from = LocalDate.of(2024, 1, 1);
    var to = LocalDate.of(2024, 1, 31);
    var response =
        List.of(
            new DailyStatsResponse(LocalDate.of(2024, 1, 1), 10),
            new DailyStatsResponse(LocalDate.of(2024, 1, 15), 5));

    given(learnStatsDailyPublicApi.findDailyStats(memberId, from, to)).willReturn(response);

    // when & then
    mockMvc
        .perform(
            get("/api/members/{memberId}/daily-stats", memberId)
                .param("from", "2024-01-01")
                .param("to", "2024-01-31"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].date").value("2024-01-01"))
        .andExpect(jsonPath("$[0].count").value(10))
        .andExpect(jsonPath("$[1].date").value("2024-01-15"))
        .andExpect(jsonPath("$[1].count").value(5));
  }

  @Test
  @WithMockMember
  @DisplayName("조회 기간 동안 학습 기록이 없으면 빈 배열을 반환한다")
  void shouldReturnEmptyArrayForNoDailyStats() throws Exception {
    // given
    var memberId = 1L;
    var from = LocalDate.of(2024, 2, 1);
    var to = LocalDate.of(2024, 2, 28);
    given(learnStatsDailyPublicApi.findDailyStats(memberId, from, to))
        .willReturn(Collections.emptyList());

    // when & then
    mockMvc
        .perform(
            get("/api/members/{memberId}/daily-stats", memberId)
                .param("from", "2024-02-01")
                .param("to", "2024-02-28"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  @WithMockMember
  @DisplayName("날짜 파라미터 형식이 잘못되면 400 에러를 반환한다")
  void shouldReturnBadRequestForInvalidDateFormat() throws Exception {
    // given
    var memberId = 1L;

    // when & then
    mockMvc
        .perform(
            get("/api/members/{memberId}/daily-stats", memberId)
                .param("from", "2024-01-01")
                .param("to", "invalid-date"))
        .andExpect(status().isBadRequest());
  }
}
