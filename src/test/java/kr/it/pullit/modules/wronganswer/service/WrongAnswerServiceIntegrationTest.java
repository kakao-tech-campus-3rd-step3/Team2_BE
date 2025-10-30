package kr.it.pullit.modules.wronganswer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.HashSet;
import java.util.List;
import jakarta.persistence.EntityManager;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.domain.entity.SourceCreationParam;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.questionset.domain.entity.MultipleChoiceQuestion;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.enums.DifficultyType;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import kr.it.pullit.modules.wronganswer.domain.entity.WrongAnswer;
import kr.it.pullit.modules.wronganswer.repository.WrongAnswerRepository;
import kr.it.pullit.modules.wronganswer.service.dto.WrongAnswerSetDto;
import kr.it.pullit.modules.wronganswer.web.dto.WrongAnswerSetResponse;
import kr.it.pullit.shared.paging.dto.CursorPageResponse;
import kr.it.pullit.support.annotation.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@IntegrationTest
@DisplayName("WrongAnswerService 통합 테스트")
class WrongAnswerServiceIntegrationTest {

  @Autowired private WrongAnswerService wrongAnswerService;

  @MockitoSpyBean private WrongAnswerRepository wrongAnswerRepository;

  @Autowired private EntityManager entityManager;

  private Member member;
  private QuestionSet firstQuestionSet;
  private QuestionSet secondQuestionSet;
  private Question firstQuestion;
  private Question secondQuestion;
  private Question thirdQuestion;
  private Long memberId;

  @BeforeEach
  void setUp() {
    member = Member.createMember(1111L, "tester@pullit.kr", "테스터");
    entityManager.persist(member);
    memberId = member.getId();
    firstQuestionSet = persistQuestionSet("AI 면접 1", "면접 대비 노트");
    secondQuestionSet = persistQuestionSet("AI 면접 2", "면접 실전 노트");

    firstQuestion = persistQuestion(firstQuestionSet, "AI 면접 예상 질문 1?");
    secondQuestion = persistQuestion(secondQuestionSet, "AI 면접 예상 질문 2?");
    thirdQuestion = persistQuestion(secondQuestionSet, "AI 면접 예상 질문 3?");
    entityManager.flush();
  }

  @Test
  @DisplayName("오답을 등록하면 새로운 문제만 저장하고 중복은 무시한다")
  void shouldRegisterNewWrongAnswersAndSkipExistingOnes() {
    wrongAnswerService.markAsWrongAnswers(
        memberId, List.of(firstQuestion.getId(), secondQuestion.getId()));
    wrongAnswerService.markAsWrongAnswers(
        memberId, List.of(firstQuestion.getId(), thirdQuestion.getId()));
    wrongAnswerService.markAsWrongAnswers(memberId, List.of());

    List<WrongAnswer> wrongAnswers =
        wrongAnswerRepository.findByMemberIdAndQuestionIdIn(
            memberId, List.of(firstQuestion.getId(), secondQuestion.getId(), thirdQuestion.getId()));

    assertThat(wrongAnswers).hasSize(3);
    assertThat(wrongAnswers.stream().map(WrongAnswer::getQuestion).toList())
        .containsExactlyInAnyOrder(firstQuestion, secondQuestion, thirdQuestion);
  }

  @Test
  @DisplayName("멤버가 존재하지 않으면 오답 등록 시 예외가 발생한다")
  void shouldThrowWhenMemberDoesNotExist() {
    assertThatThrownBy(
            () -> wrongAnswerService.markAsWrongAnswers(404L, List.of(firstQuestion.getId())))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("404");
  }

