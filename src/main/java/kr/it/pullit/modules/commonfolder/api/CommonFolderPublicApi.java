package kr.it.pullit.modules.commonfolder.api;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderResponse;
import kr.it.pullit.modules.commonfolder.web.dto.QuestionSetFolderRequest;

public interface CommonFolderPublicApi {

  List<CommonFolderResponse> getFolders(Long ownerId, CommonFolderType type);

  CommonFolder getOrCreateDefaultQuestionSetFolder(Long ownerId);

  Optional<CommonFolder> findFolderEntityById(Long ownerId, Long id);

  CommonFolderResponse getFolder(Long ownerId, Long id);

  CommonFolderResponse createFolder(Long ownerId, QuestionSetFolderRequest request);

  CommonFolderResponse updateFolder(Long ownerId, Long id, QuestionSetFolderRequest request);

  void deleteFolder(Long ownerId, Long id);
}
