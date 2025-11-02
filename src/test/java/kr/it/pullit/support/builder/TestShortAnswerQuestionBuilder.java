package kr.it.pullit.support.builder;

import java.util.ArrayList;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.domain.entity.ShortAnswerQuestion;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import lombok.Builder;

public record TestShortAnswerQuestionBuilder() {

  @Builder(builderMethodName = "internalBuilder")
  private static ShortAnswerQuestion build(
      QuestionSet questionSet, String questionText, String answer, String explanation) {
    return ShortAnswerQuestion.builder()
        .questionSet(questionSet)
        .questionText(questionText)
        .answer(answer)
        .explanation(explanation)
        .build();
  }

  public static ShortAnswerQuestionBuilder builder() {
    QuestionSet questionSet =
        TestQuestionSetBuilder.builder()
            .type(QuestionType.SHORT_ANSWER)
            .questions(new ArrayList<>())
            .build();

    return internalBuilder()
        .questionSet(questionSet)
        .questionText("주관식 문제입니다.")
        .answer("정답")
        .explanation("주관식 문제 설명입니다.");
  }
}
