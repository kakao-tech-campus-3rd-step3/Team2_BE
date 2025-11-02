package kr.it.pullit.modules.projection.learnstats.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStatsDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LearnStatsDailyJpaRepository extends JpaRepository<LearnStatsDaily, Long> {

  Optional<LearnStatsDaily> findByMemberIdAndActivityDate(Long memberId, LocalDate activityDate);

  List<LearnStatsDaily> findByMemberIdAndActivityDateBetween(
      Long memberId, LocalDate from, LocalDate to);

  @Modifying
  @Query(
      value =
          """
      INSERT INTO learn_stats_daily
      (member_id, activity_date, solved_question_count, solved_question_set_count)
      VALUES (:memberId, :date, :d, :q)
      ON DUPLICATE KEY UPDATE
        solved_question_count = solved_question_count + :d,
        solved_question_set_count = solved_question_set_count + :q
      """,
      nativeQuery = true)
  int upsertAdd(
      @Param("memberId") Long memberId,
      @Param("date") LocalDate activityDate,
      @Param("d") int questionsDelta,
      @Param("q") int questionSetsDelta);
}
