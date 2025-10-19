package kr.it.pullit.modules.wronganswer.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.questionset.api.QuestionPublicApi;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.wronganswer.api.WrongAnswerPublicApi;
import kr.it.pullit.modules.wronganswer.domain.entity.WrongAnswer;
import kr.it.pullit.modules.wronganswer.repository.WrongAnswerRepository;
import kr.it.pullit.modules.wronganswer.web.dto.WrongAnswerSetResponse;
import kr.it.pullit.shared.paging.dto.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WrongAnswerService implements WrongAnswerPublicApi {

  private final WrongAnswerRepository wrongAnswerRepository;
  private final MemberPublicApi memberPublicApi;
  private final QuestionPublicApi questionPublicApi;

  @Override
  @Transactional(readOnly = true)
  public CursorPageResponse<WrongAnswerSetResponse> getMyWrongAnswers(
      Long memberId, Long cursor, int size) {
    Pageable pageableWithOneExtra = PageRequest.of(0, size + 1);
    List<Object[]> rawResults =
        wrongAnswerRepository.findWrongAnswerQuestionSetWithCursor(
            memberId, cursor, pageableWithOneExtra);

    boolean hasNext = rawResults.size() > size;
    List<WrongAnswerSetResponse> content =
        rawResults.stream().limit(size).map(this::mapToDto).collect(Collectors.toList());

    Long nextCursor =
        hasNext && !content.isEmpty() ? extractCursorFrom(rawResults.get(size - 1)) : null;

    return CursorPageResponse.of(content, nextCursor, hasNext);
  }

  private Long extractCursorFrom(Object[] result) {
    QuestionSet questionSet = (QuestionSet) result[0];
    return questionSet.getId();
  }

  @Override
  @Transactional(readOnly = true)
  public List<WrongAnswerSetResponse> getAllMyWrongAnswers(Long memberId) {
    List<Object[]> results =
        wrongAnswerRepository.findAllWrongAnswerQuestionSetAndCountByMemberId(memberId);

    return results.stream().map(this::mapToDto).collect(Collectors.toList());
  }

  @Override
  public void markAsWrongAnswers(Long memberId, List<Long> questionIds) {
    List<WrongAnswer> wrongAnswers = new ArrayList<>();

    for (Long questionId : questionIds) {
      if (wrongAnswerRepository.findByMemberIdAndQuestionId(memberId, questionId).isPresent()) {
        continue;
      }

      Member member =
          memberPublicApi
              .findById(memberId)
              .orElseThrow(() -> new IllegalArgumentException("Member not found"));
      Question question =
          questionPublicApi
              .findEntityById(questionId)
              .orElseThrow(() -> new IllegalArgumentException("Question not found"));

      wrongAnswers.add(new WrongAnswer(member, question));
    }

    if (!wrongAnswers.isEmpty()) {
      wrongAnswerRepository.saveAll(wrongAnswers);
    }
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

  private WrongAnswerSetResponse mapToDto(Object[] result) {
    QuestionSet questionSet = (QuestionSet) result[0];
    long incorrectCount = (long) result[1];

    String category =
        questionSet.getSources().stream()
            .findFirst()
            .map(Source::getSourceFolder)
            .map(SourceFolder::getName)
            .orElse("미분류");

    return WrongAnswerSetResponse.builder()
        .questionSetId(questionSet.getId())
        .questionSetTitle(questionSet.getTitle())
        .sourceNames(
            questionSet.getSources().stream()
                .map(Source::getOriginalName)
                .collect(Collectors.toList()))
        .difficulty(questionSet.getDifficulty())
        .majorTopic("미구현")
        .incorrectCount((int) incorrectCount)
        .category(category)
        .build();
  }
}
