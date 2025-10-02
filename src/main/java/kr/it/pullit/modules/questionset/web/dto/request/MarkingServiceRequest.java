package kr.it.pullit.modules.questionset.web.dto.request;

import java.util.List;

public record MarkingServiceRequest(Long memberId, List<Long> questionIds, Boolean isReviewing) {

  public static MarkingServiceRequest of(
      Long memberId, List<Long> questionIds, Boolean isReviewing) {
    return new MarkingServiceRequest(memberId, questionIds, isReviewing);
  }
}
