package kr.it.pullit.modules.questionset.repository;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.support.annotation.JpaSliceTest;
import kr.it.pullit.support.builder.TestQuestionSetBuilder;
import kr.it.pullit.support.fixture.QuestionSetFixtures;

@JpaSliceTest
@Import(QuestionSetRepositoryImpl.class)
@DisplayName("QuestionSetRepository 슬라이스 테스트")
class QuestionSetRepositoryTest {

  @Autowired private QuestionSetRepository repository;

  @Nested
  @DisplayName("단건 조회")
  class DescribeFindOne {

    @Test
    @DisplayName("내 소유의 문제집을 ID로 조회한다")
    void findByIdAndMemberId() {
      Long ownerId = 11L;
      QuestionSet saved =
          repository.save(
              TestQuestionSetBuilder.builder().ownerId(ownerId).title("내 문제집").build());

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
    @DisplayName("문제집을 삭제하면 조회되지 않는다")
    void delete() {
      Long ownerId = 41L;
      QuestionSet saved =
          repository.save(TestQuestionSetBuilder.builder().ownerId(ownerId).title("삭제 대상").build());

      repository.deleteById(saved.getId());

      Optional<QuestionSet> found = repository.findById(saved.getId());
      assertThat(found).isEmpty();
    }
  }

  @Nested
  @DisplayName("학습/복습 흐름 조회")
  class DescribeSolvingFlows {

    @Test
    @DisplayName("문제집이 COMPLETE가 아니면 첫 풀이용 조회는 비어 있다")
    void findByIdWithQuestionsForFirstSolving() {
      Long ownerId = 50L;
      QuestionSet saved =
          repository.save(QuestionSetFixtures.withOwner(ownerId));

      var found = repository.findWithQuestionsForFirstSolving(saved.getId(), ownerId);

      // 현재 기본 상태는 PENDING이라 쿼리 전제조건을 만족하지 못함 → empty
      assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("복습 조건(틀린문제 미복습)이 없으면 복습 모드 조회는 비어 있다")
    void findQuestionSetForReviewing() {
      Long ownerId = 60L;
      QuestionSet saved =
          repository.save(QuestionSetFixtures.withOwner(ownerId));

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
      QuestionSet saved =
          repository.save(QuestionSetFixtures.withOwner(ownerId));

      var projection = repository.findQuestionSetWhenHaveNoQuestionsYet(saved.getId(), ownerId);

      // 리포지토리 시그니처상 Optional<QuestionSetResponse> 를 반환
      assertThat(projection).isPresent();
      assertThat(projection.get().getId()).isEqualTo(saved.getId());
    }
  }
}
