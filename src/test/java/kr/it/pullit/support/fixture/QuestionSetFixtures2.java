package kr.it.pullit.support.fixture;

import java.util.List;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.support.builder.TestQuestionSetBuilder2;

public final class QuestionSetFixtures2 {

  private QuestionSetFixtures2() {}

  public static QuestionSet basic() {
    return TestQuestionSetBuilder2.builder().build();
  }

  public static QuestionSet withOwner(Long ownerId) {
    return TestQuestionSetBuilder2.builder().ownerId(ownerId).build();
  }

  public static QuestionSet withQuestions(List<Question> questions) {
    return TestQuestionSetBuilder2.builder().questions(questions).build();
  }
}
