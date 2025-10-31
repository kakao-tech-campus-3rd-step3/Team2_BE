package kr.it.pullit.modules.questionset.event;

import java.util.List;
import kr.it.pullit.modules.questionset.web.dto.response.MarkingResultDto;

public record MarkingCompletedEvent(
    Long memberId, List<MarkingResultDto> results, boolean isReviewing) {}
