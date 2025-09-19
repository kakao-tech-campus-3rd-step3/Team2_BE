package kr.it.pullit.modules.questionset.service.callback;

import java.util.List;
import kr.it.pullit.modules.questionset.client.dto.LlmGeneratedQuestionDto;

@FunctionalInterface
public interface QuestionGenerationSuccessCallback {

  void onSuccess(List<LlmGeneratedQuestionDto> questions);
}
