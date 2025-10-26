package kr.it.pullit.modules.projection.learnstats.service;

import java.util.List;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsRecalibrationPublicApi;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;
import kr.it.pullit.modules.projection.learnstats.repository.LearnStatsRepository;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LearnStatsRecalibrationService implements LearnStatsRecalibrationPublicApi {

  private final MemberPublicApi memberPublicApi;
  private final QuestionSetPublicApi questionSetPublicApi;
  private final LearnStatsRepository learnStatsRepository;

  @Override
  public void recalibrateAllMembers() {
    log.info("모든 회원의 총 푼 문제 수 보정 작업을 시작합니다.");

    Pageable pageable = PageRequest.of(0, 100);
    Page<Member> memberPage;

    do {
      memberPage = memberPublicApi.findAll(pageable);
      memberPage.getContent().forEach(member -> recalibrateMember(member.getId()));
      pageable = memberPage.nextPageable();
    } while (memberPage.hasNext());

    log.info("모든 회원의 총 푼 문제 수 보정 작업을 완료했습니다.");
  }

  private void recalibrateMember(Long memberId) {
    List<QuestionSet> completedSets =
        questionSetPublicApi.findCompletedEntitiesByMemberId(memberId);

    long realTotalCount =
        completedSets.stream().mapToLong(questionSet -> questionSet.getQuestions().size()).sum();

    LearnStats stats =
        learnStatsRepository.findById(memberId).orElseGet(() -> LearnStats.newOf(memberId));
    stats.updateTotalSolvedQuestionCount(realTotalCount);
    learnStatsRepository.save(stats);

    log.info("{}번 회원의 총 푼 문제 수를 {}개로 보정했습니다.", memberId, realTotalCount);
  }
}
