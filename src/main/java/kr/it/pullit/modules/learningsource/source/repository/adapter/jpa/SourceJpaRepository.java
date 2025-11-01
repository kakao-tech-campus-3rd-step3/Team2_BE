package kr.it.pullit.modules.learningsource.source.repository.adapter.jpa;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.learningsource.source.constant.SourceStatus;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SourceJpaRepository extends JpaRepository<Source, Long> {

  List<Source> findByMemberIdOrderByCreatedAtDesc(Long memberId);

  @Query(
      "SELECT s "
          + "FROM Source s "
          + "JOIN FETCH s.sourceFolder sf "
          + "WHERE s.memberId = :memberId "
          + "ORDER BY s.createdAt DESC")
  List<Source> findSourcesByMemberIdWithDetails(@Param("memberId") Long memberId);

  Optional<Source> findByIdAndMemberId(Long id, Long memberId);

  List<Source> findByIdIn(List<Long> ids);

  List<Source> findByStatus(SourceStatus status);

  Optional<Source> findByMemberIdAndFilePath(Long memberId, String filePath);
}
