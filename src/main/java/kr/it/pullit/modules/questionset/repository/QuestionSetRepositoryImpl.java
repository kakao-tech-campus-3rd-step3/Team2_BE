package kr.it.pullit.modules.questionset.repository;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.repository.adapter.jpa.QuestionSetJpaRepository;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
  public Optional<QuestionSet> findWithQuestionsForFirstSolving(Long id, Long memberId) {
    return questionSetJpaRepository.findByIdWithQuestionsForSolve(id, memberId);
  }

  @Override
  public Optional<QuestionSet> findWithoutQuestions(Long id, Long memberId) {
    return questionSetJpaRepository.findByIdAndOwnerId(id, memberId);
  }

  @Override
  public QuestionSet save(QuestionSet questionSet) {
    return questionSetJpaRepository.save(questionSet);
  }

  @Override
  public void deleteById(Long questionSetId) {
    questionSetJpaRepository.deleteById(questionSetId);
  }

  @Override
  public void deleteAllByIds(List<Long> questionSetIds) {
    questionSetJpaRepository.deleteAllById(questionSetIds);
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
  public List<QuestionSet> findByMemberIdAndFolderIdWithCursorAndNextPageCheck(
      Long memberId, Long folderId, Long cursor, int size) {
    PageRequest pageableWithOneExtra = PageRequest.of(0, size + 1);
    return questionSetJpaRepository.findByMemberIdAndFolderIdWithCursor(
        memberId, folderId, cursor, pageableWithOneExtra);
  }

  @Override
  public List<QuestionSet> findByMemberIdWithCursorAndNextPageCheck(
      Long memberId, Long cursor, int size) {
    PageRequest pageableWithOneExtra = PageRequest.of(0, size + 1);
    return questionSetJpaRepository.findByMemberIdWithCursor(
        memberId, cursor, pageableWithOneExtra);
  }

  @Override
  public long countByCommonFolderId(Long commonFolderId) {
    return questionSetJpaRepository.countByCommonFolderId(commonFolderId);
  }

  @Override
  public List<QuestionSet> findAllByCommonFolderId(Long commonFolderId) {
    return questionSetJpaRepository.findAllByCommonFolderId(commonFolderId);
  }

  @Override
  public List<QuestionSet> findCompletedByMemberId(Long memberId) {
    return questionSetJpaRepository.findCompletedWithQuestionsByMemberId(memberId);
  }

  @Override
  public long countByOwnerId(Long memberId) {
    return questionSetJpaRepository.countByOwnerId(memberId);
  }
}
