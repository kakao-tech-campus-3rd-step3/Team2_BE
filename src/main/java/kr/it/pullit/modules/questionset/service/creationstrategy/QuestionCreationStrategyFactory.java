package kr.it.pullit.modules.questionset.service.creationstrategy;

import jakarta.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import kr.it.pullit.modules.questionset.domain.enums.QuestionType;
import kr.it.pullit.modules.questionset.exception.InvalidQuestionException;
import kr.it.pullit.modules.questionset.exception.QuestionErrorCode;
import kr.it.pullit.modules.questionset.exception.QuestionSetErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionCreationStrategyFactory {

  private final List<QuestionCreationStrategy> strategies;
  private final Map<QuestionType, QuestionCreationStrategy> strategyMap =
      new EnumMap<>(QuestionType.class);

  @PostConstruct
  void init() {
    strategies.forEach(strategy -> strategyMap.put(strategy.supportedType(), strategy));
  }

  public QuestionCreationStrategy getStrategy(QuestionType type) {
    if (type == null) {
      throw new InvalidQuestionException(QuestionErrorCode.QUESTION_TYPE_REQUIRED);
    }
    QuestionCreationStrategy strategy = strategyMap.get(type);
    if (strategy == null) {
      throw new InvalidQuestionException(
          QuestionSetErrorCode.UNSUPPORTED_QUESTION_TYPE, type.name());
    }
    return strategy;
  }
}
