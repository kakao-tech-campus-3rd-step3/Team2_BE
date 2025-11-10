package kr.it.pullit.modules.questionset.repository;

import kr.it.pullit.modules.questionset.domain.entity.MarkingResult;
import kr.it.pullit.modules.questionset.repository.adapter.jpa.MarkingResultJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MarkingResultRepositoryImpl implements MarkingResultRepository {

  private final MarkingResultJpaRepository jpaRepository;

  @Override
  public MarkingResult save(MarkingResult markingResult) {
    return jpaRepository.save(markingResult);
  }

  @Override
  public long countByQuestionSetIdAndMemberId(Long questionSetId, Long memberId) {
    return jpaRepository.countByQuestion_QuestionSetIdAndMemberId(questionSetId, memberId);
  }

  @Override
  public long countCorrectByQuestionSetIdAndMemberId(Long questionSetId, Long memberId) {
    return jpaRepository.countByQuestion_QuestionSetIdAndMemberIdAndIsCorrectIsTrue(
        questionSetId, memberId);
  }

  @Override
  public long countDistinctQuestionByMemberId(Long memberId) {
    return jpaRepository.countDistinctQuestionByMemberId(memberId);
  }

  @Override
  public long countByMemberIdAndIsCorrectIsTrue(Long memberId) {
    return jpaRepository.countByMemberIdAndIsCorrectIsTrue(memberId);
  }

  @Override
  public long countCorrectAnswersByMemberId(Long memberId) {
    return jpaRepository.countCorrectAnswersByMemberId(memberId);
  }
}
