package kr.it.pullit.modules.questionset.domain.entity;

import java.util.Objects;
import kr.it.pullit.modules.questionset.domain.enums.DifficultyType;
import kr.it.pullit.modules.questionset.domain.enums.QuestionType;

public record QuestionGenerationSpecification(
    DifficultyType difficultyType, QuestionType questionType, Integer questionCount) {
  public QuestionGenerationSpecification {
    Objects.requireNonNull(difficultyType, "DifficultyType is required");
    Objects.requireNonNull(questionType, "QuestionType is required");
    Objects.requireNonNull(questionCount, "QuestionCount is required");
  }
}
