package kr.it.pullit.modules.learningsource.source.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.learningsource.source.constant.SourceStatus;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.repository.adapter.jpa.SourceJpaRepository;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

@MockitoUnitTest
@DisplayName("SourceRepositoryImpl 단위 테스트")
class SourceRepositoryImplTest {

  @Mock private SourceJpaRepository sourceJpaRepository;

  @Mock private Source source;

  private SourceRepositoryImpl repository() {
    return new SourceRepositoryImpl(sourceJpaRepository);
  }

  @Test
  @DisplayName("save는 JPA 리포지토리에 위임한다")
  void saveDelegatesToJpaRepository() {
    given(sourceJpaRepository.save(source)).willReturn(source);

    Source saved = repository().save(source);

    assertThat(saved).isEqualTo(source);
    then(sourceJpaRepository).should().save(source);
  }

  @Test
  @DisplayName("findById는 동일한 값을 반환한다")
  void findByIdDelegates() {
    given(sourceJpaRepository.findById(1L)).willReturn(Optional.of(source));

    Optional<Source> result = repository().findById(1L);

    assertThat(result).contains(source);
    then(sourceJpaRepository).should().findById(1L);
  }

  @Test
  @DisplayName("findByMemberIdOrderByCreatedAtDesc는 정렬된 결과를 반환한다")
  void findByMemberIdOrderByCreatedAtDescDelegates() {
    given(sourceJpaRepository.findByMemberIdOrderByCreatedAtDesc(2L)).willReturn(List.of(source));

    List<Source> result = repository().findByMemberIdOrderByCreatedAtDesc(2L);

    assertThat(result).containsExactly(source);
    then(sourceJpaRepository).should().findByMemberIdOrderByCreatedAtDesc(2L);
  }

  @Test
  @DisplayName("findSourcesByMemberIdWithDetails는 결과를 그대로 반환한다")
  void findSourcesByMemberIdWithDetailsDelegates() {
    given(sourceJpaRepository.findSourcesByMemberIdWithDetails(3L)).willReturn(List.of(source));

    List<Source> result = repository().findSourcesByMemberIdWithDetails(3L);

    assertThat(result).containsExactly(source);
    then(sourceJpaRepository).should().findSourcesByMemberIdWithDetails(3L);
  }

  @Test
  @DisplayName("findByIdIn은 JPA 리포지토리의 결과를 전달한다")
  void findByIdInDelegates() {
    given(sourceJpaRepository.findByIdIn(List.of(1L, 2L))).willReturn(List.of(source));

    List<Source> result = repository().findByIdIn(List.of(1L, 2L));

    assertThat(result).containsExactly(source);
    then(sourceJpaRepository).should().findByIdIn(List.of(1L, 2L));
  }

  @Test
  @DisplayName("findByIdAndMemberId는 Optional을 그대로 반환한다")
  void findByIdAndMemberIdDelegates() {
    given(sourceJpaRepository.findByIdAndMemberId(4L, 5L)).willReturn(Optional.of(source));

    Optional<Source> result = repository().findByIdAndMemberId(4L, 5L);

    assertThat(result).contains(source);
    then(sourceJpaRepository).should().findByIdAndMemberId(4L, 5L);
  }

  @Test
  @DisplayName("findByMemberIdAndFilePath는 Optional을 반환한다")
  void findByMemberIdAndFilePathDelegates() {
    given(sourceJpaRepository.findByMemberIdAndFilePath(6L, "path"))
        .willReturn(Optional.of(source));

    Optional<Source> result = repository().findByMemberIdAndFilePath(6L, "path");

    assertThat(result).contains(source);
    then(sourceJpaRepository).should().findByMemberIdAndFilePath(6L, "path");
  }

  @Test
  @DisplayName("delete는 JPA 리포지토리에 위임한다")
  void deleteDelegates() {
    repository().delete(source);

    then(sourceJpaRepository).should().delete(source);
  }

  @Test
  @DisplayName("findByStatus는 상태별 조회를 위임한다")
  void findByStatusDelegates() {
    given(sourceJpaRepository.findByStatus(SourceStatus.READY)).willReturn(List.of(source));

    List<Source> result = repository().findByStatus(SourceStatus.READY);

    assertThat(result).containsExactly(source);
    then(sourceJpaRepository).should().findByStatus(SourceStatus.READY);
  }
}
