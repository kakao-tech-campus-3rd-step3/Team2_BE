package kr.it.pullit.support.fixture;

import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.domain.enums.DifficultyType;
import kr.it.pullit.modules.questionset.domain.enums.QuestionType;

public final class QuestionSetFixtures {

  private QuestionSetFixtures() {}

  /** 가장 기본 형태(PENDING, EASY, MULTIPLE_CHOICE, length=5) */
  public static QuestionSet basic(Long ownerId) {
    return QuestionSet.builder()
        .ownerId(ownerId)
        .title("기본 세트")
        .difficulty(DifficultyType.EASY)
        .type(QuestionType.MULTIPLE_CHOICE)
        .questionLength(5)
        .build();
  }

  /** 제목만 바꿔서 만들고 싶을 때 */
  public static QuestionSet withTitle(Long ownerId, String title) {
    return QuestionSet.builder()
        .ownerId(ownerId)
        .title(title)
        .difficulty(DifficultyType.EASY)
        .type(QuestionType.MULTIPLE_CHOICE)
        .questionLength(5)
        .build();
  }

  /** 완료 상태로 미리 만들어야 할 때 */
  public static QuestionSet complete(Long ownerId) {
    QuestionSet qs = basic(ownerId);
    qs.completeProcessing();
    return qs;
  }

  /** 실패 상태로 미리 만들어야 할 때 */
  public static QuestionSet failed(Long ownerId) {
    QuestionSet qs = basic(ownerId);
    qs.failProcessing();
    return qs;
  }

  /** 모든 주요 필드를 직접 지정하고 싶을 때 */
  public static QuestionSet of(
      Long ownerId,
      String title,
      DifficultyType difficulty,
      QuestionType type,
      int questionLength) {
    return QuestionSet.builder()
        .ownerId(ownerId)
        .title(title)
        .difficulty(difficulty)
        .type(type)
        .questionLength(questionLength)
        .build();
  }
}
