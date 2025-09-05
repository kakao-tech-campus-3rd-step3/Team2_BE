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

  @Override
  public Optional<Source> findById(Long id) {
    return sourceJpaRepository.findById(id);
  }

  @Override
  public Optional<Source> findByIdAndMemberId(Long id, Long memberId) {
    return sourceJpaRepository.findByIdAndMemberId(id, memberId);
  }

  @Override
  public List<Source> findByMemberIdOrderByCreatedAtDesc(Long memberId) {
    return sourceJpaRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
  }
}
