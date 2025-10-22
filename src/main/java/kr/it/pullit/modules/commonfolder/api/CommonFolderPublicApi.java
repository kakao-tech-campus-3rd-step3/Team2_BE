package kr.it.pullit.modules.commonfolder.api;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderRequest;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderResponse;

public interface CommonFolderPublicApi {

  List<CommonFolderResponse> getQuestionSetFolders();

  CommonFolder getOrCreateDefaultQuestionSetFolder();

  Optional<CommonFolder> findFolderEntityById(Long id);

  CommonFolderResponse getFolder(Long id);

  CommonFolderResponse createQuestionSetFolder(CommonFolderRequest request);

  CommonFolderResponse updateFolder(Long id, CommonFolderRequest request);

  void deleteFolder(Long id);
}
