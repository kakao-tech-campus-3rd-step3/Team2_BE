package kr.it.pullit.support.builder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.enums.DifficultyType;
import kr.it.pullit.modules.questionset.enums.QuestionSetStatus;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import lombok.Builder;

public record TestQuestionSetBuilder() {

  @Builder(builderMethodName = "internalBuilder")
  private static QuestionSet build(
      Long id,
      Long ownerId,
      Set<Source> sources,
      String title,
      DifficultyType difficulty,
      QuestionType type,
      List<Question> questions,
      Integer questionLength,
      QuestionSetStatus status) {

    int finalQuestionLength =
        (questionLength != null) ? questionLength : (questions != null ? questions.size() : 0);

    QuestionSet questionSet =
        QuestionSet.builder()
            .ownerId(ownerId)
            .sources(sources)
            .title(title)
            .difficulty(difficulty)
            .type(type)
            .questionLength(finalQuestionLength)
            .build();

    if (id != null) {
      setIdUsingReflection(questionSet, id);
    }

    if (questions != null) {
      questions.forEach(questionSet::addQuestion);
    }

    if (status != null && status != QuestionSetStatus.PENDING) {
      switch (status) {
        case COMPLETE -> questionSet.completeProcessing();
        case FAILED -> questionSet.failProcessing();
        default -> {}
      }
    }

    return questionSet;
  }

  private static void setIdUsingReflection(QuestionSet questionSet, Long id) {
    try {
      Field idField = QuestionSet.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(questionSet, id);

      Field versionField = QuestionSet.class.getDeclaredField("version");
      versionField.setAccessible(true);
      versionField.set(questionSet, 0L);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("리플렉션을 사용하여 ID를 설정하는 중 오류가 발생했습니다.", e);
    }
  }

  public static QuestionSetBuilder builder() {
    return internalBuilder()
        .ownerId(1L)
        .sources(new HashSet<>())
        .title("기본 문제집")
        .difficulty(DifficultyType.EASY)
        .type(QuestionType.MULTIPLE_CHOICE)
        .questions(new ArrayList<>())
        .questionLength(10)
        .status(QuestionSetStatus.PENDING);
  }
}
