package kr.it.pullit.modules.commonfolder.service;

import static kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType.QUESTION_SET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.exception.FolderNotFoundException;
import kr.it.pullit.modules.commonfolder.exception.InvalidFolderOperationException;
import kr.it.pullit.modules.commonfolder.repository.CommonFolderRepository;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderResponse;
import kr.it.pullit.modules.commonfolder.web.dto.QuestionSetFolderRequest;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@MockitoUnitTest
@DisplayName("CommonFolderService 단위 테스트")
class CommonFolderServiceTest {

  @InjectMocks private CommonFolderService commonFolderService;

  @Mock private CommonFolderRepository commonFolderRepository;

  @Nested
  @DisplayName("사용자별 기본 폴더 생성/조회")
  class GetOrCreateDefaultFolder {

    private final Long ownerId = 1L;
    private final String defaultFolderName = "전체";

    @Test
    @DisplayName("성공 - 해당 유저의 기본 폴더가 이미 존재하면, 그것을 반환한다")
    void shouldReturnExistingDefaultFolder() {
      // given
      CommonFolder existingFolder =
          CommonFolder.create(defaultFolderName, QUESTION_SET, 0, ownerId);
      given(
              commonFolderRepository.findByNameAndOwnerIdAndType(
                  defaultFolderName, ownerId, QUESTION_SET))
          .willReturn(Optional.of(existingFolder));

      // when
      CommonFolder result = commonFolderService.getOrCreateDefaultQuestionSetFolder(ownerId);

      // then
      assertThat(result).isEqualTo(existingFolder);
      verify(commonFolderRepository, never()).save(any());
    }

    @Test
    @DisplayName("성공 - 해당 유저의 기본 폴더가 없으면, 새로 생성하고 저장한 뒤 반환한다")
    void shouldCreateAndReturnNewDefaultFolder() {
      // given
      given(
              commonFolderRepository.findByNameAndOwnerIdAndType(
                  defaultFolderName, ownerId, QUESTION_SET))
          .willReturn(Optional.empty());

      given(
              commonFolderRepository.findFirstByOwnerIdAndTypeOrderBySortOrderDesc(
                  ownerId, QUESTION_SET))
          .willReturn(Optional.empty());

      when(commonFolderRepository.save(any(CommonFolder.class))).then(i -> i.getArgument(0));

      // when
      CommonFolder result = commonFolderService.getOrCreateDefaultQuestionSetFolder(ownerId);

      // then
      verify(commonFolderRepository, times(1)).save(any(CommonFolder.class));
      assertThat(result.getName()).isEqualTo(defaultFolderName);
      assertThat(result.getOwnerId()).isEqualTo(ownerId);
      assertThat(result.getSortOrder()).isZero();
    }
  }

  @Nested
  @DisplayName("폴더 목록 조회")
  class GetFolders {

    @Test
    @DisplayName("성공 - 특정 사용자의 폴더 목록만 정확히 조회한다")
    void shouldReturnOnlyOwnedFolders() {
      // given
      Long ownerId = 1L;
      CommonFolder myFolder1 = CommonFolder.create("내 폴더 1", QUESTION_SET, 0, ownerId);
      CommonFolder myFolder2 = CommonFolder.create("내 폴더 2", QUESTION_SET, 1, ownerId);

      given(commonFolderRepository.findByOwnerIdAndTypeOrderBySortOrderAsc(ownerId, QUESTION_SET))
          .willReturn(List.of(myFolder1, myFolder2));

      // when
      List<CommonFolderResponse> result = commonFolderService.getFolders(ownerId, QUESTION_SET);

      // then
      assertThat(result).hasSize(2);
      assertThat(result.get(0).name()).isEqualTo("내 폴더 1");
    }
  }

