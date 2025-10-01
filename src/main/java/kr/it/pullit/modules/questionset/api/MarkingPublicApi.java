package kr.it.pullit.modules.questionset.api;

import java.util.List;
import kr.it.pullit.modules.questionset.web.dto.request.MarkingServiceRequest;

public interface MarkingPublicApi {

  void markQuestionAsIncorrect(List<MarkingServiceRequest> requests);
}
