package kr.it.pullit.modules.questionset.web.dto.request;

import java.util.List;

public record MarkingServiceRequest(Long memberId, List<Long> questionIds) {

  public static MarkingServiceRequest of(Long memberId, List<Long> questionIds) {
    return new MarkingServiceRequest(memberId, questionIds);
  }

}
