package kr.it.pullit.modules.questionset.service.creationstrategy;

import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionResponse;
import kr.it.pullit.modules.questionset.domain.entity.MultipleChoiceQuestion;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.domain.enums.QuestionType;
import org.springframework.stereotype.Component;

@Component
public class MultipleChoiceQuestionCreationStrategy implements QuestionCreationStrategy {

  @Override
  public Question create(QuestionSet questionSet, LlmGeneratedQuestionResponse questionDto) {
    return MultipleChoiceQuestion.createFromLlm(questionSet, questionDto);
  }

  @Override
  public QuestionType supportedType() {
    return QuestionType.MULTIPLE_CHOICE;
  }
}
