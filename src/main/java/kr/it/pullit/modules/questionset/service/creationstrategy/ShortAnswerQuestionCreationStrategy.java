package kr.it.pullit.modules.questionset.service.creationstrategy;

import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionResponse;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.domain.entity.ShortAnswerQuestion;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import org.springframework.stereotype.Component;

@Component
public class ShortAnswerQuestionCreationStrategy implements QuestionCreationStrategy {

  @Override
  public Question create(QuestionSet questionSet, LlmGeneratedQuestionResponse questionDto) {
    return ShortAnswerQuestion.createFromLlm(questionSet, questionDto);
  }

  @Override
  public QuestionType supportedType() {
    return QuestionType.SHORT_ANSWER;
  }
}
