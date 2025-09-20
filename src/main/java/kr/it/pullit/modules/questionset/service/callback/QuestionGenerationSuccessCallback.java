package kr.it.pullit.modules.questionset.service.callback;

import java.util.List;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionResponse;

@FunctionalInterface
public interface QuestionGenerationSuccessCallback {
  void onSuccess(List<LlmGeneratedQuestionResponse> questions);
}
