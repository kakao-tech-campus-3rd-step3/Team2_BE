package kr.it.pullit.modules.questionset.service.factory;

import kr.it.pullit.modules.questionset.domain.enums.QuestionType;
import kr.it.pullit.modules.questionset.service.policy.type.MultipleChoicePolicy;
import kr.it.pullit.modules.questionset.service.policy.type.QuestionTypePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionTypePolicyFactory {
  MultipleChoicePolicy multipleChoicePolicy;

  public QuestionTypePolicy getInstance(QuestionType questionType) {
    return switch (questionType) {
      case MULTIPLE_CHOICE -> multipleChoicePolicy;
      default -> throw new IllegalArgumentException("Unsupported question type: " + questionType);
    };
  }
}
