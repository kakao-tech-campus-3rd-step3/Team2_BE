package kr.it.pullit.modules.questionset.web.dto.request;

import java.util.List;

public record MarkingServiceRequest(
    Long memberId, List<Long> questionIds, String answer, Boolean isReviewing) {

  public static MarkingServiceRequest of(
      Long memberId, List<Long> questionIds, String answer, Boolean isReviewing) {
    return new MarkingServiceRequest(memberId, questionIds, answer, isReviewing);
  }
}
