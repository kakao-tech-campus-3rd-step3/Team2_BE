package kr.it.pullit.modules.learningsource.source.repository;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;

public interface SourceRepository {

  Source save(Source source);

  List<Source> findByMemberIdOrderByCreatedAtDesc(Long memberId);

  List<Source> findByIdIn(List<Long> ids);

  Optional<Source> findByIdAndMemberId(Long id, Long memberId);

  Optional<Source> findById(Long id);
}
