package kr.it.pullit.modules.commonfolder.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;

public interface CommonFolderRepository extends JpaRepository<CommonFolder, Long> {

  List<CommonFolder> findByTypeOrderBySortOrderAsc(CommonFolderType type);

  Optional<CommonFolder> findFirstByTypeOrderBySortOrderDesc(CommonFolderType type);
}
