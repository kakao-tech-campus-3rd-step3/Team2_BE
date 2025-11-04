package kr.it.pullit.modules.questionset.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import kr.it.pullit.modules.commonfolder.domain.enums.FolderScope;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.domain.entity.SourceCreationParam;
import kr.it.pullit.modules.learningsource.source.exception.SourceNotFoundException;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.modules.questionset.domain.dto.QuestionSetCreateParam;
import kr.it.pullit.modules.questionset.enums.DifficultyType;
import kr.it.pullit.modules.questionset.enums.QuestionSetStatus;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import kr.it.pullit.support.builder.TestQuestionSetBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@MockitoUnitTest
@DisplayName("QuestionSet 엔티티 테스트")
class QuestionSetTest {

  @Nested
  @DisplayName("create 메서드는")
  class DescribeCreate {

    @Test
    @DisplayName("첫 번째 소스의 이름을 제목으로 사용하여 문제집을 생성한다")
    void itCreatesQuestionSetUsingFirstSourceNameAsTitle() {
      // given
      Long ownerId = 1L;
      Source firstSource = createSource(ownerId, "자료 1");
      Source secondSource = createSource(ownerId, "자료 2");
      QuestionSetCreateParam param =
          new QuestionSetCreateParam(DifficultyType.EASY, 5, QuestionType.TRUE_FALSE);

      // when
      QuestionSet questionSet =
          QuestionSet.create(ownerId, List.of(firstSource, secondSource), param);

      // then
      assertThat(questionSet.getOwnerId()).isEqualTo(ownerId);
      assertThat(questionSet.getTitle()).isEqualTo("자료 1");
      assertThat(questionSet.getDifficulty()).isEqualTo(DifficultyType.EASY);
      assertThat(questionSet.getType()).isEqualTo(QuestionType.TRUE_FALSE);
      assertThat(questionSet.getQuestionLength()).isEqualTo(5);
      assertThat(questionSet.getStatus()).isEqualTo(QuestionSetStatus.PENDING);
      assertThat(questionSet.getSources()).containsExactlyInAnyOrder(firstSource, secondSource);
    }

    @Test
    @DisplayName("소스가 비어 있으면 예외를 던진다")
    void itThrowsExceptionWhenSourcesAreEmpty() {
      // given
      Long ownerId = 5L;
      QuestionSetCreateParam param =
          new QuestionSetCreateParam(DifficultyType.HARD, 3, QuestionType.MULTIPLE_CHOICE);

      // when & then
      assertThatThrownBy(() -> QuestionSet.create(ownerId, List.of(), param))
          .isInstanceOf(SourceNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("문제 관리 기능은")
  class DescribeQuestionManagement {

    @Test
    @DisplayName("문제를 추가하면 양방향 연관관계가 설정된다")
    void itAddsQuestionAndSetsBidirectionalRelationship() {
      // given
      QuestionSet questionSet = TestQuestionSetBuilder.builder().build();
      TrueFalseQuestion question =
          TrueFalseQuestion.builder().questionText("질문").answer(true).explanation("설명").build();

      // when
      questionSet.addQuestion(question);

      // then
      assertThat(questionSet.getQuestions()).contains(question);
      assertThat(question.getQuestionSet()).isEqualTo(questionSet);
    }

    @Test
    @DisplayName("문제를 제거하면 연관관계가 해제된다")
    void itRemovesQuestionAndUnlinksRelationship() {
      // given
      QuestionSet questionSet = TestQuestionSetBuilder.builder().build();
      TrueFalseQuestion question =
          TrueFalseQuestion.builder().questionText("질문").answer(true).explanation("설명").build();
      questionSet.addQuestion(question);

      // when
      questionSet.removeQuestion(question);

      // then
      assertThat(questionSet.getQuestions()).doesNotContain(question);
      assertThat(question.getQuestionSet()).isNull();
    }
  }

  @Nested
  @DisplayName("소스 관리 기능은")
  class DescribeSourceManagement {

    @Test
    @DisplayName("소스를 추가하면 문제집과 소스 모두에 연관관계를 설정한다")
    void itAddsSourceAndSetsBidirectionalRelationship() {
      // given
      QuestionSet questionSet = TestQuestionSetBuilder.builder().build();
      Source source = createSource(questionSet.getOwnerId(), "새로운 자료");

      // when
      questionSet.addSource(source);

      // then
      assertThat(questionSet.getSources()).contains(source);
      assertThat(source.getQuestionSets()).contains(questionSet);
    }

    @Test
    @DisplayName("소스를 제거하면 양방향 연관관계가 해제된다")
    void itRemovesSourceAndUnlinksRelationship() {
      // given
      QuestionSet questionSet = TestQuestionSetBuilder.builder().build();
      Source source = createSource(questionSet.getOwnerId(), "제거할 자료");
      questionSet.addSource(source);

      // when
      questionSet.removeSource(source);

      // then
      assertThat(questionSet.getSources()).doesNotContain(source);
      assertThat(source.getQuestionSets()).doesNotContain(questionSet);
    }
  }

  @Nested
  @DisplayName("상태 변경 기능은")
  class DescribeStatusManagement {

    @Test
    @DisplayName("completeProcessing 호출 시 상태를 COMPLETE로 변경한다")
    void itMarksProcessingAsComplete() {
      // given
      QuestionSet questionSet = TestQuestionSetBuilder.builder().build();

      // when
      questionSet.completeProcessing();

      // then
      assertThat(questionSet.getStatus()).isEqualTo(QuestionSetStatus.COMPLETE);
    }

    @Test
    @DisplayName("failProcessing 호출 시 상태를 FAILED로 변경한다")
    void itMarksProcessingAsFailed() {
      // given
      QuestionSet questionSet = TestQuestionSetBuilder.builder().build();

      // when
      questionSet.failProcessing();

      // then
      assertThat(questionSet.getStatus()).isEqualTo(QuestionSetStatus.FAILED);
    }
  }

  @Nested
  @DisplayName("기타 유틸리티 기능은")
  class DescribeMiscellaneousBehaviors {

    @Test
    @DisplayName("폴더를 할당하면 연관 폴더가 변경된다")
    void itAssignsQuestionSetToFolder() {
      // given
      QuestionSet questionSet = TestQuestionSetBuilder.builder().build();
      CommonFolder folder =
          CommonFolder.create("폴더", CommonFolderType.QUESTION_SET, FolderScope.ALL, 1, 1L);

      // when
      questionSet.assignToFolder(folder);

      // then
      assertThat(questionSet.getCommonFolder()).isEqualTo(folder);
    }

    @Test
    @DisplayName("제목을 수정하면 새로운 제목이 반영된다")
    void itUpdatesTitle() {
      // given
      QuestionSet questionSet = TestQuestionSetBuilder.builder().title("이전 제목").build();

      // when
      questionSet.updateTitle("새 제목");

      // then
      assertThat(questionSet.getTitle()).isEqualTo("새 제목");
    }
  }

  private static Source createSource(Long ownerId, String originalName) {
    SourceCreationParam param =
        new SourceCreationParam(
            ownerId, originalName, "/path/" + originalName, "application/pdf", 100L);
    return Source.create(param, ownerId, SourceFolder.createDefaultFolder(ownerId));
  }
}
