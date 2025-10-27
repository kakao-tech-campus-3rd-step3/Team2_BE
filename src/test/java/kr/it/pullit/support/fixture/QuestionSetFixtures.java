package kr.it.pullit.support.fixture;

import java.util.List;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.support.builder.TestQuestionSetBuilder;

public final class QuestionSetFixtures {

  private QuestionSetFixtures() {}

  public static QuestionSet basic() {
    return TestQuestionSetBuilder.builder().build();
  }

  public static QuestionSet withOwner(Long ownerId) {
    return TestQuestionSetBuilder.builder().ownerId(ownerId).build();
  }

  public static QuestionSet withQuestions(List<Question> questions) {
    return TestQuestionSetBuilder.builder().questions(questions).build();
  }
}
