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
  public Optional<QuestionSet> findById(Long id) {
    return questionSetJpaRepository.findById(id);
  }

  @Override
  public Optional<QuestionSet> findByIdAndMemberId(Long id, Long memberId) {
    return questionSetJpaRepository.findByIdAndMemberId(id, memberId);
  }

  @Override
  public Optional<QuestionSet> findByIdWithQuestionsForSolve(Long id, Long memberId) {
    return questionSetJpaRepository.findByIdWithQuestionsForSolve(id, memberId);
  }

  /** lazy loading을 위해 문제 목록을 제외한 문제집 메타데이터만 조회합니다. */
  @Override
  public Optional<QuestionSet> findByIdWithoutQuestions(Long id, Long memberId) {
    return questionSetJpaRepository.findByIdAndOwnerId(id, memberId);
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
