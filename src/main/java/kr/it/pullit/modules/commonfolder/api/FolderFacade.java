package kr.it.pullit.modules.commonfolder.api;

public interface FolderFacade {
  void deleteFolderAndContents(Long ownerId, Long folderId);

  long getQuestionSetCountInFolder(Long ownerId, Long folderId);
}
