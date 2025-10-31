package kr.it.pullit.modules.wronganswer.repository.adapter.jpa;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.wronganswer.domain.entity.WrongAnswer;
import kr.it.pullit.modules.wronganswer.service.dto.WrongAnswerSetDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WrongAnswerJpaRepository extends JpaRepository<WrongAnswer, Long> {
  Optional<WrongAnswer> findByMemberIdAndQuestionId(Long memberId, Long questionId);

  List<WrongAnswer> findByMemberIdAndQuestionIdIn(Long memberId, List<Long> questionIds);

  @Query(
      """
            SELECT new kr.it.pullit.modules.wronganswer.service.dto.WrongAnswerSetDto(
              q.questionSet, COUNT(q.id), MAX(wa.id))
            FROM WrongAnswer wa
            JOIN wa.question q
            WHERE wa.memberId = :memberId AND wa.isReviewed = false
            GROUP BY q.questionSet
            HAVING COUNT(q.id) > 0
            ORDER BY MAX(wa.createdAt) DESC, q.questionSet.id DESC
          """)
  List<WrongAnswerSetDto> findAllWrongAnswerSetAndCountByMemberId(@Param("memberId") Long memberId);

  @Query(
      """
            SELECT new kr.it.pullit.modules.wronganswer.service.dto.WrongAnswerSetDto(
              q.questionSet, COUNT(q.id), MAX(wa.id))
            FROM WrongAnswer wa
            JOIN wa.question q
            WHERE wa.memberId = :memberId
            AND wa.isReviewed = false
            GROUP BY q.questionSet
            HAVING COUNT(q.id) > 0 AND (:cursor IS NULL OR MAX(wa.id) < :cursor)
            ORDER BY MAX(wa.createdAt) DESC, q.questionSet.id DESC
          """)
  List<WrongAnswerSetDto> findWrongAnswerSetWithCursor(
      @Param("memberId") Long memberId, @Param("cursor") Long cursor, Pageable pageable);
}
