import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;

public interface QuestionSetJpaRepository extends JpaRepository<QuestionSet, Long> {

  long countByOwnerIdAndDeletedFalse(Long memberId);

  @Query(
      "SELECT SUM(q.questionCount) FROM QuestionSet q WHERE q.owner.id = :memberId AND q.lastMarkingResult.completed = true AND q.lastMarkingResult.completedDateTime BETWEEN :start AND :end")
  Long countCompletedQuestionsByMemberIdAndDateBetween(
      @Param("memberId") Long memberId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  @Query(
      "SELECT DISTINCT q.lastMarkingResult.completedDateTime FROM QuestionSet q WHERE q.owner.id = :memberId AND q.lastMarkingResult.completed = true ORDER BY q.lastMarkingResult.completedDateTime ASC")
  List<LocalDateTime> findCompletedDatesByMemberId(@Param("memberId") Long memberId);
}
