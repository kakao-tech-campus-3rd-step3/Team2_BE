package kr.it.pullit.support.builder;

import java.util.ArrayList;
import java.util.List;
import kr.it.pullit.modules.questionset.domain.entity.MultipleChoiceQuestion;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import lombok.Builder;

public record TestMultipleChoiceQuestionBuilder() {

  @Builder(builderMethodName = "internalBuilder")
  private static MultipleChoiceQuestion build(
      QuestionSet questionSet,
      String questionText,
      List<String> options,
      String answer,
      String explanation) {
    return MultipleChoiceQuestion.builder()
        .questionSet(questionSet)
        .questionText(questionText)
        .options(options)
        .answer(answer)
        .explanation(explanation)
        .build();
  }

  public static MultipleChoiceQuestionBuilder builder() {
    QuestionSet questionSet =
        TestQuestionSetBuilder.builder()
            .type(QuestionType.MULTIPLE_CHOICE)
            .questions(new ArrayList<>())
            .build();

    return internalBuilder()
        .questionSet(questionSet)
        .questionText("객관식 문제입니다.")
        .options(List.of("보기1", "보기2", "보기3", "보기4"))
        .answer("보기1")
        .explanation("객관식 문제 설명입니다.");
  }
}
