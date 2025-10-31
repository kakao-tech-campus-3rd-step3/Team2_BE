package kr.it.pullit.modules.commonfolder.service;

import kr.it.pullit.modules.commonfolder.api.CommonFolderPublicApi;
import kr.it.pullit.modules.commonfolder.api.FolderFacade;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.exception.CommonFolderErrorCode;
import kr.it.pullit.modules.commonfolder.exception.InvalidFolderOperationException;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FolderFacadeImpl implements FolderFacade {

  private final CommonFolderPublicApi commonFolderPublicApi;
  private final QuestionSetPublicApi questionSetPublicApi;

  @Override
  @Transactional(readOnly = true)
  public long getQuestionSetCountInFolder(Long ownerId, Long folderId) {
    return questionSetPublicApi.countByFolderId(folderId);
  }

  @Override
  @Transactional
  public void deleteFolderAndContents(Long ownerId, Long folderId) {
    if (folderId.equals(CommonFolder.DEFAULT_FOLDER_ID)) {
      throw new InvalidFolderOperationException(CommonFolderErrorCode.CANNOT_DELETE_DEFAULT_FOLDER);
    }
    questionSetPublicApi.relocateQuestionSetsToDefaultFolder(folderId);
    commonFolderPublicApi.deleteFolder(ownerId, folderId);
  }
}
