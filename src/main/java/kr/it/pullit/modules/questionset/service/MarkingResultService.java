package kr.it.pullit.modules.questionset.service;

import kr.it.pullit.modules.questionset.api.MarkingResultPublicApi;
import kr.it.pullit.modules.questionset.repository.MarkingResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MarkingResultService implements MarkingResultPublicApi {

  private final MarkingResultRepository markingResultRepository;

  @Override
  public long countTotalAttemptedQuestionsByMemberId(Long memberId) {
    return markingResultRepository.countDistinctQuestionByMemberId(memberId);
  }

  @Override
  public long countTotalCorrectQuestionsByMemberId(Long memberId) {
    return markingResultRepository.countByMemberIdAndIsCorrectIsTrue(memberId);
  }

  @Override
  public long countCorrectAnswersByMemberId(Long memberId) {
    return markingResultRepository.countCorrectAnswersByMemberId(memberId);
  }
}
