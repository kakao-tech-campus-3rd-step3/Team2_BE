package kr.it.pullit.modules.questionset.repository;

import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.IncorrectAnswerQuestion;
import kr.it.pullit.modules.questionset.repository.adapter.jpa.IncorrectAnswerQuestionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class IncorrectAnswerQuestionRepositoryImpl implements IncorrectAnswerQuestionRepository {
  private final IncorrectAnswerQuestionJpaRepository incorrectAnswerQuestionJpaRepository;

  @Override
  public IncorrectAnswerQuestion save(IncorrectAnswerQuestion incorrectAnswerQuestion) {
    return incorrectAnswerQuestionJpaRepository.save(incorrectAnswerQuestion);
  }

  @Override
  public Optional<IncorrectAnswerQuestion> findByMemberIdAndQuestionId(
      Long memberId, Long questionId) {
    return incorrectAnswerQuestionJpaRepository.findByMemberIdAndQuestionId(memberId, questionId);
  }
}
