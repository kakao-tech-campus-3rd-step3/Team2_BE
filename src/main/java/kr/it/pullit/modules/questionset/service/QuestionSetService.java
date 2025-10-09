package kr.it.pullit.modules.questionset.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.exception.MemberNotFoundException;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.domain.enums.QuestionSetStatus;
import kr.it.pullit.modules.questionset.domain.event.QuestionSetCreatedEvent;
import kr.it.pullit.modules.questionset.exception.QuestionSetFailedException;
import kr.it.pullit.modules.questionset.exception.QuestionSetNotFoundException;
import kr.it.pullit.modules.questionset.exception.QuestionSetNotReadyException;
import kr.it.pullit.modules.questionset.repository.QuestionSetRepository;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetCreateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsResponse;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;
import kr.it.pullit.modules.wronganswer.exception.WrongAnswerNotFoundException;
import kr.it.pullit.shared.error.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionSetService implements QuestionSetPublicApi {

  private final QuestionSetRepository questionSetRepository;
  private final SourcePublicApi sourcePublicApi;
  private final MemberPublicApi memberPublicApi;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional(readOnly = true)
  public QuestionSetResponse getQuestionSetWhenHaveNoQuestionsYet(Long id, Long memberId) {
    return questionSetRepository.findQuestionSetWhenHaveNoQuestionsYet(id, memberId)
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
        questionSetRepository.findByIdWithQuestionsForFirstSolving(id, memberId)
            .orElseThrow(() -> handleQuestionSetNotFound(id, memberId));

    return QuestionSetResponse.from(questionSet);
  }

  private QuestionSetResponse getQuestionSetForReviewing(Long id, Long memberId) {
    return questionSetRepository.findQuestionSetForReviewing(id, memberId)
        .map(QuestionSetResponse::from).orElseThrow(() -> handleReviewSetNotFound(id, memberId));
  }

  private RuntimeException handleReviewSetNotFound(Long id, Long memberId) {
    QuestionSet qs = questionSetRepository.findByIdAndMemberId(id, memberId)
        .orElseThrow(() -> QuestionSetNotFoundException.byId(id));

    if (qs.getStatus() != QuestionSetStatus.COMPLETE) {
      return handleQuestionSetStatusException(qs);
    }

    return WrongAnswerNotFoundException.withMessage("복습할 오답이 없습니다.");
  }

  private RuntimeException handleQuestionSetNotFound(Long id, Long memberId) {
    return questionSetRepository.findByIdAndMemberId(id, memberId)
        .map(this::handleQuestionSetStatusException).orElse(QuestionSetNotFoundException.byId(id));
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
    Member owner =
        memberPublicApi.findById(ownerId).orElseThrow(() -> MemberNotFoundException.byId(ownerId));
    List<Source> sources = sourcePublicApi.findByIdIn(request.sourceIds());

    QuestionSet questionSet = QuestionSet.create(owner, sources, request);

    QuestionSet savedQuestionSet = questionSetRepository.save(questionSet);

    eventPublisher.publishEvent(QuestionSetCreatedEvent.from(savedQuestionSet));

    return QuestionSetResponse.from(savedQuestionSet);
  }

  @Override
  @Transactional(readOnly = true)
  public List<MyQuestionSetsResponse> getMemberQuestionSets(Long memberId) {
    memberPublicApi.findById(memberId).orElseThrow(() -> MemberNotFoundException.byId(memberId));
    List<QuestionSet> questionSets = questionSetRepository.findByMemberId(memberId);
    return questionSets.stream().map(MyQuestionSetsResponse::from).toList();
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
  @Transactional(readOnly = true)
  public Optional<QuestionSet> findEntityByIdAndMemberId(Long id, Long memberId) {
    return questionSetRepository.findByIdWithoutQuestions(id, memberId);
  }

  private QuestionSet findQuestionSetOrThrow(Long questionSetId) {
    return questionSetRepository.findById(questionSetId)
        .orElseThrow(() -> QuestionSetNotFoundException.byId(questionSetId));
  }
}
