package kr.it.pullit.modules.questionset.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.exception.SourceNotFoundException;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    return questionSetRepository.findQuestionSetWhenHaveNoQuestionsYet(id, memberId).orElseThrow(()->
        QuestionSetNotFoundException.byId(id));
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
            .findByIdWithQuestionsForFirstSolving(id, memberId)
            .orElseThrow(() -> handleQuestionSetNotFound(id, memberId));

    return new QuestionSetResponse(questionSet);
  }

  private QuestionSetResponse getQuestionSetForReviewing(Long id, Long memberId) {
    return questionSetRepository
        .findQuestionSetForReviewing(id, memberId)
        .map(QuestionSetResponse::new)
        .orElseThrow(
            () -> {
              QuestionSet qs =
                  questionSetRepository
                      .findByIdAndMemberId(id, memberId)
                      .orElseThrow(() -> QuestionSetNotFoundException.byId(id));

              if (qs.getStatus() != QuestionSetStatus.COMPLETE) {
                return handleQuestionSetStatusException(qs);
              }

              return WrongAnswerNotFoundException.withMessage("복습할 오답이 없습니다.");
            });
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
    List<Source> sources = sourcePublicApi.findByIdIn(request.sourceIds());

    if (sources.isEmpty()) {
      throw SourceNotFoundException.withMessage("소스가 존재하지 않습니다.");
    }

    Member owner =
        memberPublicApi.findById(ownerId).orElseThrow(() -> MemberNotFoundException.byId(ownerId));

    Set<Source> sourceSet = new HashSet<>(sources);
    String title = sources.getFirst().getOriginalName();

    QuestionSet questionSet =
        new QuestionSet(
            owner, sourceSet, title, request.difficulty(), request.type(), request.questionCount());

    QuestionSet savedQuestionSet = questionSetRepository.save(questionSet);

    eventPublisher.publishEvent(new QuestionSetCreatedEvent(savedQuestionSet.getId(), ownerId));

    return new QuestionSetResponse(savedQuestionSet);
  }

  @Override
  @Transactional(readOnly = true)
  public List<MyQuestionSetsResponse> getMemberQuestionSets(Long memberId) {
    memberPublicApi.findById(memberId).orElseThrow(() -> MemberNotFoundException.byId(memberId));

    List<QuestionSet> questionSets = questionSetRepository.findByMemberId(memberId);
    List<MyQuestionSetsResponse> myQuestionSetsResponses = new ArrayList<>();

    for (QuestionSet questionSet : questionSets) {
      List<Long> sourceIds = questionSet.getSources().stream().map(Source::getId).toList();
      List<String> sourceNames =
          questionSet.getSources().stream().map(Source::getOriginalName).toList();
      MyQuestionSetsResponse response =
          MyQuestionSetsResponse.builder()
              .questionSetId(questionSet.getId())
              .title(questionSet.getTitle())
              .sourceIds(sourceIds)
              .sourceNames(sourceNames)
              .questionCount(questionSet.getQuestionLength())
              .difficultyType(questionSet.getDifficulty())
              .questionType(questionSet.getType())
              .createdAt(questionSet.getCreatedAt())
              .build();
      myQuestionSetsResponses.add(response);
    }

    return myQuestionSetsResponses;
  }

  @Override
  @Transactional
  public void updateStatus(Long questionSetId, QuestionSetStatus status) {
    QuestionSet questionSet =
        questionSetRepository
            .findById(questionSetId)
            .orElseThrow(() -> QuestionSetNotFoundException.byId(questionSetId));
    questionSet.updateStatus(status);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<QuestionSet> findEntityByIdAndMemberId(Long id, Long memberId) {
    return questionSetRepository.findByIdWithoutQuestions(id, memberId);
  }
}
