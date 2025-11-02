package kr.it.pullit.modules.commonfolder.exception;

import kr.it.pullit.shared.error.BusinessException;

public class FolderNotFoundException extends BusinessException {

  public FolderNotFoundException(CommonFolderErrorCode errorCode, String message) {
    super(errorCode, message);
  }

  public static FolderNotFoundException byId(Long folderId) {
    return new FolderNotFoundException(
        CommonFolderErrorCode.FOLDER_NOT_FOUND, "폴더 ID: " + folderId);
  }
}
