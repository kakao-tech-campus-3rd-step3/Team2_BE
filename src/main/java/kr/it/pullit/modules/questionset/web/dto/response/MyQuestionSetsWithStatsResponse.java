package kr.it.pullit.modules.questionset.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.it.pullit.modules.projection.learnstats.web.dto.LearnStatsResponse;
import kr.it.pullit.shared.paging.dto.CursorPageResponse;
import lombok.Builder;

@Builder
public record MyQuestionSetsWithStatsResponse(
    @Schema(description = "문제집 목록 (페이지네이션)")
        CursorPageResponse<MyQuestionSetsResponse> questionSets,
    @Schema(description = "종합 학습 통계") LearnStatsResponse learnStats) {}
