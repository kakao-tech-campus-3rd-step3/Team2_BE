package kr.it.pullit.modules.commonfolder.repository;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import kr.it.pullit.modules.commonfolder.domain.enums.FolderScope;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommonFolderRepository extends JpaRepository<CommonFolder, Long> {

  List<CommonFolder> findByTypeOrderBySortOrderAsc(CommonFolderType type);

  List<CommonFolder> findByOwnerIdAndTypeOrderBySortOrderAsc(Long ownerId, CommonFolderType type);

  Optional<CommonFolder> findFirstByTypeOrderBySortOrderDesc(CommonFolderType type);

  Optional<CommonFolder> findFirstByOwnerIdAndTypeOrderBySortOrderDesc(
      Long ownerId, CommonFolderType type);

  Optional<CommonFolder> findByNameAndType(String name, CommonFolderType type);

  Optional<CommonFolder> findByNameAndOwnerIdAndType(
      String name, Long ownerId, CommonFolderType type);

  Optional<CommonFolder> findByOwnerIdAndTypeAndScope(
      Long ownerId, CommonFolderType type, FolderScope scope);
}
