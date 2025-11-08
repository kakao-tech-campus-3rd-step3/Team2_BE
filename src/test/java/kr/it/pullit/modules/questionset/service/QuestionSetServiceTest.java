package kr.it.pullit.modules.questionset.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.commonfolder.api.CommonFolderPublicApi;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import kr.it.pullit.modules.commonfolder.domain.enums.FolderScope;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.domain.entity.SourceCreationParam;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.exception.MemberNotFoundException;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsPublicApi;
import kr.it.pullit.modules.questionset.domain.entity.MultipleChoiceQuestion;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.enums.DifficultyType;
import kr.it.pullit.modules.questionset.enums.QuestionSetStatus;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import kr.it.pullit.modules.questionset.event.QuestionSetCreatedEvent;
import kr.it.pullit.modules.questionset.exception.QuestionSetFailedException;
import kr.it.pullit.modules.questionset.exception.QuestionSetNotFoundException;
import kr.it.pullit.modules.questionset.exception.QuestionSetNotReadyException;
import kr.it.pullit.modules.questionset.exception.QuestionSetUnauthorizedException;
import kr.it.pullit.modules.questionset.exception.SourceNotReadyException;
import kr.it.pullit.modules.questionset.repository.MarkingResultRepository;
import kr.it.pullit.modules.questionset.repository.QuestionRepository;
import kr.it.pullit.modules.questionset.repository.QuestionSetRepository;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetCreateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetUpdateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsResponse;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;
import kr.it.pullit.modules.wronganswer.exception.WrongAnswerNotFoundException;
import kr.it.pullit.shared.event.EventPublisher;
import kr.it.pullit.shared.paging.dto.CursorPageResponse;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import kr.it.pullit.support.builder.TestQuestionSetBuilder;
import kr.it.pullit.support.fixture.MemberFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

@MockitoUnitTest
@DisplayName("QuestionSetService 단위 테스트")
class QuestionSetServiceTest {

  private QuestionSetService questionSetService;

  @Mock private QuestionSetRepository questionSetRepository;
  @Mock private CommonFolderPublicApi commonFolderPublicApi;
  @Mock private SourcePublicApi sourcePublicApi;
  @Mock private MemberPublicApi memberPublicApi;
  @Mock private EventPublisher eventPublisher;
  @Mock private QuestionRepository questionRepository;
  @Mock private MarkingResultRepository markingResultRepository;
  @Mock private LearnStatsPublicApi learnStatsPublicApi;

  @BeforeEach
  void setUp() {
    QuestionSetExceptionHandler questionSetExceptionHandler =
        new QuestionSetExceptionHandler(questionSetRepository);
    questionSetService =
        new QuestionSetService(
            questionSetRepository,
            questionRepository,
            markingResultRepository,
            learnStatsPublicApi,
            commonFolderPublicApi,
            sourcePublicApi,
            memberPublicApi,
            eventPublisher,
            questionSetExceptionHandler);
  }

  @Nested
  @DisplayName("getQuestionSetForSolving")
  class DescribeGetQuestionSetForSolving {

    @Test
    @DisplayName("완료된 문제집이면 질문 목록과 함께 반환한다")
    void returnsCompletedQuestionSet() {
      QuestionSet questionSet = createQuestionSetWithId(11L);
      addQuestion(questionSet);
      questionSet.completeProcessing();

      when(questionSetRepository.findWithQuestionsForFirstSolving(11L, 5L))
          .thenReturn(Optional.of(questionSet));

      QuestionSetResponse response = questionSetService.getQuestionSetForSolving(11L, 5L, false);

      assertThat(response.getId()).isEqualTo(11L);
      verify(questionSetRepository).findWithQuestionsForFirstSolving(11L, 5L);
    }

    @Test
    @DisplayName("대기 중인 문제집이면 NotReady 예외를 던진다")
    void throwsWhenPending() {
      QuestionSet questionSet = createQuestionSetWithId(21L);

      when(questionSetRepository.findWithQuestionsForFirstSolving(21L, 9L))
          .thenReturn(Optional.empty());
      when(questionSetRepository.findByIdAndMemberId(21L, 9L)).thenReturn(Optional.of(questionSet));

      assertThatThrownBy(() -> questionSetService.getQuestionSetForSolving(21L, 9L, false))
          .isInstanceOf(QuestionSetNotReadyException.class);
    }

