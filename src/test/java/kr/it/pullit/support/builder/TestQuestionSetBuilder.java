package kr.it.pullit.support.builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.enums.DifficultyType;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import lombok.Builder;

public record TestQuestionSetBuilder() {

  @Builder(builderMethodName = "internalBuilder")
  private static QuestionSet build(
      Long ownerId,
      Set<Source> sources,
      String title,
      DifficultyType difficulty,
      QuestionType type,
      List<Question> questions) {

    QuestionSet questionSet =
        QuestionSet.builder()
            .ownerId(ownerId)
            .sources(sources)
            .title(title)
            .difficulty(difficulty)
            .type(type)
            .questionLength(questions != null ? questions.size() : 0)
            .build();

    if (questions != null) {
      questions.forEach(questionSet::addQuestion);
    }
    return questionSet;
  }

  public static QuestionSetBuilder builder() {
    return internalBuilder()
        .ownerId(1L)
        .sources(new HashSet<>())
        .title("기본 문제집")
        .difficulty(DifficultyType.EASY)
        .type(QuestionType.MULTIPLE_CHOICE)
        .questions(new ArrayList<>());
  }
}
