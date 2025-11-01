package kr.it.pullit.support.builder;

import java.util.ArrayList;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.domain.entity.TrueFalseQuestion;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import lombok.Builder;

public record TestTrueFalseQuestionBuilder() {

  @Builder(builderMethodName = "internalBuilder")
  private static TrueFalseQuestion build(
      QuestionSet questionSet, String questionText, boolean answer, String explanation) {
    return TrueFalseQuestion.builder()
        .questionSet(questionSet)
        .questionText(questionText)
        .answer(answer)
        .explanation(explanation)
        .build();
  }

  public static TrueFalseQuestionBuilder builder() {
    QuestionSet questionSet =
        TestQuestionSetBuilder.builder()
            .type(QuestionType.TRUE_FALSE)
            .questions(new ArrayList<>())
            .build();

    return internalBuilder()
        .questionSet(questionSet)
        .questionText("이것은 테스트 문제입니다.")
        .answer(true)
        .explanation("테스트 설명입니다.");
  }
}
