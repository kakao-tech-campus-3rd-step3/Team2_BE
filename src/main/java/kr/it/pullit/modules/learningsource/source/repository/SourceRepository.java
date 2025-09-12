package kr.it.pullit.modules.learningsource.source.repository;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;

public interface SourceRepository {

  Source save(Source source);

  List<Source> findByMemberIdOrderByCreatedAtDesc(Long memberId);

  Optional<Source> findByIdAndMemberId(Long id, Long memberId);
}
