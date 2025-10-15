package kr.it.pullit.modules.questionset.repository.adapter.jpa;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionSetJpaRepository extends JpaRepository<QuestionSet, Long> {

  @Query(
      """
       SELECT qs
       FROM QuestionSet qs
       LEFT JOIN FETCH qs.questions
       WHERE qs.id = :id
       AND qs.owner.id = :memberId
      """)
  Optional<QuestionSet> findByIdAndMemberId(@Param("id") Long id, @Param("memberId") Long memberId);

  @Query(
      """
       SELECT qs
       FROM QuestionSet qs
       LEFT JOIN FETCH qs.questions
       LEFT JOIN FETCH qs.sources
       WHERE qs.id = :id
       AND qs.owner.id = :memberId
       AND qs.status = 'COMPLETE'
      """)
  Optional<QuestionSet> findByIdWithQuestionsForSolve(
      @Param("id") Long id, @Param("memberId") Long memberId);

  @Query(
      """
      SELECT qs
      FROM QuestionSet qs
      WHERE qs.owner.id = :memberId
      """)
  List<QuestionSet> findByMemberId(@Param("memberId") Long memberId);

  @Query(
      """
        SELECT DISTINCT qs
        FROM QuestionSet qs
        LEFT JOIN FETCH qs.questions q
        JOIN q.wrongAnswer wa
        WHERE qs.id = :id
        AND wa.member.id = :memberId
        AND wa.isReviewed = false
        AND qs.status = 'COMPLETE'
      """)
  Optional<QuestionSet> findWrongAnswersByIdAndMemberId(
      @Param("id") Long id, @Param("memberId") Long memberId);

  Optional<QuestionSet> findByIdAndOwnerId(Long id, Long ownerId);

  @Query(
      """
        SELECT qs
        FROM QuestionSet qs
        WHERE qs.id = :id
        AND qs.owner.id = :memberId
        AND qs.status != 'COMPLETE'
      """)
  Optional<QuestionSet> findQuestionSetWhenHaveNoQuestionsYet(Long id, Long memberId);

  @Query(
      """
        SELECT qs
        FROM QuestionSet qs
        WHERE qs.owner.id = :memberId
        AND (:cursor IS NULL OR qs.id < :cursor)
        ORDER BY qs.createdAt DESC, qs.id DESC
      """)
  List<QuestionSet> findByMemberIdWithCursor(
      @Param("memberId") Long memberId, @Param("cursor") Long cursor, Pageable pageable);
}
