package kr.it.pullit.modules.questionset.service.event;

import java.util.List;
import kr.it.pullit.modules.questionset.web.dto.response.MarkingResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MarkingCompletedEvent {

  private final Long memberId;
  private final List<MarkingResult> results;
  private final boolean isReviewing;
}
