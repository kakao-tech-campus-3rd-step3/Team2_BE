package kr.it.pullit.modules.learningsource.sourcefolder.repository.adapter.jpa;

import java.util.Optional;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceFolderJpaRepository extends JpaRepository<SourceFolder, Long> {

  Optional<SourceFolder> findByMemberIdAndName(Long memberId, String name);
}
