package kr.it.pullit.support.fixture;

import kr.it.pullit.modules.questionset.domain.entity.MultipleChoiceQuestion;
import kr.it.pullit.modules.questionset.domain.entity.ShortAnswerQuestion;
import kr.it.pullit.modules.questionset.domain.entity.TrueFalseQuestion;
import kr.it.pullit.support.builder.TestMultipleChoiceQuestionBuilder;
import kr.it.pullit.support.builder.TestShortAnswerQuestionBuilder;
import kr.it.pullit.support.builder.TestTrueFalseQuestionBuilder;

public final class QuestionFixtures {

  private QuestionFixtures() {}

  public static TrueFalseQuestion aCorrectTrueFalseQuestion() {
    return TestTrueFalseQuestionBuilder.builder().answer(true).build();
  }

  public static TrueFalseQuestion anIncorrectTrueFalseQuestion() {
    return TestTrueFalseQuestionBuilder.builder().answer(false).build();
  }

  public static MultipleChoiceQuestion aCorrectMultipleChoiceQuestion() {
    return TestMultipleChoiceQuestionBuilder.builder().answer("보기1").build();
  }

  public static ShortAnswerQuestion aCorrectShortAnswerQuestion() {
    return TestShortAnswerQuestionBuilder.builder().answer("정답").build();
  }
}
