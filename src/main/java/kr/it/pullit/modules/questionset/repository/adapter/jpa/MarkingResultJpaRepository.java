package kr.it.pullit.modules.questionset.repository.adapter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import kr.it.pullit.modules.questionset.domain.entity.MarkingResult;

public interface MarkingResultJpaRepository extends JpaRepository<MarkingResult, Long> {

  long countByQuestion_QuestionSetIdAndMemberId(Long questionSetId, Long memberId);

  long countByQuestion_QuestionSetIdAndMemberIdAndIsCorrectIsTrue(
      Long questionSetId, Long memberId);

  @Query(
      """
      SELECT COUNT(DISTINCT mr.question.id) FROM marking_result mr WHERE mr.memberId = :memberId
      """)
  long countDistinctQuestionByMemberId(@Param("memberId") Long memberId);

  long countByMemberIdAndIsCorrectIsTrue(Long memberId);
}
