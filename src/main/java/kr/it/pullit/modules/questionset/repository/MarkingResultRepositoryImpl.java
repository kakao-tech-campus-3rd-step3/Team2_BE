package kr.it.pullit.modules.questionset.repository;

import org.springframework.stereotype.Repository;
import kr.it.pullit.modules.questionset.domain.entity.MarkingResult;
import kr.it.pullit.modules.questionset.repository.adapter.jpa.MarkingResultJpaRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MarkingResultRepositoryImpl implements MarkingResultRepository {

  private final MarkingResultJpaRepository jpaRepository;

  @Override
  public MarkingResult save(MarkingResult markingResult) {
    return jpaRepository.save(markingResult);
  }

  @Override
  public long countByMemberIdAndIsCorrectIsTrue(Long memberId) {
    return jpaRepository.countByMemberIdAndIsCorrectIsTrue(memberId);
  }

  @Override
  public long countByQuestionSetIdAndMemberId(Long questionSetId, Long memberId) {
    return jpaRepository.countByQuestion_QuestionSetIdAndMemberId(questionSetId, memberId);
  }
}
