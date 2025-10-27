package kr.it.pullit.modules.questionset.web.dto.response;

import kr.it.pullit.shared.paging.dto.CursorPageResponse;
import lombok.Builder;

@Builder
public record MyQuestionSetsWithProgressResponse(
    CursorPageResponse<MyQuestionSetsResponse> questionSets, int learningProgress) {}
