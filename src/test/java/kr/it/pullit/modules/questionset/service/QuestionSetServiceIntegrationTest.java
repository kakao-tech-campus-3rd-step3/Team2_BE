package kr.it.pullit.modules.questionset.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import kr.it.pullit.modules.commonfolder.domain.enums.FolderScope;
import kr.it.pullit.modules.commonfolder.repository.CommonFolderRepository;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.domain.entity.SourceCreationParam;
import kr.it.pullit.modules.learningsource.source.repository.SourceRepository;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.modules.learningsource.sourcefolder.repository.SourceFolderRepository;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.domain.entity.MultipleChoiceQuestion;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.enums.DifficultyType;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import kr.it.pullit.modules.questionset.event.QuestionSetCreatedEvent;
import kr.it.pullit.modules.questionset.exception.QuestionSetNotReadyException;
import kr.it.pullit.modules.questionset.repository.QuestionSetRepository;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetCreateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetUpdateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsResponse;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;
import kr.it.pullit.modules.wronganswer.domain.entity.WrongAnswer;
import kr.it.pullit.shared.event.EventPublisher;
import kr.it.pullit.shared.paging.dto.CursorPageResponse;
import kr.it.pullit.support.annotation.IntegrationTest;
import kr.it.pullit.support.builder.TestQuestionSetBuilder;
import kr.it.pullit.support.fixture.MemberFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@IntegrationTest
@DisplayName("QuestionSetService 통합 테스트")
class QuestionSetServiceIntegrationTest {

  @Autowired private QuestionSetPublicApi publicApi;
  @Autowired private QuestionSetRepository repository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private SourceRepository sourceRepository;
  @Autowired private SourceFolderRepository sourceFolderRepository;
  @Autowired private CommonFolderRepository commonFolderRepository;
  @Autowired private EntityManager entityManager;
  @MockitoBean private EventPublisher eventPublisher;

  @BeforeEach
  void setUp() {
    Mockito.reset(eventPublisher);
  }

  @Test
  @DisplayName("제목을 수정하면 DB에 반영된다")
  void updateTitle_persists() {
    // given
    Long ownerId = 101L;
    QuestionSet saved =
        repository.save(TestQuestionSetBuilder.builder().ownerId(ownerId).title("old").build());
    entityManager.flush();
    entityManager.clear();

    // when
    publicApi.update(saved.getId(), new QuestionSetUpdateRequestDto("new-title", null), ownerId);

    // then
    QuestionSet updated = repository.findById(saved.getId()).orElseThrow();
    assertThat(updated.getTitle()).isEqualTo("new-title");
  }

  @Test
  @DisplayName("삭제하면 조회되지 않는다")
  void delete_removesRow() {
    // given
    Long ownerId = 102L;
    QuestionSet saved =
        repository.save(
            TestQuestionSetBuilder.builder().ownerId(ownerId).title("to-delete").build());
    entityManager.flush();
    entityManager.clear();

    // when
    publicApi.delete(saved.getId(), ownerId);

    entityManager.flush();
    entityManager.clear();

    // then
    assertThat(repository.findById(saved.getId())).isEmpty();
  }

  @Test
  @DisplayName("문제집을 생성하면 기본 폴더가 자동으로 설정되고 이벤트가 발행된다")
  void create_assignsDefaultFolderAndPublishesEvent() {
    Member member = persistMember();
    Source source = persistReadySource(member.getId(), "기본.pdf");

    QuestionSetCreateRequestDto request =
        new QuestionSetCreateRequestDto(
            DifficultyType.EASY, 3, QuestionType.MULTIPLE_CHOICE, List.of(source.getId()), null);

    QuestionSetResponse response = publicApi.create(request, member.getId());

    QuestionSet saved = repository.findById(response.getId()).orElseThrow();
    assertThat(saved.getCommonFolder()).isNotNull();
    assertThat(saved.getCommonFolder().getOwnerId()).isEqualTo(member.getId());
    assertThat(saved.getSources()).extracting(Source::getId).containsExactly(source.getId());

    verify(eventPublisher).publish(isA(QuestionSetCreatedEvent.class));
  }

  @Test
  @DisplayName("특정 폴더를 지정하면 해당 폴더에 할당된다")
  void create_assignsProvidedFolder() {
    Member member = persistMember();
    Source source = persistReadySource(member.getId(), "커스텀.pdf");
    CommonFolder folder =
        commonFolderRepository.save(
            CommonFolder.create(
                "커스텀", CommonFolderType.QUESTION_SET, FolderScope.CUSTOM, 0, member.getId()));

    QuestionSetCreateRequestDto request =
        new QuestionSetCreateRequestDto(
            DifficultyType.HARD,
            2,
            QuestionType.TRUE_FALSE,
            List.of(source.getId()),
            folder.getId());

    QuestionSetResponse response = publicApi.create(request, member.getId());

    QuestionSet saved = repository.findById(response.getId()).orElseThrow();
    assertThat(saved.getCommonFolder().getId()).isEqualTo(folder.getId());
    assertThat(saved.getCommonFolder().getName()).isEqualTo("커스텀");
  }

  @Test
  @DisplayName("완료된 문제집을 첫 풀이용으로 조회할 수 있다")
  void getQuestionSetForSolving_returnsCompletedSet() {
    Member member = persistMember();
    QuestionSet questionSet = persistCompleteQuestionSet(member.getId(), "완료 문제집");

    QuestionSetResponse response =
        publicApi.getQuestionSetForSolving(questionSet.getId(), member.getId(), false);

    assertThat(response.getId()).isEqualTo(questionSet.getId());
    assertThat(response.getQuestions()).hasSize(1);
  }

