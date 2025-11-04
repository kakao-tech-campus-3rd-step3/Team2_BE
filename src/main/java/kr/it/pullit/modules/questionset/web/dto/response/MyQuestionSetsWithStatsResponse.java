package kr.it.pullit.modules.questionset.web.dto.response;

import kr.it.pullit.modules.projection.learnstats.web.dto.LearnStatsResponse;
import kr.it.pullit.shared.paging.dto.CursorPageResponse;
import lombok.Builder;

@Builder
public record MyQuestionSetsWithStatsResponse(
    CursorPageResponse<MyQuestionSetsResponse> questionSets, LearnStatsResponse learnStats) {}
