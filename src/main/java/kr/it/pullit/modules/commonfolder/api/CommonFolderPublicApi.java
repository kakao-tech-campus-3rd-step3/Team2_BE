package kr.it.pullit.modules.commonfolder.api;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderResponse;
import kr.it.pullit.modules.commonfolder.web.dto.QuestionSetFolderRequest;

public interface CommonFolderPublicApi {

  List<CommonFolderResponse> getFolders(CommonFolderType type);

  CommonFolder getOrCreateDefaultQuestionSetFolder();

  Optional<CommonFolder> findFolderEntityById(Long id);

  CommonFolderResponse getFolder(Long id);

  CommonFolderResponse createFolder(QuestionSetFolderRequest request);

  CommonFolderResponse updateFolder(Long id, QuestionSetFolderRequest request);

  void deleteFolder(Long id);
}
