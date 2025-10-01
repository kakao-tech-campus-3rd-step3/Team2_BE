package kr.it.pullit.modules.questionset.repository;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.repository.adapter.jpa.QuestionSetJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class QuestionSetRepositoryImpl implements QuestionSetRepository {

  private final QuestionSetJpaRepository questionSetJpaRepository;

  @Override
  public Optional<QuestionSet> findByIdAndMemberId(Long id, Long memberId) {
    return questionSetJpaRepository.findByIdWithQuestions(id, memberId);
  }

  @Override
  public Optional<QuestionSet> findByIdWithoutQuestions(Long id, Long memberId) {
    return questionSetJpaRepository.findQuestionSetByIdAndOwner_Id(id, memberId);
  }

  @Override
  public QuestionSet save(QuestionSet questionSet) {
    return questionSetJpaRepository.save(questionSet);
  }

  @Override
  public Optional<QuestionSet> findWrongAnswersByIdAndMemberId(Long id, Long memberId) {
    return questionSetJpaRepository.findWrongAnswersByIdAndMemberId(id, memberId);
  }

  @Override
  public List<QuestionSet> findByMemberId(Long memberId) {
    return questionSetJpaRepository.findByMemberId(memberId);
  }
}