  @Test
  @DisplayName("복습 모드로 조회하면 틀린 문제가 포함된 문제집을 반환한다")
  void getQuestionSetForSolving_reviewingReturnsSet() {
    Member member = persistMember();
    QuestionSet questionSet = persistCompleteQuestionSet(member.getId(), "복습 문제집");
    MultipleChoiceQuestion question =
        (MultipleChoiceQuestion) questionSet.getQuestions().getFirst();
    persistWrongAnswer(member.getId(), question);

    QuestionSetResponse response =
        publicApi.getQuestionSetForSolving(questionSet.getId(), member.getId(), true);

    assertThat(response.getId()).isEqualTo(questionSet.getId());
    assertThat(response.getQuestions()).hasSize(1);
  }

  @Test
  @DisplayName("문제집이 아직 준비 중이면 첫 풀이 조회 시 예외를 던진다")
  void getQuestionSetForSolving_pendingThrows() {
    Member member = persistMember();
    QuestionSet pending = persistPendingQuestionSet(member.getId());

    assertThatThrownBy(
            () -> publicApi.getQuestionSetForSolving(pending.getId(), member.getId(), false))
        .isInstanceOf(QuestionSetNotReadyException.class);
  }

  @Test
  @DisplayName("생성 직후 조회 API는 문제집 요약을 반환한다")
  void getQuestionSetWhenHaveNoQuestionsYet_returnsProjection() {
    Member member = persistMember();
    QuestionSet pending = persistPendingQuestionSet(member.getId());

    QuestionSetResponse response =
        publicApi.getQuestionSetWhenHaveNoQuestionsYet(pending.getId(), member.getId());

    assertThat(response.getId()).isEqualTo(pending.getId());
    assertThat(response.getQuestions()).isEmpty();
  }

  @Test
  @DisplayName("내 문제집 목록 조회는 사용자가 가진 문제집을 모두 반환한다")
  void getMemberQuestionSets_returnsAllSets() {
    Member member = persistMember();
    QuestionSet first = persistPendingQuestionSet(member.getId());
    QuestionSet second = persistCompleteQuestionSet(member.getId(), "완료");

    List<MyQuestionSetsResponse> responses = publicApi.getMemberQuestionSets(member.getId());
    CursorPageResponse<MyQuestionSetsResponse> cursorPage =
        publicApi.getMemberQuestionSets(member.getId(), null, 10);

    assertThat(responses)
        .extracting(MyQuestionSetsResponse::questionSetId)
        .containsExactlyInAnyOrder(first.getId(), second.getId());
    assertThat(cursorPage.content())
        .extracting(MyQuestionSetsResponse::questionSetId)
        .containsExactlyInAnyOrder(first.getId(), second.getId());
  }

  private Member persistMember() {
    return memberRepository.save(MemberFixtures.basicUser());
  }

  private Source persistReadySource(Long ownerId, String originalName) {
    SourceFolder folder =
        sourceFolderRepository.save(SourceFolder.create(ownerId, "소스폴더", "설명", "#FFFFFF"));
    SourceCreationParam param =
        new SourceCreationParam(
            ownerId, originalName, "path/" + UUID.randomUUID(), "application/pdf", 1024L);
    Source source = Source.create(param, ownerId, folder);
    source.markAsReady();
    return sourceRepository.save(source);
  }

  private QuestionSet persistPendingQuestionSet(Long ownerId) {
    CommonFolder folder = ensureDefaultFolder(ownerId);
    QuestionSet questionSet =
        TestQuestionSetBuilder.builder().ownerId(ownerId).title("대기중").build();
    questionSet.assignToFolder(folder);
    QuestionSet saved = repository.save(questionSet);
    entityManager.flush();
    entityManager.clear();
    return saved;
  }

  private QuestionSet persistCompleteQuestionSet(Long ownerId, String title) {
    CommonFolder folder = ensureDefaultFolder(ownerId);
    QuestionSet questionSet =
        TestQuestionSetBuilder.builder().ownerId(ownerId).title(title).build();
    questionSet.assignToFolder(folder);
    addQuestion(questionSet, "문제");
    questionSet.setQuestionLength(questionSet.getQuestions().size());
    questionSet.completeProcessing();
    QuestionSet saved = repository.save(questionSet);
    entityManager.flush();
    entityManager.clear();
    return saved;
  }

  private void persistWrongAnswer(Long memberId, MultipleChoiceQuestion question) {
    WrongAnswer wrongAnswer = WrongAnswer.create(memberId, question);
    entityManager.persist(wrongAnswer);
    entityManager.flush();
  }

  private CommonFolder ensureDefaultFolder(Long ownerId) {
    return commonFolderRepository
        .findByOwnerIdAndTypeAndScope(ownerId, CommonFolderType.QUESTION_SET, FolderScope.ALL)
        .orElseGet(
            () ->
                commonFolderRepository.save(
                    CommonFolder.create(
                        CommonFolder.DEFAULT_NAME,
                        CommonFolderType.QUESTION_SET,
                        FolderScope.ALL,
                        0,
                        ownerId)));
  }

  private MultipleChoiceQuestion addQuestion(QuestionSet questionSet, String text) {
    MultipleChoiceQuestion question =
        MultipleChoiceQuestion.builder()
            .questionSet(questionSet)
            .questionText(text)
            .options(List.of("1", "2", "3", "4"))
            .answer("1")
            .explanation("해설")
            .build();
    questionSet.addQuestion(question);
    return question;
  }
}
