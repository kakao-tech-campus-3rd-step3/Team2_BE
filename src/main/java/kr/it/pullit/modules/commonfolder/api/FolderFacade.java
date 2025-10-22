package kr.it.pullit.modules.commonfolder.api;

public interface FolderFacade {
  void deleteFolderAndContents(Long folderId);

  long getQuestionSetCountInFolder(Long folderId);
}
