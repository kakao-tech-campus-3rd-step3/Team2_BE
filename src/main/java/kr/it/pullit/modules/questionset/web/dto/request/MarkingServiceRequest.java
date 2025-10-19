package kr.it.pullit.modules.questionset.web.dto.request;

import java.util.List;

public record MarkingServiceRequest(
    Long memberId, List<MarkingRequest> markingRequests, Boolean isReviewing) {

  public static MarkingServiceRequest of(
      Long memberId, List<MarkingRequest> request, Boolean isReviewing) {
    return new MarkingServiceRequest(memberId, request, isReviewing);
  }
}
