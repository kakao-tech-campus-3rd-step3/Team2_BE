package kr.it.pullit.modules.questionset.api;

import java.util.List;
import kr.it.pullit.modules.questionset.client.dto.LlmGeneratedQuestionDto;
import kr.it.pullit.modules.questionset.service.callback.QuestionGenerationSuccessCallback;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;

public interface QuestionPublicApi {

  void generateQuestions(
      QuestionSetResponse questionSetResponse, QuestionGenerationSuccessCallback callback);

  void saveQuestions(Long questionSetId, List<LlmGeneratedQuestionDto> questions);
}
