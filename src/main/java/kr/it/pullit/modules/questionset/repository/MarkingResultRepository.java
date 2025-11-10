package kr.it.pullit.modules.questionset.repository;

import kr.it.pullit.modules.questionset.domain.entity.MarkingResult;

public interface MarkingResultRepository {
  MarkingResult save(MarkingResult markingResult);

  long countByQuestionSetIdAndMemberId(Long questionSetId, Long memberId);

  long countCorrectByQuestionSetIdAndMemberId(Long questionSetId, Long memberId);

  long countDistinctQuestionByMemberId(Long memberId);

  long countByMemberIdAndIsCorrectIsTrue(Long memberId);

  long countCorrectAnswersByMemberId(Long memberId);
}
