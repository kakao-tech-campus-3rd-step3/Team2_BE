package kr.it.pullit.modules.questionset.api;

public interface MarkingResultPublicApi {
  /**
   * 사용자가 시도한 적이 있는 중복 없는 문제의 총 수를 계산합니다.
   *
   * @param memberId 회원 ID
   * @return 총 푼 문제 수
   */
  long countTotalAttemptedQuestionsByMemberId(Long memberId);

  /**
   * 사용자가 정답을 맞힌 문제의 총 수를 계산합니다.
   *
   * @param memberId 회원 ID
   * @return 총 맞힌 문제 수
   */
  long countTotalCorrectQuestionsByMemberId(Long memberId);

  long countCorrectAnswersByMemberId(Long memberId);
}
