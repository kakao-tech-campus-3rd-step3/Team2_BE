package kr.it.pullit.modules.projection.learnstats.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import kr.it.pullit.support.annotation.JpaSliceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@JpaSliceTest
@Import(H2LearnStatsDailyRepositoryImpl.class)
class LearnStatsDailyJpaRepositoryTest {

  @Autowired private LearnStatsDailyRepository learnStatsDailyRepository;

  @Test
  void upsertAdd() {
    Long memberId = 1L;
    LocalDate activityDate = LocalDate.now();
    int questionsDelta = 5;
    int questionSetsDelta = 2;

    int result =
        learnStatsDailyRepository.upsertAdd(
            memberId, activityDate, questionsDelta, questionSetsDelta);

    assertEquals(1, result);
  }
}