  @Nested
  @DisplayName("폴더 생성")
  class CreateFolder {
    @Test
    @DisplayName("성공 - 기존 폴더가 있을 때 다음 순서로 새로운 폴더를 생성하고 저장한다")
    void createFolder_Success_WithExistingFolders() {
      // given
      Long ownerId = 1L;
      QuestionSetFolderRequest request = new QuestionSetFolderRequest("새 폴더", QUESTION_SET);
      CommonFolder lastFolder = CommonFolder.create("마지막 폴더", QUESTION_SET, 5, ownerId);

      given(
              commonFolderRepository.findFirstByOwnerIdAndTypeOrderBySortOrderDesc(
                  ownerId, QUESTION_SET))
          .willReturn(Optional.of(lastFolder));

      ArgumentCaptor<CommonFolder> folderCaptor = ArgumentCaptor.forClass(CommonFolder.class);
      when(commonFolderRepository.save(folderCaptor.capture())).then(i -> i.getArgument(0));

      // when
      commonFolderService.createFolder(ownerId, request);

      // then
      verify(commonFolderRepository, times(1)).save(any(CommonFolder.class));
      CommonFolder savedFolder = folderCaptor.getValue();
      assertThat(savedFolder.getName()).isEqualTo("새 폴더");
      assertThat(savedFolder.getSortOrder()).isEqualTo(6);
    }
  }

  @Nested
  @DisplayName("폴더 수정")
  class UpdateFolder {

    @Test
    @DisplayName("실패 - 존재하지 않는 폴더를 수정하려 하면 FolderNotFoundException이 발생한다")
    void updateFolder_Fail_WhenFolderNotFound() {
      // given
      Long ownerId = 1L;
      Long folderId = 999L;
      QuestionSetFolderRequest request = new QuestionSetFolderRequest("새 이름", QUESTION_SET);
      given(commonFolderRepository.findById(folderId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> commonFolderService.updateFolder(ownerId, folderId, request))
          .isInstanceOf(FolderNotFoundException.class);
    }

    @Test
    @DisplayName("실패 - 다른 사람의 폴더를 수정하려 하면 FolderNotFoundException이 발생한다")
    void updateFolder_Fail_WhenFolderOwnedByAnother() {
      // given
      Long ownerId = 1L;
      Long folderId = 2L;
      Long anotherOwnerId = 99L;
      CommonFolder anotherFolder = CommonFolder.create("다른 사람 폴더", QUESTION_SET, 0, anotherOwnerId);
      QuestionSetFolderRequest request = new QuestionSetFolderRequest("새 이름", QUESTION_SET);
      given(commonFolderRepository.findById(folderId)).willReturn(Optional.of(anotherFolder));

      // when & then
      assertThatThrownBy(() -> commonFolderService.updateFolder(ownerId, folderId, request))
          .isInstanceOf(FolderNotFoundException.class);
    }

    @Test
    @DisplayName("실패 - 기본 폴더의 이름을 변경하려 하면 InvalidFolderOperationException이 발생한다")
    void updateDefaultFolderName_shouldThrowException() {
      // given
      Long ownerId = 1L;
      Long folderId = 2L;
      CommonFolder defaultFolder = CommonFolder.create("전체", QUESTION_SET, 0, ownerId);
      QuestionSetFolderRequest request = new QuestionSetFolderRequest("다른 이름", QUESTION_SET);
      given(commonFolderRepository.findById(folderId)).willReturn(Optional.of(defaultFolder));

      // when & then
      assertThatThrownBy(() -> commonFolderService.updateFolder(ownerId, folderId, request))
          .isInstanceOf(InvalidFolderOperationException.class);
    }
  }

  @Nested
  @DisplayName("폴더 삭제")
  class DeleteFolder {
    @Test
    @DisplayName("실패 - 다른 사용자의 폴더는 삭제할 수 없다 (FolderNotFoundException 발생)")
    void deleteFolder_Fail_WhenFolderOwnedByAnother() {
      // given
      Long ownerId = 1L;
      Long folderId = 10L;
      Long anotherOwnerId = 99L;
      CommonFolder anotherFolder = CommonFolder.create("다른 사람 폴더", QUESTION_SET, 0, anotherOwnerId);
      given(commonFolderRepository.findById(folderId)).willReturn(Optional.of(anotherFolder));

      // when & then
      assertThatThrownBy(() -> commonFolderService.deleteFolder(ownerId, folderId))
          .isInstanceOf(FolderNotFoundException.class);
      verify(commonFolderRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("실패 - 기본 폴더는 삭제할 수 없다")
    void deleteFolder_Fail_WhenDeletingDefaultFolder() {
      // given
      Long ownerId = 1L;
      Long folderId = CommonFolder.DEFAULT_FOLDER_ID;

      // when & then
      assertThatThrownBy(() -> commonFolderService.deleteFolder(ownerId, folderId))
          .isInstanceOf(InvalidFolderOperationException.class);
      verify(commonFolderRepository, never()).deleteById(anyLong());
    }
  }
}
