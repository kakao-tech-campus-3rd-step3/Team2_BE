package kr.it.pullit.modules.learningsource.source.repository.adapter.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;

public interface SourceJpaRepository extends JpaRepository<Source, Long> {

  List<Source> findByMemberIdOrderByCreatedAtDesc(Long memberId);

  Optional<Source> findByIdAndMemberId(Long id, Long memberId);
}
