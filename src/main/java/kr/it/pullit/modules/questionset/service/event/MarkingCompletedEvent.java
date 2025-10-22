package kr.it.pullit.modules.questionset.service.event;

import java.util.List;
import kr.it.pullit.modules.questionset.web.dto.response.MarkingResult;

public record MarkingCompletedEvent(
    Long memberId, List<MarkingResult> results, boolean isReviewing) {}
