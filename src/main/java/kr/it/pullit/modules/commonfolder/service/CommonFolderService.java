package kr.it.pullit.modules.commonfolder.service;

import java.util.List;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.repository.CommonFolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonFolderService {

  private final CommonFolderRepository commonFolderRepository;

  public List<CommonFolder> getAllFolders() {
    return commonFolderRepository.findAllByOrderBySortOrderAsc();
  }

  public List<CommonFolder> getFoldersByType(String type) {
    return commonFolderRepository.findByTypeOrderBySortOrderAsc(type);
  }

  @Transactional
  public CommonFolder createFolder(String name, String type, Long parentId, int sortOrder) {
    CommonFolder parent = null;
    if (parentId != null) {
      parent =
          commonFolderRepository
              .findById(parentId)
              .orElseThrow(() -> new IllegalArgumentException("부모 폴더를 찾을 수 없습니다."));
    }

    boolean duplicate = commonFolderRepository.existsByParentAndSortOrder(parent, sortOrder);
    if (duplicate) {
      throw new IllegalArgumentException("같은 부모 내에서 sortOrder가 중복되었습니다.");
    }

    CommonFolder folder =
        CommonFolder.builder().name(name).type(type).parent(parent).sortOrder(sortOrder).build();

    return commonFolderRepository.save(folder);
  }

  @Transactional(readOnly = true)
  public CommonFolder getFolderById(Long id) {
    return commonFolderRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("해당 ID의 폴더를 찾을 수 없습니다."));
  }

  @Transactional
  public CommonFolder updateFolder(
      Long id, String name, String type, Long parentId, int sortOrder) {
    CommonFolder folder =
        commonFolderRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("수정할 폴더를 찾을 수 없습니다."));

    CommonFolder parent = null;
    if (parentId != null) {
      parent =
          commonFolderRepository
              .findById(parentId)
              .orElseThrow(() -> new IllegalArgumentException("부모 폴더를 찾을 수 없습니다."));
    }

    boolean duplicate =
        commonFolderRepository.existsByParentAndSortOrder(parent, sortOrder)
            && !folder.getSortOrder().equals(sortOrder);
    if (duplicate) {
      throw new IllegalArgumentException("같은 부모 내에서 sortOrder가 중복되었습니다.");
    }

    folder.update(name, type, parent, sortOrder);
    return commonFolderRepository.save(folder);
  }

  @Transactional
  public void deleteFolder(Long id) {
    commonFolderRepository.deleteById(id);
  }
}
