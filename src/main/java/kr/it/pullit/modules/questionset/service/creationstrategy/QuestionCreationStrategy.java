package kr.it.pullit.modules.questionset.service.creationstrategy;

import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionResponse;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.enums.QuestionType;

public interface QuestionCreationStrategy {

  Question create(QuestionSet questionSet, LlmGeneratedQuestionResponse questionDto);

  QuestionType supportedType();
}
