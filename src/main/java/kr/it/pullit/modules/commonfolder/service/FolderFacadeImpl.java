package kr.it.pullit.modules.commonfolder.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kr.it.pullit.modules.commonfolder.api.CommonFolderPublicApi;
import kr.it.pullit.modules.commonfolder.api.FolderFacade;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FolderFacadeImpl implements FolderFacade {

  private final CommonFolderPublicApi commonFolderPublicApi;
  private final QuestionSetPublicApi questionSetPublicApi;

  @Override
  @Transactional(readOnly = true)
  public long getQuestionSetCountInFolder(Long folderId) {
    return questionSetPublicApi.countByFolderId(folderId);
  }

  @Override
  @Transactional
  public void deleteFolderAndContents(Long folderId) {
    questionSetPublicApi.deleteAllByFolderId(folderId);
    commonFolderPublicApi.deleteFolder(folderId);
  }
}
