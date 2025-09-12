package kr.it.pullit.modules.learningsource.source.repository;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.repository.adapter.jpa.SourceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SourceRepositoryImpl implements SourceRepository {

  private final SourceJpaRepository sourceJpaRepository;

  @Override
  public Source save(Source source) {
    return sourceJpaRepository.save(source);
  }

  /**
   * 사용처: 학습 소스 목록 조회 API.
   *
   * @param memberId 조회 대상 회원 식별자
   * @return 생성일시 최신순의 소스 목록
   */
  @Override
  public List<Source> findByMemberIdOrderByCreatedAtDesc(Long memberId) {
    return sourceJpaRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
  }

  @Override
  public List<Source> findByIdIn(List<Long> ids) {
    return sourceJpaRepository.findByIdIn(ids);
  }

  @Override
  public Optional<Source> findByIdAndMemberId(Long id, Long memberId) {
    return sourceJpaRepository.findByIdAndMemberId(id, memberId);
  }

  @Override
  public Optional<Source> findById(Long id) {
    return sourceJpaRepository.findById(id);
  }
}
