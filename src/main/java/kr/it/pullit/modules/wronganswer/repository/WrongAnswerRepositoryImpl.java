package kr.it.pullit.modules.wronganswer.repository;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.wronganswer.domain.entity.WrongAnswer;
import kr.it.pullit.modules.wronganswer.repository.adapter.jpa.WrongAnswerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WrongAnswerRepositoryImpl implements WrongAnswerRepository {
  private final WrongAnswerJpaRepository wrongAnswerJpaRepository;

  @Override
  public WrongAnswer save(WrongAnswer wrongAnswer) {
    return wrongAnswerJpaRepository.save(wrongAnswer);
  }

  @Override
  public Optional<WrongAnswer> findByMemberIdAndQuestionId(Long memberId, Long questionId) {
    return wrongAnswerJpaRepository.findByMemberIdAndQuestionId(memberId, questionId);
  }

  @Override
  public List<WrongAnswer> saveAll(Iterable<WrongAnswer> entities) {
    return wrongAnswerJpaRepository.saveAll(entities);
  }

  @Override
  public List<Object[]> findAllWrongAnswerQuestionSetAndCountByMemberId(Long memberId) {
    return wrongAnswerJpaRepository.findAllWrongAnswerQuestionSetAndCountByMemberId(memberId);
  }

  @Override
  public List<Object[]> findWrongAnswerQuestionSetWithCursor(
      Long memberId, Long cursor, Pageable pageable) {
    return wrongAnswerJpaRepository.findWrongAnswerQuestionSetWithCursor(
        memberId, cursor, pageable);
  }
}
