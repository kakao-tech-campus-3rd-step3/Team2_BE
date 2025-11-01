package kr.it.pullit.modules.questionset.service;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.commonfolder.api.CommonFolderPublicApi;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.constant.SourceStatus;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.exception.MemberNotFoundException;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.domain.dto.QuestionSetCreateParam;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.enums.QuestionSetStatus;
import kr.it.pullit.modules.questionset.event.QuestionSetCreatedEvent;
import kr.it.pullit.modules.questionset.exception.QuestionSetFailedException;
import kr.it.pullit.modules.questionset.exception.QuestionSetNotFoundException;
import kr.it.pullit.modules.questionset.exception.QuestionSetNotReadyException;
import kr.it.pullit.modules.questionset.exception.QuestionSetUnauthorizedException;
import kr.it.pullit.modules.questionset.exception.SourceNotReadyException;
import kr.it.pullit.modules.questionset.repository.QuestionSetRepository;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetCreateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetUpdateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsResponse;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;
import kr.it.pullit.modules.wronganswer.exception.WrongAnswerNotFoundException;
import kr.it.pullit.shared.error.BusinessException;
import kr.it.pullit.shared.event.EventPublisher;
import kr.it.pullit.shared.paging.dto.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestionSetService implements QuestionSetPublicApi {

  private final QuestionSetRepository questionSetRepository;
  private final CommonFolderPublicApi commonFolderPublicApi;
  private final SourcePublicApi sourcePublicApi;
  private final MemberPublicApi memberPublicApi;
  private final EventPublisher eventPublisher;

  @Override
  @Transactional(readOnly = true)
  public QuestionSetResponse getQuestionSetWhenHaveNoQuestionsYet(Long id, Long memberId) {
    return questionSetRepository
        .findQuestionSetWhenHaveNoQuestionsYet(id, memberId)
        .orElseThrow(() -> QuestionSetNotFoundException.byId(id));
  }

  @Override
  @Transactional(readOnly = true)
  public QuestionSetResponse getQuestionSetForSolving(Long id, Long memberId, Boolean isReviewing) {
    if (isReviewing) {
      return getQuestionSetForReviewing(id, memberId);
    }
    return getQuestionSetForFirstSolving(id, memberId);
  }

  private QuestionSetResponse getQuestionSetForFirstSolving(Long id, Long memberId) {
    QuestionSet questionSet =
        questionSetRepository
            .findWithQuestionsForFirstSolving(id, memberId)
            .orElseThrow(() -> handleQuestionSetNotFound(id, memberId));

    return QuestionSetResponse.from(questionSet);
  }

  private QuestionSetResponse getQuestionSetForReviewing(Long id, Long memberId) {
    return questionSetRepository
        .findQuestionSetForReviewing(id, memberId)
        .map(QuestionSetResponse::from)
        .orElseThrow(() -> handleReviewSetNotFound(id, memberId));
  }

  private RuntimeException handleReviewSetNotFound(Long id, Long memberId) {
    QuestionSet qs =
        questionSetRepository
            .findByIdAndMemberId(id, memberId)
            .orElseThrow(() -> QuestionSetNotFoundException.byId(id));

    if (qs.getStatus() != QuestionSetStatus.COMPLETE) {
      return handleQuestionSetStatusException(qs);
    }

    return WrongAnswerNotFoundException.noWrongAnswersToReview();
  }

  private RuntimeException handleQuestionSetNotFound(Long id, Long memberId) {
    return questionSetRepository
        .findByIdAndMemberId(id, memberId)
        .map(this::handleQuestionSetStatusException)
        .orElse(QuestionSetNotFoundException.byId(id));
  }

  private BusinessException handleQuestionSetStatusException(QuestionSet qs) {
    return switch (qs.getStatus()) {
      case PENDING -> QuestionSetNotReadyException.byId(qs.getId());
      case FAILED -> QuestionSetFailedException.byId(qs.getId());
      default -> QuestionSetNotFoundException.byId(qs.getId());
    };
  }

  @Transactional
  public QuestionSetResponse create(QuestionSetCreateRequestDto request, Long ownerId) {
    memberPublicApi.findById(ownerId).orElseThrow(() -> MemberNotFoundException.byId(ownerId));
    List<Source> sources = sourcePublicApi.findByIdIn(request.sourceIds());

    validateAllSourcesAreReady(sources);

    QuestionSetCreateParam createParam = QuestionSetCreateParam.from(request);

    QuestionSet questionSet = QuestionSet.create(ownerId, sources, createParam);

    assignFolderToQuestionSet(request.commonFolderId(), questionSet, ownerId);

    QuestionSet savedQuestionSet = questionSetRepository.save(questionSet);

    eventPublisher.publish(QuestionSetCreatedEvent.from(savedQuestionSet));

    return QuestionSetResponse.from(savedQuestionSet);
  }

  @Override
  @Transactional(readOnly = true)
  public long countByFolderId(Long folderId) {
    return questionSetRepository.countByCommonFolderId(folderId);
  }

  private void assignFolderToQuestionSet(Long folderId, QuestionSet questionSet, Long ownerId) {
    if (folderId != null) {
      CommonFolder folder =
          commonFolderPublicApi
              .findFolderEntityById(ownerId, folderId)
              .orElseThrow(() -> new IllegalArgumentException("해당 ID의 폴더를 찾을 수 없습니다."));
      questionSet.assignToFolder(folder);
    } else {
      CommonFolder defaultFolder =
          commonFolderPublicApi.getOrCreateDefaultQuestionSetFolder(ownerId);
      questionSet.assignToFolder(defaultFolder);
    }
  }

  private void validateAllSourcesAreReady(List<Source> sources) {
    List<Source> notReadySources =
        sources.stream().filter(s -> s.getStatus() != SourceStatus.READY).toList();

    if (!notReadySources.isEmpty()) {
      // 여기에서 예외를 발생시켜 사용자에게 즉시 피드백을 줍니다.
      throw new SourceNotReadyException(notReadySources);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public CursorPageResponse<MyQuestionSetsResponse> getMemberQuestionSets(
      Long memberId, Long cursor, int size) {
    memberPublicApi.findById(memberId).orElseThrow(() -> MemberNotFoundException.byId(memberId));
    List<QuestionSet> results =
        questionSetRepository.findByMemberIdWithCursorAndNextPageCheck(memberId, cursor, size);
    List<MyQuestionSetsResponse> myQuestionSetsResponses =
        results.stream().map(MyQuestionSetsResponse::from).toList();
    return CursorPageResponse.of(
        myQuestionSetsResponses, size, MyQuestionSetsResponse::questionSetId);
  }

  @Override
  @Transactional(readOnly = true)
  public CursorPageResponse<MyQuestionSetsResponse> getMemberQuestionSets(
      Long memberId, Long cursor, int size, Long folderId) {
    memberPublicApi.findById(memberId).orElseThrow(() -> MemberNotFoundException.byId(memberId));

    List<QuestionSet> results =
        questionSetRepository.findByMemberIdAndFolderIdWithCursorAndNextPageCheck(
            memberId, folderId, cursor, size);

    List<MyQuestionSetsResponse> myQuestionSetsResponses =
        results.stream().map(MyQuestionSetsResponse::from).toList();

    return CursorPageResponse.of(
        myQuestionSetsResponses, size, MyQuestionSetsResponse::questionSetId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<MyQuestionSetsResponse> getMemberQuestionSets(Long memberId) {
    memberPublicApi.findById(memberId).orElseThrow(() -> MemberNotFoundException.byId(memberId));
    List<QuestionSet> questionSets = questionSetRepository.findByMemberId(memberId);
    return questionSets.stream().map(MyQuestionSetsResponse::from).toList();
  }

  @Override
  public long countByMemberId(Long memberId) {
    return questionSetRepository.countByOwnerId(memberId);
  }

  @Override
  @Transactional
  public void markAsComplete(Long questionSetId) {
    QuestionSet questionSet = findQuestionSetOrThrow(questionSetId);
    questionSet.completeProcessing();
  }

  @Override
  @Transactional
  public void markAsFailed(Long questionSetId) {
    QuestionSet questionSet = findQuestionSetOrThrow(questionSetId);
    questionSet.failProcessing();
  }

  @Override
  @Transactional
  public void update(Long questionSetId, QuestionSetUpdateRequestDto request, Long memberId) {
    QuestionSet questionSet = findQuestionSetByIdAndMemberIdOrThrow(questionSetId, memberId);

    if (request.title() != null) {
      questionSet.updateTitle(request.title());
    }

    if (request.commonFolderId() != null) {
      assignFolderToQuestionSet(request.commonFolderId(), questionSet, memberId);
    }
  }

  @Override
  @Transactional
  public void deleteAllByFolderId(Long folderId) {
    List<QuestionSet> questionSetsToDelete =
        questionSetRepository.findAllByCommonFolderId(folderId);
    List<Long> questionSetIds = questionSetsToDelete.stream().map(QuestionSet::getId).toList();
    questionSetRepository.deleteAllByIds(questionSetIds);
  }

  @Override
  @Transactional
  public void relocateQuestionSetsToDefaultFolder(Long memberId, Long folderId) {
    CommonFolder defaultFolder =
        commonFolderPublicApi.getOrCreateDefaultQuestionSetFolder(memberId);
    List<QuestionSet> questionSets = questionSetRepository.findAllByCommonFolderId(folderId);
    questionSets.forEach(questionSet -> questionSet.assignToFolder(defaultFolder));
  }

  @Override
  public List<QuestionSet> findAllByFolderId(Long folderId) {
    return questionSetRepository.findAllByCommonFolderId(folderId);
  }

  @Override
  @Transactional
  public void delete(Long questionSetId, Long memberId) {
    QuestionSet questionSet =
        questionSetRepository
            .findById(questionSetId)
            .orElseThrow(() -> QuestionSetNotFoundException.byId(questionSetId));

    if (!questionSet.getOwnerId().equals(memberId)) {
      throw QuestionSetUnauthorizedException.byId(questionSetId);
    }

    questionSetRepository.deleteById(questionSet.getId());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<QuestionSet> findEntityByIdAndMemberId(Long id, Long memberId) {
    return questionSetRepository.findWithoutQuestions(id, memberId);
  }

  @Override
  public long countCompletedQuestionsByMemberId(Long memberId) {
    return questionSetRepository.countCompletedQuestionsByMemberId(memberId);
  }

  private QuestionSet findQuestionSetByIdAndMemberIdOrThrow(Long questionSetId, Long memberId) {
    QuestionSet questionSet = findQuestionSetOrThrow(questionSetId);

    if (!questionSet.getOwnerId().equals(memberId)) {
      throw QuestionSetUnauthorizedException.byId(questionSetId);
    }
    return questionSet;
  }

  private QuestionSet findQuestionSetOrThrow(Long questionSetId) {
    return questionSetRepository
        .findById(questionSetId)
        .orElseThrow(() -> QuestionSetNotFoundException.byId(questionSetId));
  }
}
