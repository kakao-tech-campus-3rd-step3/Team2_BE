package kr.it.pullit.modules.commonfolder.repository;

import java.util.List;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommonFolderRepository extends JpaRepository<CommonFolder, Long> {
  List<CommonFolder> findAllByOrderBySortOrderAsc();

  List<CommonFolder> findByTypeOrderBySortOrderAsc(String type);

  boolean existsByParentAndSortOrder(CommonFolder parent, int sortOrder);
}
