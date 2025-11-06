package kr.it.pullit.modules.questionset.repository;

import static kr.it.pullit.support.fixture.MemberFixtures.basicUser;
import static kr.it.pullit.support.fixture.QuestionSetFixtures.createCompletedQuestionSet;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import kr.it.pullit.modules.commonfolder.domain.enums.FolderScope;
import kr.it.pullit.modules.commonfolder.repository.CommonFolderRepository;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.modules.member.repository.MemberRepositoryImpl;
import kr.it.pullit.modules.questionset.domain.entity.MultipleChoiceQuestion;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.wronganswer.domain.entity.WrongAnswer;
import kr.it.pullit.support.annotation.JpaSliceTest;
import kr.it.pullit.support.builder.TestQuestionSetBuilder;
import kr.it.pullit.support.clock.MutableClock;
import kr.it.pullit.support.config.MutableClockConfig;
import kr.it.pullit.support.fixture.QuestionSetFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@JpaSliceTest
@Import({QuestionSetRepositoryImpl.class, MemberRepositoryImpl.class, MutableClockConfig.class})
@DisplayName("QuestionSetRepository 슬라이스 테스트")
class QuestionSetRepositoryTest {

  @Autowired private QuestionSetRepository repository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private MutableClock mutableClock;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private EntityManager entityManager;
  private Member member;

  @Autowired private CommonFolderRepository commonFolderRepository;

  @BeforeEach
  void setUp() {
    mutableClock.setInstant(
        LocalDateTime.of(2025, 1, 1, 0, 0).atZone(ZoneId.of("UTC")).toInstant());
    member = memberRepository.save(basicUser());
  }

  @Nested
  @DisplayName("통계 쿼리")
  class DescribeStatisticQueries {
    private final LocalDateTime today = LocalDateTime.of(2025, 1, 1, 0, 0);

    @Test
    @DisplayName("특정 기간 동안 회원이 완료한 문제의 총 개수를 조회한다")
    void shouldCountCompletedQuestionsBetweenDates() {
      // given
      mutableClock.setInstant(today.minusDays(1).atZone(ZoneId.of("UTC")).toInstant());
      repository.save(createCompletedQuestionSet(member, 10)); // 어제

      mutableClock.setInstant(today.atZone(ZoneId.of("UTC")).toInstant());
      repository.save(createCompletedQuestionSet(member, 15)); // 오늘

      mutableClock.setInstant(today.plusDays(1).atZone(ZoneId.of("UTC")).toInstant());
      repository.save(createCompletedQuestionSet(member, 20)); // 내일

      // when
      long count =
          repository.countCompletedQuestionsByMemberIdAndDateBetween(
              member.getId(), today.withHour(0), today.withHour(23).withMinute(59));

      // then
      assertThat(count).isEqualTo(15);
    }

    @Test
    @DisplayName("회원이 문제를 완료한 날짜 목록을 중복 없이 오름차순으로 조회한다")
    void shouldFindCompletedDatesInAscOrder() {
      // given
      mutableClock.setInstant(today.plusDays(1).atZone(ZoneId.of("UTC")).toInstant());
      repository.save(createCompletedQuestionSet(member, 10)); // 내일

      mutableClock.setInstant(today.atZone(ZoneId.of("UTC")).toInstant());
      repository.save(createCompletedQuestionSet(member, 15)); // 오늘

      // when
      List<LocalDateTime> completedDates = repository.findCompletedDatesByMemberId(member.getId());

      // then
      assertThat(completedDates).hasSize(2);
      assertThat(completedDates).isSorted();
      assertThat(completedDates.get(0).toLocalDate()).isEqualTo(today.toLocalDate());
      assertThat(completedDates.get(1).toLocalDate()).isEqualTo(today.toLocalDate().plusDays(1));
    }
  }

  @Nested
  @DisplayName("단건 조회")
  class DescribeFindOne {

    @Test
    @DisplayName("내 소유의 문제집을 ID로 조회한다")
    void findByIdAndMemberId() {
      Long ownerId = 11L;
      QuestionSet saved =
          repository.save(TestQuestionSetBuilder.builder().ownerId(ownerId).title("내 문제집").build());

      Optional<QuestionSet> found = repository.findByIdAndMemberId(saved.getId(), ownerId);

      assertThat(found).isPresent();
      assertThat(found.get().getId()).isEqualTo(saved.getId());
      assertThat(found.get().getOwnerId()).isEqualTo(ownerId);
    }

    @Test
    @DisplayName("해당 사용자가 접근할 수 없는 문제집이면 조회되지 않는다")
    void findByIdAndMemberId_notOwner() {
      Long ownerId = 13L;
      QuestionSet saved =
          repository.save(
              TestQuestionSetBuilder.builder().ownerId(ownerId).title("소유자만 조회").build());

      Optional<QuestionSet> found = repository.findByIdAndMemberId(saved.getId(), 999L);

      assertThat(found).isEmpty();
    }
  }

