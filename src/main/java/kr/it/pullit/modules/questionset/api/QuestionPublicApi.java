package kr.it.pullit.modules.questionset.api;

import java.util.List;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionResponse;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionGenerationRequest;

public interface QuestionPublicApi {

  List<LlmGeneratedQuestionResponse> generateQuestions(QuestionGenerationRequest request);

  void saveQuestion(Question question);
}
