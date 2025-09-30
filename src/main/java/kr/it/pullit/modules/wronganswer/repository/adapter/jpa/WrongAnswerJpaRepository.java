package kr.it.pullit.modules.wronganswer.repository.adapter.jpa;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.wronganswer.domain.entity.WrongAnswer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WrongAnswerJpaRepository extends JpaRepository<WrongAnswer, Long> {
  Optional<WrongAnswer> findByMemberIdAndQuestionId(Long memberId, Long questionId);

  @Query(
      """
        SELECT q.questionSet, COUNT(q.id)
        FROM WrongAnswer wa
        JOIN wa.question q
        WHERE wa.member.id = :memberId
        GROUP BY q.questionSet
        ORDER BY MAX(wa.createdAt) DESC, q.questionSet.id DESC
      """)
  List<Object[]> findAllWrongAnswerQuestionSetAndCountByMemberId(@Param("memberId") Long memberId);

  @Query(
      """
        SELECT q.questionSet, COUNT(q.id)
        FROM WrongAnswer wa
        JOIN wa.question q
        WHERE wa.member.id = :memberId
          AND (:cursor IS NULL OR q.questionSet.id < :cursor)
        GROUP BY q.questionSet
        ORDER BY MAX(wa.createdAt) DESC, q.questionSet.id DESC
      """)
  List<Object[]> findWrongAnswerQuestionSetWithCursor(
      @Param("memberId") Long memberId, @Param("cursor") Long cursor, Pageable pageable);
}
