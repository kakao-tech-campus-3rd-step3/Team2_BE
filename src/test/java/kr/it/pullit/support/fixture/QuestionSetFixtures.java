package kr.it.pullit.support.fixture;

import java.util.List;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import kr.it.pullit.support.builder.TestQuestionSetBuilder;

public final class QuestionSetFixtures {

  private QuestionSetFixtures() {}

  public static QuestionSet basic() {
    QuestionSet questionSet =
        TestQuestionSetBuilder.builder().type(QuestionType.MULTIPLE_CHOICE).build();
    questionSet.addQuestion(QuestionFixtures.aCorrectMultipleChoiceQuestion());
    return questionSet;
  }

  public static QuestionSet aTrueFalseQuestionSet() {
    QuestionSet questionSet =
        TestQuestionSetBuilder.builder().type(QuestionType.TRUE_FALSE).build();
    questionSet.addQuestion(QuestionFixtures.aCorrectTrueFalseQuestion());
    return questionSet;
  }

  public static QuestionSet aShortAnswerQuestionSet() {
    QuestionSet questionSet =
        TestQuestionSetBuilder.builder().type(QuestionType.SHORT_ANSWER).build();
    questionSet.addQuestion(QuestionFixtures.aCorrectShortAnswerQuestion());
    return questionSet;
  }

  public static QuestionSet withOwner(Long ownerId) {
    return TestQuestionSetBuilder.builder().ownerId(ownerId).build();
  }

  public static QuestionSet withQuestions(List<Question> questions) {
    return TestQuestionSetBuilder.builder().questions(questions).build();
  }
}
