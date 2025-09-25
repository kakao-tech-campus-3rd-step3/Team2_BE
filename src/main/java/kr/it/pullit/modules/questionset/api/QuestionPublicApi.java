package kr.it.pullit.modules.questionset.api;

import java.util.List;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionResponse;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionGenerationRequest;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionCreateRequest;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionUpdateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionResponse;

public interface QuestionPublicApi {

  List<LlmGeneratedQuestionResponse> generateQuestions(QuestionGenerationRequest request);

  void saveQuestion(Question question);

  QuestionResponse createQuestion(QuestionCreateRequest requestDto);

  QuestionResponse updateQuestion(Long questionId, QuestionUpdateRequestDto requestDto);

  void deleteQuestion(Long questionId);

  QuestionResponse getQuestionById(Long questionId);
}
