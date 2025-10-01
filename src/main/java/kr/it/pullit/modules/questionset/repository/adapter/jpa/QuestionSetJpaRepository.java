package kr.it.pullit.modules.questionset.repository.adapter.jpa;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionSetJpaRepository extends JpaRepository<QuestionSet, Long> {

  //TODO: 회원 함께 조인해서 가져오는 식으로 변경 필요
  @Query("""
          SELECT qs
          FROM QuestionSet qs
          LEFT JOIN FETCH qs.questions
          WHERE qs.id = :id
          AND qs.owner.id = :memberId
         """)
  Optional<QuestionSet> findByIdWithQuestions(@Param("id") Long id, @Param("memberId") Long memberId);

  @Query("""
          SELECT qs
          FROM QuestionSet qs
          LEFT JOIN FETCH qs.questions
          WHERE qs.id = :id
          AND qs.owner.id = :memberId
  """)
  Optional<QuestionSet> findByIdWithQuestionsForSolve(@Param("id") Long id, @Param("memberId") Long memberId);

  @Query("""
         SELECT qs
         FROM QuestionSet qs
         WHERE qs.owner.id = :memberId
         """)
  List<QuestionSet> findByMemberId(@Param("memberId") Long memberId);

  @Query("""
          SELECT DISTINCT qs
          FROM QuestionSet qs
          LEFT JOIN FETCH qs.questions q
          LEFT JOIN q.wrongAnswer wa
          WHERE qs.id = :id
          AND wa.member.id = :memberId
          AND wa.isReviewed = false
        """)
  Optional<QuestionSet> findWrongAnswersByIdAndMemberId(
      @Param("id") Long id,
      @Param("memberId") Long memberId);

  Optional<QuestionSet> findQuestionSetByIdAndOwner_Id(Long id, Long ownerId);
}
