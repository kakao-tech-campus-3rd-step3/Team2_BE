package kr.it.pullit.modules.projection.learnstats.query;

import kr.it.pullit.modules.projection.learnstats.domain.LearnStatsProjection;
import kr.it.pullit.modules.projection.learnstats.repository.LearnStatsProjectionRepository;
import kr.it.pullit.modules.projection.learnstats.web.dto.LearnStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LearnStatsQueryService {

  private final LearnStatsProjectionRepository learnStatsProjectionRepository;

  public LearnStatsResponse getLearnStats(Long memberId) {
    LearnStatsProjection projection =
        learnStatsProjectionRepository
            .findById(memberId)
            .orElseGet(() -> LearnStatsProjection.newOf(memberId));

    return LearnStatsResponse.from(projection);
  }
}
