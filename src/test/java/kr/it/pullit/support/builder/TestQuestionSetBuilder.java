package kr.it.pullit.support.builder;

import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.domain.enums.DifficultyType;
import kr.it.pullit.modules.questionset.domain.enums.QuestionType;

public final class TestQuestionSetBuilder {

  private Long ownerId = 1L;
  private String title = "문제집";
  private DifficultyType difficulty = DifficultyType.EASY;
  private QuestionType type = QuestionType.MULTIPLE_CHOICE;
  private Integer questionLength = 3;

  private boolean markComplete = false;
  private boolean markFailed = false;

  private TestQuestionSetBuilder() {}

  public static TestQuestionSetBuilder builder() {
    return new TestQuestionSetBuilder();
  }

  public static TestQuestionSetBuilder builder(Long ownerId) {
    return builder().ownerId(ownerId);
  }

  public TestQuestionSetBuilder owner(Member member) {
    if (member != null) this.ownerId = member.getId();
    return this;
  }

  public TestQuestionSetBuilder ownerId(Long ownerId) {
    this.ownerId = ownerId;
    return this;
  }

  public TestQuestionSetBuilder title(String title) {
    this.title = title;
    return this;
  }

  public TestQuestionSetBuilder difficulty(DifficultyType difficulty) {
    this.difficulty = difficulty;
    return this;
  }

  public TestQuestionSetBuilder difficultyEasy() {
    this.difficulty = DifficultyType.EASY;
    return this;
  }

  public TestQuestionSetBuilder difficultyHard() {
    this.difficulty = DifficultyType.HARD;
    return this;
  }

  public TestQuestionSetBuilder type(QuestionType type) {
    this.type = type;
    return this;
  }

  public TestQuestionSetBuilder typeMultipleChoice() {
    this.type = QuestionType.MULTIPLE_CHOICE;
    return this;
  }

  public TestQuestionSetBuilder typeShortAnswer() {
    this.type = QuestionType.SHORT_ANSWER;
    return this;
  }

  public TestQuestionSetBuilder typeTrueFalse() {
    this.type = QuestionType.TRUE_FALSE;
    return this;
  }

  public TestQuestionSetBuilder questionLength(int length) {
    this.questionLength = length;
    return this;
  }

  /** 기본값 PENDING 유지 */
  public TestQuestionSetBuilder statusPending() {
    this.markComplete = false;
    this.markFailed = false;
    return this;
  }

  public TestQuestionSetBuilder statusComplete() {
    this.markComplete = true;
    this.markFailed = false;
    return this;
  }

  public TestQuestionSetBuilder statusFailed() {
    this.markComplete = false;
    this.markFailed = true;
    return this;
  }

  public QuestionSet build() {
    QuestionSet qs =
        QuestionSet.builder()
            .ownerId(ownerId)
            .title(title)
            .difficulty(difficulty)
            .type(type)
            .questionLength(questionLength)
            .build();

    if (markComplete) {
      qs.completeProcessing();
    } else if (markFailed) {
      qs.failProcessing();
    }

    return qs;
  }
}
