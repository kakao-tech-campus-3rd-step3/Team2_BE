package kr.it.pullit.modules.learningsource.source.repository.adapter.jpa;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SourceJpaRepository extends JpaRepository<Source, Long> {

  List<Source> findByMemberIdOrderByCreatedAtDesc(Long memberId);

  @Query(
      "SELECT DISTINCT s FROM Source s "
          + "LEFT JOIN FETCH s.sourceFolder "
          + "LEFT JOIN FETCH s.questionSets "
          + "WHERE s.member.id = :memberId "
          + "ORDER BY s.createdAt DESC")
  List<Source> findSourcesByMemberIdWithDetails(@Param("memberId") Long memberId);

  Optional<Source> findByIdAndMemberId(Long id, Long memberId);

  List<Source> findByIdIn(List<Long> ids);
}
