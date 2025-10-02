package kr.it.pullit.modules.questionset.api;

import kr.it.pullit.modules.questionset.web.dto.request.MarkingServiceRequest;

public interface MarkingPublicApi {

  void markQuestions(MarkingServiceRequest request);
}
