package kr.it.pullit.modules.questionset.service;

import java.util.List;
import java.util.stream.Collectors;
import kr.it.pullit.modules.questionset.api.MarkingPublicApi;
import kr.it.pullit.modules.questionset.web.dto.request.MarkingServiceRequest;
import kr.it.pullit.modules.wronganswer.api.WrongAnswerPublicApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarkingService implements MarkingPublicApi {

  private final WrongAnswerPublicApi wrongAnswerPublicApi;

  @Override
  public void markQuestionAsIncorrect(List<MarkingServiceRequest> requests) {
    if (requests == null || requests.isEmpty()) {
      return;
    }

    Long memberId = requests.getFirst().memberId();
    List<Long> questionIds =
        requests.stream().map(MarkingServiceRequest::questionId).collect(Collectors.toList());

    wrongAnswerPublicApi.markAsWrongAnswers(memberId, questionIds);
  }
}
