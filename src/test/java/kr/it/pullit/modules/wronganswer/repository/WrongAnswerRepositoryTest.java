package kr.it.pullit.modules.wronganswer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.MultipleChoiceQuestion;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.enums.DifficultyType;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import kr.it.pullit.modules.wronganswer.domain.entity.WrongAnswer;
import kr.it.pullit.modules.wronganswer.service.dto.WrongAnswerSetDto;
import kr.it.pullit.support.annotation.JpaSliceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@JpaSliceTest
@Import(WrongAnswerRepositoryImpl.class)
@DisplayName("WrongAnswerRepository JPA 테스트")
class WrongAnswerRepositoryTest {

  private static final Long MEMBER_ID = 42L;

  @Autowired private WrongAnswerRepository wrongAnswerRepository;

  @Autowired private TestEntityManager entityManager;

  private QuestionSet firstQuestionSet;
  private QuestionSet secondQuestionSet;
  private Question firstQuestion;
  private Question secondQuestion;
  private Question thirdQuestion;

  @BeforeEach
  void setUp() {
    firstQuestionSet = persistQuestionSet("자료구조 1");
    secondQuestionSet = persistQuestionSet("자료구조 2");

    firstQuestion = persistQuestion(firstQuestionSet, "스택의 특징은?");
    secondQuestion = persistQuestion(firstQuestionSet, "큐의 특징은?");
    thirdQuestion = persistQuestion(secondQuestionSet, "해시 테이블 충돌 처리 방식은?");

    persistWrongAnswer(MEMBER_ID, firstQuestion);
    persistWrongAnswer(MEMBER_ID, secondQuestion).markAsReviewed();
    persistWrongAnswer(MEMBER_ID, thirdQuestion);
    // [FIX 1] 'otherMemberQuestion' 변수를 인라인 처리하여 선언-사용 거리 문제를 해결
    persistWrongAnswer(999L, persistQuestion(secondQuestionSet, "다른 회원의 오답 데이터"));
    entityManager.flush();
    entityManager.clear();
  }

  @Test
  @DisplayName("save와 findByMemberIdAndQuestionId는 저장한 오답을 정확히 조회한다")
  void shouldSaveAndFindWrongAnswerByMemberAndQuestionId() {
    Question question = persistQuestion(firstQuestionSet, "재귀 호출의 종료 조건은?");
    // [FIX 2] 변수 선언과 사용 위치를 좁힐 수 없는 경우 final 키워드를 붙여 경고 해결
    final WrongAnswer saved = wrongAnswerRepository.save(WrongAnswer.create(100L, question));
    entityManager.flush();
    entityManager.clear();

    Optional<WrongAnswer> found =
        wrongAnswerRepository.findByMemberIdAndQuestionId(100L, question.getId());

    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(saved.getId());
  }

  @Test
  @DisplayName("findByMemberIdAndQuestionIdIn은 지정한 문제들만 반환한다")
  void shouldFindWrongAnswersByMemberAndQuestionIds() {
    List<WrongAnswer> wrongAnswers =
        wrongAnswerRepository.findByMemberIdAndQuestionIdIn(
            MEMBER_ID,
            List.of(firstQuestion.getId(), secondQuestion.getId(), thirdQuestion.getId()));

    assertThat(wrongAnswers)
        .extracting(wrongAnswer -> wrongAnswer.getQuestion().getId())
        .containsExactlyInAnyOrder(
            firstQuestion.getId(), secondQuestion.getId(), thirdQuestion.getId());
  }

  @Test
  @DisplayName("집계 조회는 검토되지 않은 오답만 문제집별로 반환하고 커서 조건을 적용한다")
  void shouldAggregateWrongAnswersWithCursor() {
    List<WrongAnswerSetDto> firstPage =
        wrongAnswerRepository.findWrongAnswerSetWithCursor(MEMBER_ID, null, 1);

    assertThat(firstPage).hasSize(2);
    WrongAnswerSetDto latestSet = firstPage.getFirst();
    WrongAnswerSetDto olderSet = firstPage.get(1);

    assertThat(latestSet.count()).isEqualTo(1L);
    assertThat(latestSet.questionSet().getId()).isEqualTo(secondQuestionSet.getId());
    assertThat(olderSet.count()).isEqualTo(1L);

    List<WrongAnswerSetDto> secondPage =
        wrongAnswerRepository.findWrongAnswerSetWithCursor(
            MEMBER_ID, latestSet.lastWrongAnswerId(), 1);

    assertThat(secondPage).hasSize(1);
    assertThat(secondPage.getFirst().questionSet().getId()).isEqualTo(firstQuestionSet.getId());

    List<WrongAnswerSetDto> allResults =
        wrongAnswerRepository.findAllWrongAnswerSetAndCountByMemberId(MEMBER_ID);
    assertThat(allResults)
        .extracting(dto -> dto.questionSet().getId())
        .containsExactly(secondQuestionSet.getId(), firstQuestionSet.getId());
  }

  private QuestionSet persistQuestionSet(String title) {
    QuestionSet questionSet =
        QuestionSet.builder()
            .ownerId(1L)
            .sources(new HashSet<>())
            .title(title)
            .difficulty(DifficultyType.EASY)
            .type(QuestionType.MULTIPLE_CHOICE)
            .questionLength(0)
            .build();
    entityManager.persist(questionSet);
    return questionSet;
  }

  private Question persistQuestion(QuestionSet questionSet, String questionText) {
    MultipleChoiceQuestion question =
        MultipleChoiceQuestion.builder()
            .questionSet(questionSet)
            .questionText(questionText)
            .explanation("해설")
            .options(List.of("A", "B", "C", "D"))
            .answer("A")
            .build();
    questionSet.addQuestion(question);
    entityManager.persist(question);
    return question;
  }

  private WrongAnswer persistWrongAnswer(Long memberId, Question question) {
    WrongAnswer wrongAnswer = WrongAnswer.create(memberId, question);
    entityManager.persist(wrongAnswer);
    return wrongAnswer;
  }
}
