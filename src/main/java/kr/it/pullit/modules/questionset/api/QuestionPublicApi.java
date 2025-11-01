package kr.it.pullit.modules.questionset.api;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionSetResponse;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionGenerationRequest;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionCreateRequest;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionUpdateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionResponse;

public interface QuestionPublicApi {

  LlmGeneratedQuestionSetResponse generateQuestions(QuestionGenerationRequest request);

  void saveQuestion(Question question);

  QuestionResponse createQuestion(QuestionCreateRequest requestDto);

  QuestionResponse updateQuestion(Long questionId, QuestionUpdateRequestDto requestDto);

  void deleteQuestion(Long questionId);

  QuestionResponse getQuestionById(Long questionId);

  Optional<Question> findEntityById(Long questionId);

  List<Question> findEntitiesByIds(List<Long> questionIds);
}