    @Test
    @DisplayName("생성 실패한 문제집이면 Failed 예외를 던진다")
    void throwsWhenFailed() {
      QuestionSet questionSet = createQuestionSetWithId(31L);
      questionSet.failProcessing();

      when(questionSetRepository.findWithQuestionsForFirstSolving(31L, 9L))
          .thenReturn(Optional.empty());
      when(questionSetRepository.findByIdAndMemberId(31L, 9L)).thenReturn(Optional.of(questionSet));

      assertThatThrownBy(() -> questionSetService.getQuestionSetForSolving(31L, 9L, false))
          .isInstanceOf(QuestionSetFailedException.class);
    }

    @Test
    @DisplayName("존재하지 않으면 NotFound 예외를 던진다")
    void throwsWhenNotFound() {
      when(questionSetRepository.findWithQuestionsForFirstSolving(41L, 8L))
          .thenReturn(Optional.empty());
      when(questionSetRepository.findByIdAndMemberId(41L, 8L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> questionSetService.getQuestionSetForSolving(41L, 8L, false))
          .isInstanceOf(QuestionSetNotFoundException.class);
    }

    @Test
    @DisplayName("복습 모드에서는 틀린 문제집을 반환한다")
    void returnsQuestionSetForReviewing() {
      QuestionSet questionSet = createQuestionSetWithId(51L);
      questionSet.completeProcessing();

      when(questionSetRepository.findQuestionSetForReviewing(51L, 3L))
          .thenReturn(Optional.of(questionSet));

      QuestionSetResponse response = questionSetService.getQuestionSetForSolving(51L, 3L, true);

      assertThat(response.getId()).isEqualTo(51L);
      verify(questionSetRepository).findQuestionSetForReviewing(51L, 3L);
    }

    @Test
    @DisplayName("복습 모드라도 문제집이 준비 중이면 NotReady 예외를 던진다")
    void reviewingThrowsWhenPending() {
      QuestionSet questionSet = createQuestionSetWithId(61L);

      when(questionSetRepository.findQuestionSetForReviewing(61L, 4L)).thenReturn(Optional.empty());
      when(questionSetRepository.findByIdAndMemberId(61L, 4L)).thenReturn(Optional.of(questionSet));

      assertThatThrownBy(() -> questionSetService.getQuestionSetForSolving(61L, 4L, true))
          .isInstanceOf(QuestionSetNotReadyException.class);
    }

    @Test
    @DisplayName("복습 조건이 없으면 WrongAnswer 예외를 던진다")
    void reviewingThrowsWhenNoWrongAnswers() {
      QuestionSet questionSet = createQuestionSetWithId(71L);
      questionSet.completeProcessing();

      when(questionSetRepository.findQuestionSetForReviewing(71L, 4L)).thenReturn(Optional.empty());
      when(questionSetRepository.findByIdAndMemberId(71L, 4L)).thenReturn(Optional.of(questionSet));

      assertThatThrownBy(() -> questionSetService.getQuestionSetForSolving(71L, 4L, true))
          .isInstanceOf(WrongAnswerNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("create")
  class DescribeCreate {

    @Test
    @DisplayName("READY 상태가 아닌 소스가 포함되면 SourceNotReady 예외를 던진다")
    void throwsWhenSourceNotReady() {
      Long ownerId = 101L;
      Member member = MemberFixtures.basicUser();
      Source source = createSource(ownerId, "자료.pdf");
      source.markAsFailed();

      when(memberPublicApi.findById(ownerId)).thenReturn(Optional.of(member));
      when(sourcePublicApi.findByIdIn(List.of(10L))).thenReturn(List.of(source));

      QuestionSetCreateRequestDto request =
          new QuestionSetCreateRequestDto(
              DifficultyType.HARD, 5, QuestionType.SHORT_ANSWER, List.of(10L), null);

      assertThatThrownBy(() -> questionSetService.create(request, ownerId))
          .isInstanceOf(SourceNotReadyException.class);

      verify(questionSetRepository, never()).save(any());
    }

    @Test
    @DisplayName("폴더 ID가 주어지면 해당 폴더에 할당한다")
    void assignsProvidedFolder() {
      Long ownerId = 201L;
      Member member = MemberFixtures.basicUser();
      Source source = createSource(ownerId, "자료.pdf");
      source.markAsReady();
      CommonFolder folder =
          CommonFolder.create("폴더", CommonFolderType.QUESTION_SET, FolderScope.CUSTOM, 0, ownerId);
      ReflectionTestUtils.setField(folder, "id", 5L);

      when(memberPublicApi.findById(ownerId)).thenReturn(Optional.of(member));
      when(sourcePublicApi.findByIdIn(List.of(1L))).thenReturn(List.of(source));
      when(commonFolderPublicApi.findFolderEntityById(ownerId, 5L)).thenReturn(Optional.of(folder));
      when(questionSetRepository.save(any(QuestionSet.class)))
          .thenAnswer(
              invocation -> {
                QuestionSet qs = invocation.getArgument(0);
                ReflectionTestUtils.setField(qs, "id", 300L);
                return qs;
              });

      QuestionSetCreateRequestDto request =
          new QuestionSetCreateRequestDto(
              DifficultyType.EASY, 3, QuestionType.MULTIPLE_CHOICE, List.of(1L), 5L);

      QuestionSetResponse response = questionSetService.create(request, ownerId);

      ArgumentCaptor<QuestionSet> captor = ArgumentCaptor.forClass(QuestionSet.class);
      verify(questionSetRepository).save(captor.capture());
      verify(commonFolderPublicApi, never()).getOrCreateDefaultQuestionSetFolder(ownerId);
      verify(eventPublisher).publish(any(QuestionSetCreatedEvent.class));

      QuestionSet saved = captor.getValue();
      assertThat(saved.getCommonFolder()).isEqualTo(folder);
      assertThat(response.getCommonFolderId()).isEqualTo(folder.getId());
    }

    @Test
    @DisplayName("폴더 ID가 없으면 기본 폴더를 조회한다")
    void assignsDefaultFolderWhenMissing() {
      Long ownerId = 301L;
      Member member = MemberFixtures.basicUser();
      Source source = createSource(ownerId, "자료.pdf");
      source.markAsReady();
      CommonFolder defaultFolder =
          CommonFolder.create(
              CommonFolder.DEFAULT_NAME,
              CommonFolderType.QUESTION_SET,
              FolderScope.ALL,
              0,
              ownerId);
      ReflectionTestUtils.setField(defaultFolder, "id", 7L);

      when(memberPublicApi.findById(ownerId)).thenReturn(Optional.of(member));
      when(sourcePublicApi.findByIdIn(List.of(2L))).thenReturn(List.of(source));
      when(commonFolderPublicApi.getOrCreateDefaultQuestionSetFolder(ownerId))
          .thenReturn(defaultFolder);
      when(questionSetRepository.save(any(QuestionSet.class)))
          .thenAnswer(
              invocation -> {
                QuestionSet qs = invocation.getArgument(0);
                ReflectionTestUtils.setField(qs, "id", 400L);
                return qs;
              });

      QuestionSetCreateRequestDto request =
          new QuestionSetCreateRequestDto(
              DifficultyType.HARD, 4, QuestionType.TRUE_FALSE, List.of(2L), null);

      QuestionSetResponse response = questionSetService.create(request, ownerId);

      ArgumentCaptor<QuestionSet> captor = ArgumentCaptor.forClass(QuestionSet.class);
      verify(questionSetRepository).save(captor.capture());
      verify(commonFolderPublicApi).getOrCreateDefaultQuestionSetFolder(ownerId);
      verify(eventPublisher).publish(any(QuestionSetCreatedEvent.class));

      QuestionSet saved = captor.getValue();
      assertThat(saved.getCommonFolder()).isEqualTo(defaultFolder);
      assertThat(response.getCommonFolderId()).isEqualTo(defaultFolder.getId());
    }

    @Test
    @DisplayName("존재하지 않는 폴더 ID면 예외가 발생한다")
    void throwsWhenFolderNotFound() {
      Long ownerId = 401L;
      Member member = MemberFixtures.basicUser();
      Source source = createSource(ownerId, "자료.pdf");
      source.markAsReady();

      when(memberPublicApi.findById(ownerId)).thenReturn(Optional.of(member));
      when(sourcePublicApi.findByIdIn(List.of(99L))).thenReturn(List.of(source));
      when(commonFolderPublicApi.findFolderEntityById(ownerId, 123L)).thenReturn(Optional.empty());

      QuestionSetCreateRequestDto request =
          new QuestionSetCreateRequestDto(
              DifficultyType.HARD, 4, QuestionType.MULTIPLE_CHOICE, List.of(99L), 123L);

      assertThatThrownBy(() -> questionSetService.create(request, ownerId))
          .isInstanceOf(IllegalArgumentException.class);

      verify(questionSetRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원이 존재하지 않으면 예외가 발생한다")
    void throwsWhenMemberMissing() {
      Long ownerId = 402L;

      when(memberPublicApi.findById(ownerId)).thenReturn(Optional.empty());

      QuestionSetCreateRequestDto request =
          new QuestionSetCreateRequestDto(
              DifficultyType.EASY, 3, QuestionType.TRUE_FALSE, List.of(), null);

      assertThatThrownBy(() -> questionSetService.create(request, ownerId))
          .isInstanceOf(MemberNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("기타 단일 동작")
  class DescribeMiscellaneousOperations {

    @Test
    @DisplayName("문제가 아직 생성되지 않은 문제집을 조회한다")
    void getQuestionSetWhenNoQuestions() {
      QuestionSet questionSet = createQuestionSetWithId(501L);
      QuestionSetResponse response = QuestionSetResponse.from(questionSet);

      when(questionSetRepository.findQuestionSetWhenHaveNoQuestionsYet(501L, 100L))
          .thenReturn(Optional.of(response));

      QuestionSetResponse result =
          questionSetService.getQuestionSetWhenHaveNoQuestionsYet(501L, 100L);

      assertThat(result.getId()).isEqualTo(501L);
    }

    @Test
    @DisplayName("폴더별 문제집 수를 카운트한다")
    void countByFolder() {
      when(questionSetRepository.countByCommonFolderId(44L)).thenReturn(7L);

      long count = questionSetService.countByFolderId(44L);

      assertThat(count).isEqualTo(7L);
    }

    @Test
    @DisplayName("문제집을 완료 상태로 변경한다")
    void markAsComplete() {
      QuestionSet questionSet = createQuestionSetWithId(601L);

      when(questionSetRepository.findById(601L)).thenReturn(Optional.of(questionSet));

      questionSetService.markAsComplete(601L);

      assertThat(questionSet.getStatus()).isEqualTo(QuestionSetStatus.COMPLETE);
    }

    @Test
    @DisplayName("문제집을 실패 상태로 변경한다")
    void markAsFailed() {
      QuestionSet questionSet = createQuestionSetWithId(701L);

      when(questionSetRepository.findById(701L)).thenReturn(Optional.of(questionSet));

      questionSetService.markAsFailed(701L);

      assertThat(questionSet.getStatus()).isEqualTo(QuestionSetStatus.FAILED);
    }
  }

  @Nested
  @DisplayName("update")
  class DescribeUpdate {

    @Test
    @DisplayName("제목과 폴더를 함께 수정한다")
    void updatesTitleAndFolder() {
      QuestionSet questionSet = createQuestionSetWithId(801L);
      Long memberId = 1L;
      CommonFolder folder =
          CommonFolder.create(
              "새 폴더", CommonFolderType.QUESTION_SET, FolderScope.CUSTOM, 0, memberId);
      ReflectionTestUtils.setField(folder, "id", 9L);

      when(questionSetRepository.findById(801L)).thenReturn(Optional.of(questionSet));
      when(commonFolderPublicApi.findFolderEntityById(memberId, 9L))
          .thenReturn(Optional.of(folder));

      QuestionSetUpdateRequestDto request =
          QuestionSetUpdateRequestDto.builder().title("새 제목").commonFolderId(9L).build();

      questionSetService.update(801L, request, memberId);

      assertThat(questionSet.getTitle()).isEqualTo("새 제목");
      assertThat(questionSet.getCommonFolder()).isEqualTo(folder);
    }

    @Test
    @DisplayName("소유자가 아니면 업데이트 시 예외를 던진다")
    void updateFailsWhenUnauthorized() {
      QuestionSet questionSet = createQuestionSetWithId(901L);

      when(questionSetRepository.findById(901L)).thenReturn(Optional.of(questionSet));

      QuestionSetUpdateRequestDto request =
          QuestionSetUpdateRequestDto.builder().title("새 제목").commonFolderId(null).build();

      assertThatThrownBy(() -> questionSetService.update(901L, request, 999L))
          .isInstanceOf(QuestionSetUnauthorizedException.class);
    }

    @Test
    @DisplayName("폴더 ID가 없으면 폴더는 그대로 둔다")
    void updateWithoutFolderId() {
      QuestionSet questionSet = createQuestionSetWithId(902L);
      questionSet.updateTitle("원래 제목");

      when(questionSetRepository.findById(902L)).thenReturn(Optional.of(questionSet));

      QuestionSetUpdateRequestDto request =
          QuestionSetUpdateRequestDto.builder().title("변경된 제목").commonFolderId(null).build();

      questionSetService.update(902L, request, questionSet.getOwnerId());

      verify(commonFolderPublicApi, never()).getOrCreateDefaultQuestionSetFolder(anyLong());
      assertThat(questionSet.getTitle()).isEqualTo("변경된 제목");
    }
  }

  @Nested
  @DisplayName("삭제/이동")
  class DescribeDeleteAndMove {

    @Test
    @DisplayName("폴더에 속한 문제집을 모두 삭제한다")
    void deleteAllByFolderId() {
      QuestionSet q1 = createQuestionSetWithId(1001L);
      QuestionSet q2 = createQuestionSetWithId(1002L);

      when(questionSetRepository.findAllByCommonFolderId(55L)).thenReturn(List.of(q1, q2));

      questionSetService.deleteAllByFolderId(55L);

      verify(questionSetRepository).deleteAllByIds(List.of(1001L, 1002L));
    }

    @Test
    @DisplayName("문제집을 기본 폴더로 이동시킨다")
    void relocateQuestionSetsToDefaultFolder() {
      Long memberId = 1L;
      Long folderId = 77L;
      CommonFolder sourceFolder =
          CommonFolder.create(
              "소스 폴더", CommonFolderType.QUESTION_SET, FolderScope.CUSTOM, 1, memberId);
      CommonFolder defaultFolder =
          CommonFolder.create(
              CommonFolder.DEFAULT_NAME,
              CommonFolderType.QUESTION_SET,
              FolderScope.ALL,
              0,
              memberId);
      QuestionSet q1 = createQuestionSetWithId(1101L);
      QuestionSet q2 = createQuestionSetWithId(1102L);

      when(commonFolderPublicApi.findFolderEntityById(memberId, folderId))
          .thenReturn(Optional.of(sourceFolder));
      when(commonFolderPublicApi.getOrCreateDefaultQuestionSetFolder(memberId))
          .thenReturn(defaultFolder);

      questionSetService.relocateQuestionSetsToDefaultFolder(memberId, folderId);

      verify(questionSetRepository)
          .relocateAllByFolderIdToDefaultFolder(folderId, defaultFolder.getId());
    }

    @Test
    @DisplayName("문제집을 삭제하면 학습 통계가 차감된다")
    void deleteQuestionSetAndApplyLearnStats() {
      // given
      Long questionSetId = 1201L;
      Long memberId = 1L;
      QuestionSet questionSet = createQuestionSetWithId(questionSetId, memberId);

      when(questionSetRepository.findByIdWithQuestions(questionSetId))
          .thenReturn(Optional.of(questionSet));
      when(markingResultRepository.countCorrectByQuestionSetIdAndMemberId(questionSetId, memberId))
          .thenReturn(3L);

      // when
      questionSetService.delete(questionSetId, memberId);

      // then
      assertThat(questionSet.getDeletedAt()).isNotNull();
      verify(learnStatsPublicApi).applyQuestionSetDeleted(memberId, 3L);
    }

    @Test
    @DisplayName("풀이 기록 없는 문제집을 삭제해도 학습 통계는 변하지 않는다")
    void deleteQuestionSetWithoutLearnStats() {
      // given
      Long questionSetId = 1201L;
      Long memberId = 1L;
      QuestionSet questionSet = createQuestionSetWithId(questionSetId, memberId);

      when(questionSetRepository.findByIdWithQuestions(questionSetId))
          .thenReturn(Optional.of(questionSet));
      when(markingResultRepository.countCorrectByQuestionSetIdAndMemberId(questionSetId, memberId))
          .thenReturn(0L);

      // when
      questionSetService.delete(questionSetId, memberId);

      // then
      assertThat(questionSet.getDeletedAt()).isNotNull();
      verify(learnStatsPublicApi, never()).applyQuestionSetDeleted(anyLong(), anyLong());
    }

    @Test
    @DisplayName("소유자가 아니면 삭제 시 예외를 던진다")
    void deleteFailsWhenUnauthorized() {
      QuestionSet questionSet = createQuestionSetWithId(1202L);

      when(questionSetRepository.findByIdWithQuestions(1202L)).thenReturn(Optional.of(questionSet));

      assertThatThrownBy(() -> questionSetService.delete(1202L, 999L))
          .isInstanceOf(QuestionSetUnauthorizedException.class);
    }
  }

  @Nested
  @DisplayName("조회 집계")
  class DescribeQueriesAndCounts {

    @Test
    @DisplayName("사용자 문제집 목록을 커서 없이 조회한다")
    void listMemberQuestionSets() {
      Long memberId = 1L;
      QuestionSet q1 = createQuestionSetWithId(1301L);
      QuestionSet q2 = createQuestionSetWithId(1302L);

      when(memberPublicApi.findById(memberId)).thenReturn(Optional.of(MemberFixtures.basicUser()));
      when(questionSetRepository.findByMemberId(memberId)).thenReturn(List.of(q1, q2));

      List<MyQuestionSetsResponse> responses = questionSetService.getMemberQuestionSets(memberId);

      assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("커서 기반 목록을 조회한다")
    void listMemberQuestionSetsWithCursor() {
      Long memberId = 2L;
      QuestionSet q1 = createQuestionSetWithId(1401L);
      QuestionSet q2 = createQuestionSetWithId(1402L);

      when(memberPublicApi.findById(memberId)).thenReturn(Optional.of(MemberFixtures.basicUser()));
      when(questionSetRepository.findByMemberIdWithCursorAndNextPageCheck(memberId, 0L, 2))
          .thenReturn(List.of(q1, q2));

      CursorPageResponse<MyQuestionSetsResponse> responses =
          questionSetService.getMemberQuestionSets(memberId, 0L, 2);

      assertThat(responses.content()).hasSize(2);
      assertThat(responses.hasNext()).isFalse();
    }

    @Test
    @DisplayName("폴더를 지정하여 커서 기반 목록을 조회한다")
    void listMemberQuestionSetsWithCursorAndFolder() {
      Long memberId = 2L;
      QuestionSet q1 = createQuestionSetWithId(1501L);

      when(memberPublicApi.findById(memberId)).thenReturn(Optional.of(MemberFixtures.basicUser()));
      when(questionSetRepository.findByMemberIdAndFolderIdWithCursorAndNextPageCheck(
              memberId, 9L, 0L, 1))
          .thenReturn(List.of(q1));

      CursorPageResponse<MyQuestionSetsResponse> responses =
          questionSetService.getMemberQuestionSets(memberId, 0L, 1, 9L);

      assertThat(responses.content()).hasSize(1);
    }

    @Test
    @DisplayName("문제집 수와 전체 문제 수를 카운트한다")
    void countAggregations() {
      when(questionSetRepository.countByOwnerId(3L)).thenReturn(5L);
      when(questionRepository.countByQuestionSetOwnerId(3L)).thenReturn(12L);
      assertThat(questionSetService.countByMemberId(3L)).isEqualTo(5L);
      assertThat(questionSetService.countByQuestionSetOwnerId(3L)).isEqualTo(12L);
    }

    @Test
    @DisplayName("엔티티를 직접 조회한다")
    void findEntityByIdAndMemberId() {
      QuestionSet questionSet = createQuestionSetWithId(1601L);

      when(questionSetRepository.findWithoutQuestions(1601L, 7L))
          .thenReturn(Optional.of(questionSet));

      Optional<QuestionSet> found = questionSetService.findEntityByIdAndMemberId(1601L, 7L);

      assertThat(found).contains(questionSet);
    }
  }

  private QuestionSet createQuestionSetWithId(Long id, Long ownerId) {
    QuestionSet questionSet = TestQuestionSetBuilder.builder().ownerId(ownerId).build();
    ReflectionTestUtils.setField(questionSet, "id", id);
    return questionSet;
  }

  private QuestionSet createQuestionSetWithId(Long id) {
    return createQuestionSetWithId(id, 1L);
  }

  private void addQuestion(QuestionSet questionSet) {
    MultipleChoiceQuestion question =
        MultipleChoiceQuestion.builder()
            .questionSet(questionSet)
            .questionText("질문")
            .options(List.of("1", "2", "3", "4"))
            .answer("1")
            .explanation("해설")
            .build();
    questionSet.addQuestion(question);
  }

  private Source createSource(Long ownerId, String name) {
    SourceCreationParam param =
        new SourceCreationParam(ownerId, name, "path", "application/pdf", 1024L);
    return Source.create(param, ownerId, SourceFolder.createDefaultFolder(ownerId));
  }
}
