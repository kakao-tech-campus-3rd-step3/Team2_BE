package kr.it.pullit.modules.commonfolder.api;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderResponse;
import kr.it.pullit.modules.commonfolder.web.dto.CreateFolderRequest;
import kr.it.pullit.modules.commonfolder.web.dto.UpdateFolderRequest;

public interface CommonFolderPublicApi {

  void createInitialFolders(Long ownerId);

  List<CommonFolderResponse> getFolders(Long ownerId, CommonFolderType type);

  CommonFolder getOrCreateDefaultQuestionSetFolder(Long ownerId);

  Optional<CommonFolder> findFolderEntityById(Long ownerId, Long id);

  CommonFolderResponse getFolder(Long ownerId, Long id);

  CommonFolderResponse createFolder(Long ownerId, CreateFolderRequest request);

  CommonFolderResponse updateFolder(Long ownerId, Long id, UpdateFolderRequest request);

  void deleteFolder(Long ownerId, Long id);
}
