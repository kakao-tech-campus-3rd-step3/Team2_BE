package kr.it.pullit.modules.wronganswer.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.questionset.api.QuestionPublicApi;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.wronganswer.api.WrongAnswerPublicApi;
import kr.it.pullit.modules.wronganswer.domain.entity.WrongAnswer;
import kr.it.pullit.modules.wronganswer.repository.WrongAnswerRepository;
import kr.it.pullit.modules.wronganswer.service.dto.WrongAnswerSetDto;
import kr.it.pullit.modules.wronganswer.web.dto.WrongAnswerSetResponse;
import kr.it.pullit.shared.paging.dto.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WrongAnswerService implements WrongAnswerPublicApi {

  private final WrongAnswerRepository wrongAnswerRepository;
  private final MemberPublicApi memberPublicApi;
  private final QuestionPublicApi questionPublicApi;

  @Override
  @Transactional(readOnly = true)
  public CursorPageResponse<WrongAnswerSetResponse> getMyWrongAnswers(
      Long memberId, Long cursor, int size) {
    List<WrongAnswerSetDto> results = fetchWrongAnswerSets(memberId, cursor, size);

    boolean hasNext = results.size() > size;
    List<WrongAnswerSetResponse> content = toContent(results, size);
    Long nextCursor = calculateNextCursor(results, size, hasNext);

    return CursorPageResponse.of(content, nextCursor, hasNext);
  }

  private List<WrongAnswerSetDto> fetchWrongAnswerSets(Long memberId, Long cursor, int size) {
    Pageable pageableWithOneExtra = PageRequest.of(0, size + 1);
    return wrongAnswerRepository.findWrongAnswerSetWithCursor(
        memberId, cursor, pageableWithOneExtra);
  }

  private List<WrongAnswerSetResponse> toContent(List<WrongAnswerSetDto> results, int size) {
    return results.stream().limit(size).map(this::toResponse).collect(Collectors.toList());
  }

  // TODO: 카테고리 추가 필요.
  private WrongAnswerSetResponse toResponse(WrongAnswerSetDto dto) {
    QuestionSet questionSet = dto.questionSet();
    List<String> sourceNames =
        questionSet.getSources().stream().map(Source::getOriginalName).toList();

    return WrongAnswerSetResponse.of(
        questionSet.getId(),
        questionSet.getTitle(),
        sourceNames,
        questionSet.getDifficulty(),
        questionSet.getTitle(),
        dto.count(),
        null);
  }

  private Long calculateNextCursor(List<WrongAnswerSetDto> results, int size, boolean hasNext) {
    if (!hasNext) {
      return null;
    }
    return results.get(size - 1).lastWrongAnswerId();
  }

  @Override
  public void markAsWrongAnswers(Long memberId, List<Long> questionIds) {
    if (questionIds == null || questionIds.isEmpty()) {
      return;
    }

    validateMemberExists(memberId);
    List<Question> questions = questionPublicApi.findEntitiesByIds(questionIds);
    List<WrongAnswer> newWrongAnswers = filterNewWrongAnswers(memberId, questionIds, questions);
    persistNewWrongAnswers(memberId, questionIds, newWrongAnswers);
  }

  private List<WrongAnswer> filterNewWrongAnswers(
      Long memberId, List<Long> questionIds, List<Question> questions) {
    Set<Long> existingWrongAnswerQuestionIds =
        findExistingWrongAnswerQuestionIds(memberId, questionIds);
    return createNewWrongAnswers(memberId, questions, existingWrongAnswerQuestionIds);
  }

  private void persistNewWrongAnswers(
      Long memberId, List<Long> questionIds, List<WrongAnswer> newWrongAnswers) {
    if (newWrongAnswers.isEmpty()) {
      return;
    }
    try {
      wrongAnswerRepository.saveAll(newWrongAnswers);
    } catch (DataIntegrityViolationException e) {
      log.warn(
          "오답 등록 중 동시성 충돌이 발생했습니다. 멱등성 보장을 위해 정상 처리합니다. memberId: {}, questionIds: {}",
          memberId,
          questionIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
    }
  }

  private void validateMemberExists(Long memberId) {
    memberPublicApi
        .findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("요청한 회원 ID를 찾을 수 없습니다: " + memberId));
  }

  private Set<Long> findExistingWrongAnswerQuestionIds(Long memberId, List<Long> questionIds) {
    return wrongAnswerRepository.findByMemberIdAndQuestionIdIn(memberId, questionIds).stream()
        .map(wrongAnswer -> wrongAnswer.getQuestion().getId())
        .collect(Collectors.toSet());
  }

  private List<WrongAnswer> createNewWrongAnswers(
      Long memberId, List<Question> questions, Set<Long> existingIds) {
    return questions.stream()
        .filter(question -> !existingIds.contains(question.getId()))
        .map(question -> WrongAnswer.create(memberId, question))
        .collect(Collectors.toList());
  }

  @Override
  public void markAsCorrectAnswers(Long memberId, List<Long> questionIds) {
    for (Long questionId : questionIds) {
      WrongAnswer wrongAnswer =
          wrongAnswerRepository
              .findByMemberIdAndQuestionId(memberId, questionId)
              .orElseThrow(() -> new IllegalArgumentException("Wrong answer not found"));
      wrongAnswer.markAsReviewed();
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<WrongAnswerSetResponse> getAllMyWrongAnswers(Long memberId) {

    List<WrongAnswerSetDto> results =
        wrongAnswerRepository.findAllWrongAnswerSetAndCountByMemberId(memberId);

    return results.stream().map(this::toResponse).collect(Collectors.toList());
  }
}