  @Nested
  @DisplayName("목록 조회")
  class DescribeFindList {

    @Test
    @DisplayName("내 문제집 목록을 모두 가져온다")
    void findByMemberId() {
      Long ownerId = 20L;

      repository.save(TestQuestionSetBuilder.builder().ownerId(ownerId).title("A").build());
      repository.save(TestQuestionSetBuilder.builder().ownerId(ownerId).title("B").build());
      repository.save(TestQuestionSetBuilder.builder().ownerId(ownerId).title("C").build());

      List<QuestionSet> result = repository.findByMemberId(ownerId);

      assertThat(result).isNotEmpty();
      assertThat(result).allMatch(q -> q.getOwnerId().equals(ownerId));
    }

    @Test
    @DisplayName("커서 기반으로 다음 페이지를 가져온다")
    void findByMemberIdWithCursor() {
      Long ownerId = 30L;

      QuestionSet q1 =
          repository.save(TestQuestionSetBuilder.builder().ownerId(ownerId).title("1").build());
      QuestionSet q2 =
          repository.save(TestQuestionSetBuilder.builder().ownerId(ownerId).title("2").build());
      QuestionSet q3 =
          repository.save(TestQuestionSetBuilder.builder().ownerId(ownerId).title("3").build());

      // 커서를 가장 뒤(id가 가장 큰)로 주고 2개만
      List<QuestionSet> page =
          repository.findByMemberIdAndFolderIdWithCursorAndNextPageCheck(
              ownerId, null, q3.getId(), 2);

      assertThat(page.size()).isLessThanOrEqualTo(2);
      assertThat(page).allMatch(q -> q.getOwnerId().equals(ownerId));

      // 더 이전 커서로 다시 조회
      List<QuestionSet> page2 =
          repository.findByMemberIdAndFolderIdWithCursorAndNextPageCheck(
              ownerId, null, q1.getId(), 2);

      assertThat(page2).isNotNull();
      assertThat(page2).allMatch(q -> q.getOwnerId().equals(ownerId));
    }
  }

  @Nested
  @DisplayName("저장/삭제")
  class DescribeSaveDelete {

    @Test
    @DisplayName("문제집을 저장하고 다시 조회할 수 있다")
    void saveAndFind() {
      Long ownerId = 40L;
      QuestionSet saved =
          repository.save(
              TestQuestionSetBuilder.builder().ownerId(ownerId).title("저장 테스트").build());

      Optional<QuestionSet> found = repository.findById(saved.getId());

      assertThat(found).isPresent();
      assertThat(found.get().getTitle()).isEqualTo("저장 테스트");
    }

    @Test
    @DisplayName("문제집을 삭제하면 논리적으로 삭제되고 조회되지 않는다")
    void delete() {
      // given
      Long ownerId = 41L;
      QuestionSet saved =
          repository.save(TestQuestionSetBuilder.builder().ownerId(ownerId).title("삭제 대상").build());

      // when
      repository.deleteById(saved.getId());
      entityManager.flush();

      // then
      // 1. @SQLRestriction에 의해 조회되지 않는지 확인
      Optional<QuestionSet> found = repository.findById(saved.getId());
      assertThat(found).isEmpty();

      // 2. DB에는 deleted_at이 설정된 채로 남아있는지 직접 확인
      Map<String, Object> dbRow =
          jdbcTemplate.queryForMap("SELECT * FROM question_set WHERE id = ?", saved.getId());
      assertThat(dbRow.get("deleted_at")).isNotNull();
    }
  }

  @Nested
  @DisplayName("학습/복습 흐름 조회")
  class DescribeSolvingFlows {

    @Test
    @DisplayName("문제집이 COMPLETE가 아니면 첫 풀이용 조회는 비어 있다")
    void findByIdWithQuestionsForFirstSolving() {
      Long ownerId = 50L;
      QuestionSet saved = repository.save(QuestionSetFixtures.withOwner(ownerId));

      var found = repository.findWithQuestionsForFirstSolving(saved.getId(), ownerId);

      // 현재 기본 상태는 PENDING이라 쿼리 전제조건을 만족하지 못함 → empty
      assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("복습 조건(틀린문제 미복습)이 없으면 복습 모드 조회는 비어 있다")
    void findQuestionSetForReviewing() {
      Long ownerId = 60L;
      QuestionSet saved = repository.save(QuestionSetFixtures.withOwner(ownerId));

      var found = repository.findQuestionSetForReviewing(saved.getId(), ownerId);

      // wrong_answer 조건이 없으므로 → empty
      assertThat(found).isEmpty();
    }
  }

