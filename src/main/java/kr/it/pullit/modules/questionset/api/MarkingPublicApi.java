package kr.it.pullit.modules.questionset.api;

import kr.it.pullit.modules.questionset.web.dto.request.MarkingServiceRequest;
import kr.it.pullit.modules.questionset.web.dto.response.MarkQuestionsResponse;

public interface MarkingPublicApi {

  MarkQuestionsResponse markQuestions(MarkingServiceRequest request);
}