  @Test
  @DisplayName("오답집 페이지 조회는 커서 정보를 포함한 응답을 반환한다")
  void shouldReturnPagedWrongAnswers() {
    wrongAnswerService.markAsWrongAnswers(
        memberId, List.of(firstQuestion.getId(), secondQuestion.getId()));
    wrongAnswerService.markAsWrongAnswers(memberId, List.of(thirdQuestion.getId()));

    List<WrongAnswer> memberWrongAnswers =
        wrongAnswerRepository.findByMemberIdAndQuestionIdIn(
            memberId, List.of(firstQuestion.getId(), secondQuestion.getId(), thirdQuestion.getId()));

    List<WrongAnswer> setTwoWrongAnswers =
        memberWrongAnswers.stream()
            .filter(wrongAnswer -> wrongAnswer.getQuestion().getQuestionSet().equals(secondQuestionSet))
            .toList();
    WrongAnswer setOneWrongAnswer =
        memberWrongAnswers.stream()
            .filter(wrongAnswer -> wrongAnswer.getQuestion().getQuestionSet().equals(firstQuestionSet))
            .findFirst()
            .orElseThrow();

    WrongAnswerSetDto latestSetDto =
        WrongAnswerSetDto.builder()
            .questionSet(secondQuestionSet)
            .count((long) setTwoWrongAnswers.size())
            .lastWrongAnswerId(
                setTwoWrongAnswers.stream().map(WrongAnswer::getId).max(Long::compare).orElseThrow())
            .build();
    WrongAnswerSetDto olderSetDto =
        WrongAnswerSetDto.builder()
            .questionSet(firstQuestionSet)
            .count(1L)
            .lastWrongAnswerId(setOneWrongAnswer.getId())
            .build();

    Mockito.doReturn(List.of(latestSetDto, olderSetDto))
        .when(wrongAnswerRepository)
        .findWrongAnswerSetWithCursor(memberId, null, 1);
    Mockito.doReturn(List.of(olderSetDto))
        .when(wrongAnswerRepository)
        .findWrongAnswerSetWithCursor(memberId, latestSetDto.lastWrongAnswerId(), 1);

    CursorPageResponse<WrongAnswerSetResponse> firstPage =
        wrongAnswerService.getMyWrongAnswers(memberId, null, 1);

    assertThat(firstPage.hasNext()).isTrue();
    assertThat(firstPage.content()).hasSize(1);
    WrongAnswerSetResponse firstResponse = firstPage.content().getFirst();
    assertThat(firstResponse.questionSetTitle()).isEqualTo(secondQuestionSet.getTitle());
    assertThat(firstResponse.sourceNames()).containsExactly("면접 실전 노트");

    CursorPageResponse<WrongAnswerSetResponse> secondPage =
        wrongAnswerService.getMyWrongAnswers(memberId, firstPage.nextCursor(), 1);

    assertThat(secondPage.hasNext()).isFalse();
    assertThat(secondPage.content()).hasSize(1);
    assertThat(secondPage.content().getFirst().questionSetId())
        .isEqualTo(firstQuestionSet.getId());
  }

  @Test
  @DisplayName("전체 오답 조회는 검토되지 않은 오답만 반환한다")
  void shouldReturnAllWrongAnswers() {
    wrongAnswerService.markAsWrongAnswers(
        memberId, List.of(firstQuestion.getId(), secondQuestion.getId()));
    wrongAnswerService.markAsCorrectAnswers(memberId, List.of(secondQuestion.getId()));

    List<WrongAnswerSetResponse> responses =
        wrongAnswerService.getAllMyWrongAnswers(memberId);

    assertThat(responses).hasSize(1);
    assertThat(responses.getFirst().questionSetId()).isEqualTo(firstQuestionSet.getId());
  }

  @Test
  @DisplayName("정답 처리하면 해당 오답은 검토됨으로 표시된다")
  void shouldMarkWrongAnswersAsReviewed() {
    wrongAnswerService.markAsWrongAnswers(
        memberId, List.of(firstQuestion.getId(), secondQuestion.getId()));

    wrongAnswerService.markAsCorrectAnswers(memberId, List.of(secondQuestion.getId()));
    wrongAnswerService.markAsCorrectAnswers(memberId, null);

    List<WrongAnswer> wrongAnswers =
        wrongAnswerRepository.findByMemberIdAndQuestionIdIn(
            memberId, List.of(firstQuestion.getId(), secondQuestion.getId()));

    assertThat(wrongAnswers)
        .filteredOn(wrongAnswer -> wrongAnswer.getQuestion().getId().equals(secondQuestion.getId()))
        .allMatch(WrongAnswer::getIsReviewed);
    assertThat(wrongAnswers)
        .filteredOn(wrongAnswer -> wrongAnswer.getQuestion().getId().equals(firstQuestion.getId()))
        .allMatch(wrongAnswer -> !wrongAnswer.getIsReviewed());
  }

  private QuestionSet persistQuestionSet(String title, String sourceName) {
    SourceFolder folder = SourceFolder.create(1L, "폴더", "설명", "#FFFFFF");
    entityManager.persist(folder);

    SourceCreationParam creationParam =
        new SourceCreationParam(1L, sourceName, "path", "application/pdf", 1024L);
    Source source = Source.create(creationParam, 1L, folder);
    source.markAsReady();
    entityManager.persist(source);

    QuestionSet questionSet =
        QuestionSet.builder()
            .ownerId(1L)
            .sources(new HashSet<>())
            .title(title)
            .difficulty(DifficultyType.HARD)
            .type(QuestionType.MULTIPLE_CHOICE)
            .questionLength(0)
            .build();
    questionSet.addSource(source);
    entityManager.persist(questionSet);
    return questionSet;
  }

  private Question persistQuestion(QuestionSet questionSet, String questionText) {
    MultipleChoiceQuestion question =
        MultipleChoiceQuestion.builder()
            .questionSet(questionSet)
            .questionText(questionText)
            .options(List.of("1", "2", "3", "4"))
            .answer("1")
            .explanation("해설")
            .build();
    questionSet.addQuestion(question);
    entityManager.persist(question);
    return question;
  }
}