  @Nested
  @DisplayName("생성 직후 조회")
  class DescribeNoQuestionsYet {

    @Test
    @DisplayName("문제가 아직 생성되지 않은 상태로 조회할 수 있다")
    void findQuestionSetWhenHaveNoQuestionsYet() {
      Long ownerId = 70L;
      QuestionSet saved = repository.save(QuestionSetFixtures.withOwner(ownerId));

      var projection = repository.findQuestionSetWhenHaveNoQuestionsYet(saved.getId(), ownerId);

      // 리포지토리 시그니처상 Optional<QuestionSetResponse> 를 반환
      assertThat(projection).isPresent();
      assertThat(projection.get().getId()).isEqualTo(saved.getId());
    }
  }

  @Nested
  @DisplayName("폴더 기반 조회/집계")
  class DescribeFolderQueries {

    @Test
    @DisplayName("폴더 ID로 문제집을 모두 조회한다")
    void findAllByCommonFolderId() {
      Long ownerId = 80L;
      CommonFolder folder = saveFolder(ownerId, "폴더");

      QuestionSet inFolderA = createQuestionSetAssignedTo(folder, ownerId, "A");
      QuestionSet inFolderB = createQuestionSetAssignedTo(folder, ownerId, "B");
      QuestionSet otherFolder =
          TestQuestionSetBuilder.builder().ownerId(ownerId).title("기타").build();

      repository.save(inFolderA);
      repository.save(inFolderB);
      repository.save(otherFolder);

      List<QuestionSet> results = repository.findAllByCommonFolderId(folder.getId());

      assertThat(results).hasSize(2);
      assertThat(results)
          .allMatch(qs -> qs.getCommonFolder() != null)
          .allMatch(qs -> qs.getCommonFolder().getId().equals(folder.getId()));
    }

    @Test
    @DisplayName("폴더에 속한 문제집 수를 계산한다")
    void countByCommonFolderId() {
      Long ownerId = 81L;
      CommonFolder folder = saveFolder(ownerId, "카운트");

      repository.save(createQuestionSetAssignedTo(folder, ownerId, "1"));
      repository.save(createQuestionSetAssignedTo(folder, ownerId, "2"));

      long count = repository.countByCommonFolderId(folder.getId());

      assertThat(count).isEqualTo(2L);
    }
  }

  @Nested
  @DisplayName("통계 계산")
  class DescribeStatistics {

    @Test
    @DisplayName("사용자가 보유한 문제집 수를 계산한다")
    void countByOwnerId() {
      Long ownerId = 83L;

      repository.save(TestQuestionSetBuilder.builder().ownerId(ownerId).title("1").build());
      repository.save(TestQuestionSetBuilder.builder().ownerId(ownerId).title("2").build());
      repository.save(TestQuestionSetBuilder.builder().ownerId(999L).title("다른 사용자").build());

      long count = repository.countByOwnerId(ownerId);

      assertThat(count).isEqualTo(2L);
    }
  }

  @Nested
  @DisplayName("복습 조회")
  class DescribeReviewing {

    @Test
    @DisplayName("틀린 문제가 남아 있으면 복습용 문제집을 조회한다")
    void findQuestionSetForReviewing() {
      Long ownerId = 84L;
      CommonFolder folder = saveFolder(ownerId, "복습");

      QuestionSet questionSet = createQuestionSetAssignedTo(folder, ownerId, "복습 문제집");
      MultipleChoiceQuestion question = addQuestion(questionSet, "틀린 문제");
      questionSet.setQuestionLength(questionSet.getQuestions().size());
      questionSet.completeProcessing();
      QuestionSet saved = repository.save(questionSet);

      WrongAnswer wrongAnswer = WrongAnswer.create(ownerId, question);
      entityManager.persist(wrongAnswer);
      entityManager.flush();

      Optional<QuestionSet> found = repository.findQuestionSetForReviewing(saved.getId(), ownerId);

      assertThat(found).isPresent();
      assertThat(found.get().getQuestions()).hasSize(1);
    }
  }

  private CommonFolder saveFolder(Long ownerId, String name) {
    CommonFolder folder =
        CommonFolder.create(name, CommonFolderType.QUESTION_SET, FolderScope.CUSTOM, 0, ownerId);
    return commonFolderRepository.save(folder);
  }

  private QuestionSet createQuestionSetAssignedTo(CommonFolder folder, Long ownerId, String title) {
    QuestionSet questionSet =
        TestQuestionSetBuilder.builder().ownerId(ownerId).title(title).build();
    questionSet.assignToFolder(folder);
    return questionSet;
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
