package kr.it.pullit.modules.learningsource.source.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import kr.it.pullit.modules.learningsource.source.constant.SourceStatus;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.shared.jpa.BaseEntity;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@MockitoUnitTest
class SourceTest {

  private static Source createSource(Long memberId, String filePath) {
    SourceCreationParam param =
        new SourceCreationParam(memberId, "자료.pdf", filePath, "application/pdf", 1024L);
    return Source.create(param, memberId, SourceFolder.createDefaultFolder(memberId));
  }

  @Test
  @DisplayName("Source.create는 기본 상태와 메타데이터를 설정한다")
  void createInitializesDefaults() {
    Source source = createSource(1L, "learning-sources/one.pdf");

    assertThat(source.getMemberId()).isEqualTo(1L);
    assertThat(source.getOriginalName()).isEqualTo("자료.pdf");
    assertThat(source.getFilePath()).isEqualTo("learning-sources/one.pdf");
    assertThat(source.getStatus()).isEqualTo(SourceStatus.UPLOADED);
  }

  @Test
  @DisplayName("파일 정보를 갱신하면 상태가 UPLOADED로 초기화된다")
  void updateFileInfoResetsStatusToUploaded() {
    Source source = createSource(2L, "learning-sources/two.pdf");
    source.markAsReady();

    source.updateFileInfo("새파일.pdf", "application/pdf", 2048L);

    assertThat(source.getOriginalName()).isEqualTo("새파일.pdf");
    assertThat(source.getFileSizeBytes()).isEqualTo(2048L);
    assertThat(source.getStatus()).isEqualTo(SourceStatus.UPLOADED);
  }

  @Test
  @DisplayName("상태 전이를 순차적으로 수행할 수 있다")
  void statusTransitions() {
    Source source = createSource(3L, "learning-sources/three.pdf");

    source.startProcessing();
    assertThat(source.getStatus()).isEqualTo(SourceStatus.PROCESSING);

    source.markAsFailed();
    assertThat(source.getStatus()).isEqualTo(SourceStatus.FAILED);

    source.markAsReady();
    assertThat(source.getStatus()).isEqualTo(SourceStatus.READY);
  }

  @Test
  @DisplayName("문제집 생성 이력이 없으면 최근 생성일이 null이다")
  void getRecentQuestionGeneratedAtReturnsNullWhenEmpty() {
    Source source = createSource(4L, "learning-sources/four.pdf");

    assertThat(source.getRecentQuestionGeneratedAt()).isNull();
  }

  @Test
  @DisplayName("문제집 생성 이력 중 가장 최근 생성일을 반환한다")
  void getRecentQuestionGeneratedAtReturnsMostRecent() throws Exception {
    Source source = createSource(5L, "learning-sources/five.pdf");

    QuestionSet older = QuestionSet.builder().ownerId(1L).title("old").build();
    QuestionSet newer = QuestionSet.builder().ownerId(1L).title("new").build();

    setTimestamps(older, LocalDateTime.of(2024, 1, 1, 0, 0));
    setTimestamps(newer, LocalDateTime.of(2024, 2, 1, 0, 0));

    older.addSource(source);
    newer.addSource(source);

    assertThat(source.getQuestionSets()).hasSize(2);
    assertThat(source.getRecentQuestionGeneratedAt()).isEqualTo(LocalDateTime.of(2024, 2, 1, 0, 0));
  }

  @Test
  @DisplayName("삭제 전 콜백이 연관된 문제집과의 연관관계를 끊는다")
  void preRemoveDetachesQuestionSets() throws Exception {
    Source source = createSource(6L, "learning-sources/six.pdf");
    QuestionSet questionSet = QuestionSet.builder().ownerId(2L).title("질문집").build();
    questionSet.addSource(source);

    invokePreRemove(source);

    assertThat(source.getQuestionSets()).isEmpty();
    assertThat(questionSet.getSources()).doesNotContain(source);
  }

  private static void setTimestamps(BaseEntity entity, LocalDateTime time) throws Exception {
    Field createdAt = BaseEntity.class.getDeclaredField("createdAt");
    createdAt.setAccessible(true);
    createdAt.set(entity, time);

    Field updatedAt = BaseEntity.class.getDeclaredField("updatedAt");
    updatedAt.setAccessible(true);
    updatedAt.set(entity, time);
  }

  private static void invokePreRemove(Source source) throws Exception {
    Method method = Source.class.getDeclaredMethod("preRemove");
    method.setAccessible(true);
    method.invoke(source);
  }
}
