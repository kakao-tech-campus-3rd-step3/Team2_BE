package kr.it.pullit.modules.commonfolder.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kr.it.pullit.modules.commonfolder.api.CommonFolderPublicApi;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.exception.CommonFolderErrorCode;
import kr.it.pullit.modules.commonfolder.exception.InvalidFolderOperationException;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("FolderFacadeImpl 단위 테스트")
class FolderFacadeImplTest {

  @Mock private CommonFolderPublicApi commonFolderPublicApi;

  @Mock private QuestionSetPublicApi questionSetPublicApi;

  @InjectMocks private FolderFacadeImpl folderFacade;

  @Test
  @DisplayName("폴더 안 문제집 수를 조회하면 QuestionSetPublicApi에 위임한다")
  void getQuestionSetCountInFolder() {
    when(questionSetPublicApi.countByFolderId(10L)).thenReturn(5L);

    long count = folderFacade.getQuestionSetCountInFolder(10L);

    verify(questionSetPublicApi).countByFolderId(10L);
    assertThat(count).isEqualTo(5L);
  }

  @Test
  @DisplayName("기본 폴더가 아니면 문제집과 폴더를 함께 삭제한다")
  void deleteFolderAndContents() {
    folderFacade.deleteFolderAndContents(20L);

    verify(questionSetPublicApi).deleteAllByFolderId(20L);
    verify(commonFolderPublicApi).deleteFolder(20L);
  }

  @Test
  @DisplayName("기본 폴더는 삭제를 시도하면 예외가 발생한다")
  void deleteFolderAndContents_defaultFolder() {
    assertThatThrownBy(() -> folderFacade.deleteFolderAndContents(CommonFolder.DEFAULT_FOLDER_ID))
        .isInstanceOf(InvalidFolderOperationException.class)
        .extracting("errorCode")
        .isEqualTo(CommonFolderErrorCode.CANNOT_DELETE_DEFAULT_FOLDER);

    verify(questionSetPublicApi, never()).deleteAllByFolderId(CommonFolder.DEFAULT_FOLDER_ID);
    verify(commonFolderPublicApi, never()).deleteFolder(CommonFolder.DEFAULT_FOLDER_ID);
  }
}
