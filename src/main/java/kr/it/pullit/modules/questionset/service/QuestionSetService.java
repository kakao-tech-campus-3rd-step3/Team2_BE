package kr.it.pullit.modules.questionset.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.domain.enums.QuestionSetStatus;
import kr.it.pullit.modules.questionset.domain.event.QuestionSetCreatedEvent;
import kr.it.pullit.modules.questionset.repository.QuestionSetRepository;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetCreateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsResponse;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;
import kr.it.pullit.modules.wronganswer.api.WrongAnswerPublicApi;
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
  private final WrongAnswerPublicApi wrongAnswerPublicApi;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  public QuestionSetResponse getQuestionSet(Long id, Long memberId, Boolean isReviewing) {
    if (isReviewing) {
      return getQuestionSetForReviewing(id, memberId);
    }
    return getQuestionSetForSolving(id, memberId);
  }

  private QuestionSetResponse getQuestionSetForSolving(Long id, Long memberId) {
    QuestionSet questionSet = questionSetRepository
        .findByIdWithQuestionsForSolve(id)
        .orElseThrow(() -> {
          // COMPLETE가 아닌 경우 상태 확인
          QuestionSet qs = questionSetRepository.findById(id)
              .orElseThrow(() -> new NotFoundException("문제집을 찾을 수 없습니다."));

          return switch (qs.getStatus()) {
            case PENDING -> new QuestionSetNotReadyException("문제집이 아직 생성 중입니다.");
            case FAILED -> new QuestionSetFailedException("문제집 생성에 실패했습니다.");
            default -> new NotFoundException("문제집을 찾을 수 없습니다.");
          };
        });

    return new QuestionSetResponse(questionSet);
  }

  private QuestionSetResponse getQuestionSetForReviewing(Long id, Long memberId) {
    QuestionSet questionSet = questionSetRepository
        .findWrongAnswersById(id, memberId)
        .orElseThrow(() -> {
          // COMPLETE가 아니거나 복습할 오답이 없는 경우
          QuestionSet qs = questionSetRepository.findById(id)
              .orElseThrow(() -> new NotFoundException("문제집을 찾을 수 없습니다."));

          if (qs.getStatus() != QuestionSetStatus.COMPLETE) {
            return switch (qs.getStatus()) {
              case PENDING -> new QuestionSetNotReadyException("문제집이 아직 생성 중입니다.");
              case FAILED -> new QuestionSetFailedException("문제집 생성에 실패했습니다.");
              default -> new NotFoundException("문제집을 찾을 수 없습니다.");
            };
          }

          return new NotFoundException("복습할 오답이 없습니다.");
        });

    return new QuestionSetResponse(questionSet);
  }


  @Transactional
  public QuestionSetResponse create(QuestionSetCreateRequestDto request, Long ownerId) {
    List<Source> sources = sourcePublicApi.findByIdIn(request.sourceIds());

    if (sources.isEmpty()) {
      throw new IllegalArgumentException("소스가 존재하지 않습니다.");
    }

    Member owner =
        memberPublicApi
            .findById(ownerId)
            .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다"));

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
    Member member =
        memberPublicApi
            .findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("맴버를 찾을 수 없습니다."));

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
            .orElseThrow(
                () -> new IllegalArgumentException("문제집을 찾을 수 없습니다. ID: " + questionSetId));
    questionSet.updateStatus(status);
  }

  @Override
  public Optional<QuestionSet> findEntityByIdAndMemberId(Long id, Long memberId) {
    return questionSetRepository.findByIdWithoutQuestions(id, memberId);
  }
}
