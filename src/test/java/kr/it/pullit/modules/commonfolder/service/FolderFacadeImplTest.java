package kr.it.pullit.modules.commonfolder.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import kr.it.pullit.modules.commonfolder.api.CommonFolderPublicApi;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.exception.InvalidFolderOperationException;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@MockitoUnitTest
@DisplayName("FolderFacadeImpl 단위 테스트")
class FolderFacadeImplTest {

  @InjectMocks private FolderFacadeImpl folderFacade;

  @Mock private CommonFolderPublicApi commonFolderPublicApi;

  @Mock private QuestionSetPublicApi questionSetPublicApi;

  @Nested
  @DisplayName("폴더와 내용 삭제 기능")
  class DeleteFolderAndContents {
    private final Long ownerId = 1L;

    @Test
    @DisplayName("성공 - 문제집을 기본 폴더로 이동시킨 후, 폴더를 삭제한다")
    void deleteFolderAndRelocate_Success() {
      // given
      Long folderIdToDelete = 2L;

      // when
      folderFacade.deleteFolderAndContents(ownerId, folderIdToDelete);

      // then
      InOrder inOrder = inOrder(questionSetPublicApi, commonFolderPublicApi);
      inOrder
          .verify(questionSetPublicApi)
          .relocateQuestionSetsToDefaultFolder(ownerId, folderIdToDelete);
      inOrder.verify(commonFolderPublicApi).deleteFolder(ownerId, folderIdToDelete);
    }

    @Test
    @DisplayName("실패 - 기본 폴더는 삭제할 수 없다")
    void deleteFolderFailWhenDeletingDefaultFolder() {
      // given
      Long defaultFolderId = CommonFolder.DEFAULT_FOLDER_ID;

      // when & then
      assertThatThrownBy(() -> folderFacade.deleteFolderAndContents(ownerId, defaultFolderId))
          .isInstanceOf(InvalidFolderOperationException.class)
          .hasMessage("기본 폴더는 삭제할 수 없습니다.");

      // then
      verify(questionSetPublicApi, never())
          .relocateQuestionSetsToDefaultFolder(anyLong(), anyLong());
      verify(commonFolderPublicApi, never()).deleteFolder(anyLong(), anyLong());
    }
  }
}
