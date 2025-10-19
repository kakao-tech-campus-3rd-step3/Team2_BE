package kr.it.pullit.modules.questionset.repository;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.repository.adapter.jpa.QuestionSetJpaRepository;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
  public Optional<QuestionSet> findByIdWithQuestionsForFirstSolving(Long id, Long memberId) {
    return questionSetJpaRepository.findByIdWithQuestionsForSolve(id, memberId);
  }

  @Override
  public Optional<QuestionSet> findByIdWithoutQuestions(Long id, Long memberId) {
    return questionSetJpaRepository.findByIdAndOwnerId(id, memberId);
  }

  @Override
  public QuestionSet save(QuestionSet questionSet) {
    return questionSetJpaRepository.save(questionSet);
  }

  @Override
  public Optional<QuestionSet> findQuestionSetForReviewing(Long id, Long memberId) {
    return questionSetJpaRepository.findWrongAnswersByIdAndMemberId(id, memberId);
  }

  @Override
  public Optional<QuestionSetResponse> findQuestionSetWhenHaveNoQuestionsYet(
      Long id, Long memberId) {
    return questionSetJpaRepository
        .findQuestionSetWhenHaveNoQuestionsYet(id, memberId)
        .map(QuestionSetResponse::new);
  }

  @Override
  public List<QuestionSet> findByMemberId(Long memberId) {
    return questionSetJpaRepository.findByMemberId(memberId);
  }

  @Override
  public List<QuestionSet> findByMemberIdWithCursor(Long memberId, Long cursor, Pageable pageable) {
    return questionSetJpaRepository.findByMemberIdWithCursor(memberId, cursor, pageable);
  }

  @Override
  public void delete(QuestionSet questionSet) {
    questionSetJpaRepository.delete(questionSet);
  }
}
