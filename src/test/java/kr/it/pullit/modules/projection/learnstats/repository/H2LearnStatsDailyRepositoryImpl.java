package kr.it.pullit.modules.projection.learnstats.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Profile("test")
@Repository
@RequiredArgsConstructor
public class H2LearnStatsDailyRepositoryImpl implements LearnStatsDailyRepository {

  @PersistenceContext private final EntityManager em;

  @Override
  @Transactional
  public int upsertAdd(
      Long memberId, LocalDate activityDate, int questionsDelta, int questionSetsDelta) {
    String sql =
        """
            MERGE INTO learn_stats_daily
            (member_id, activity_date, solved_question_count, solved_question_set_count)
            KEY(member_id, activity_date)
            VALUES (:memberId, :activityDate,
                (SELECT COALESCE(MAX(solved_question_count), 0)
                  FROM learn_stats_daily WHERE member_id = :memberId
                  AND activity_date = :activityDate) + :questionsDelta,
                (SELECT COALESCE(MAX(solved_question_set_count), 0)
                  FROM learn_stats_daily WHERE member_id = :memberId
                  AND activity_date = :activityDate) + :questionSetsDelta)
            """;

    return em.createNativeQuery(sql)
        .setParameter("memberId", memberId)
        .setParameter("activityDate", activityDate)
        .setParameter("questionsDelta", questionsDelta)
        .setParameter("questionSetsDelta", questionSetsDelta)
        .executeUpdate();
  }
}
