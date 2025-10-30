package kr.it.pullit.modules.commonfolder.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import kr.it.pullit.modules.commonfolder.exception.CommonFolderErrorCode;
import kr.it.pullit.modules.commonfolder.exception.InvalidFolderOperationException;
import kr.it.pullit.modules.commonfolder.repository.CommonFolderRepository;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderResponse;
import kr.it.pullit.modules.commonfolder.web.dto.QuestionSetFolderRequest;
import kr.it.pullit.support.annotation.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@DisplayName("CommonFolderService 통합 테스트")
class CommonFolderServiceIntegrationTest {

  @Autowired private CommonFolderService commonFolderService;

  @Autowired private CommonFolderRepository commonFolderRepository;

  @Nested
  @DisplayName("폴더 조회")
  class DescribeGetFolders {

    @Test
    @DisplayName("타입으로 조회하면 정렬 순서대로 DTO를 반환한다")
    void getFolders() {
      CommonFolderType type = CommonFolderType.QUESTION_SET;
      commonFolderRepository.save(CommonFolder.create("B", type, 2));
      commonFolderRepository.save(CommonFolder.create("A", type, 0));
      commonFolderRepository.save(CommonFolder.create("C", type, 1));

      List<CommonFolderResponse> responses = commonFolderService.getFolders(type);

      assertThat(responses).hasSize(3);
      assertThat(responses).extracting(CommonFolderResponse::sortOrder).containsExactly(0, 1, 2);
    }

    @Test
    @DisplayName("ID로 조회하면 DTO를 반환한다")
    void getFolder() {
      CommonFolder saved =
          commonFolderRepository.save(CommonFolder.create("폴더", CommonFolderType.QUESTION_SET, 0));

      CommonFolderResponse response = commonFolderService.getFolder(saved.getId());

      assertThat(response.id()).isEqualTo(saved.getId());
      assertThat(response.name()).isEqualTo("폴더");
    }

    @Test
    @DisplayName("엔티티 Optional로도 조회할 수 있다")
    void findFolderEntityById() {
      CommonFolder saved =
          commonFolderRepository.save(CommonFolder.create("엔티티", CommonFolderType.QUESTION_SET, 0));

      Optional<CommonFolder> found = commonFolderService.findFolderEntityById(saved.getId());

      assertThat(found).isPresent();
      assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("존재하지 않는 ID면 Optional.empty()를 반환한다")
    void findFolderEntityById_notFound() {
      Optional<CommonFolder> found = commonFolderService.findFolderEntityById(999L);

      assertThat(found).isEmpty();
    }
  }

  @Nested
  @DisplayName("기본 폴더")
  class DescribeGetOrCreateDefaultQuestionSetFolder {

    @Test
    @DisplayName("기본 폴더가 없으면 새로 생성한다")
    void createWhenMissing() {
      CommonFolder defaultFolder = commonFolderService.getOrCreateDefaultQuestionSetFolder();

      assertThat(defaultFolder.getName()).isEqualTo(CommonFolder.DEFAULT_NAME);
      assertThat(defaultFolder.getType()).isEqualTo(CommonFolderType.QUESTION_SET);
      assertThat(commonFolderRepository.findById(defaultFolder.getId())).isPresent();
    }

    @Test
    @DisplayName("이미 존재하면 기존 폴더를 반환한다")
    void returnExisting() {
      CommonFolder firstCall = commonFolderService.getOrCreateDefaultQuestionSetFolder();

      CommonFolder secondCall = commonFolderService.getOrCreateDefaultQuestionSetFolder();

      assertThat(secondCall.getId()).isEqualTo(firstCall.getId());
    }
  }

  @Nested
  @DisplayName("폴더 생성")
  class DescribeCreateFolder {

    @Test
    @DisplayName("다음 정렬 순서를 부여해 생성한다")
    void createFolder() {
      commonFolderRepository.save(CommonFolder.create("기존", CommonFolderType.QUESTION_SET, 3));

      CommonFolderResponse response =
          commonFolderService.createFolder(
              new QuestionSetFolderRequest("새 폴더", CommonFolderType.QUESTION_SET));

      assertThat(response.sortOrder()).isEqualTo(4);
      assertThat(commonFolderRepository.findById(response.id())).isPresent();
    }
  }

  @Nested
  @DisplayName("폴더 수정")
  class DescribeUpdateFolder {

    @Test
    @DisplayName("기본 폴더가 아니면 이름을 수정한다")
    void updateFolder() {
      CommonFolder folder =
          commonFolderRepository.save(CommonFolder.create("수정전", CommonFolderType.QUESTION_SET, 0));

      CommonFolderResponse response =
          commonFolderService.updateFolder(
              folder.getId(), new QuestionSetFolderRequest("수정후", CommonFolderType.QUESTION_SET));

      assertThat(response.name()).isEqualTo("수정후");
      assertThat(commonFolderRepository.findById(folder.getId()).orElseThrow().getName())
          .isEqualTo("수정후");
    }

    @Test
    @DisplayName("기본 폴더는 수정할 수 없어 예외를 던진다")
    void updateFolder_default() {
      CommonFolder folder =
          commonFolderRepository.save(
              CommonFolder.create(CommonFolder.DEFAULT_NAME, CommonFolderType.QUESTION_SET, 0));

      assertThatThrownBy(
              () ->
                  commonFolderService.updateFolder(
                      folder.getId(),
                      new QuestionSetFolderRequest("변경", CommonFolderType.QUESTION_SET)))
          .isInstanceOf(InvalidFolderOperationException.class)
          .extracting("errorCode")
          .isEqualTo(CommonFolderErrorCode.CANNOT_UPDATE_DEFAULT_FOLDER);
    }
  }

  @Nested
  @DisplayName("폴더 삭제")
  class DescribeDeleteFolder {

    @Test
    @DisplayName("ID로 삭제하면 DB에서도 사라진다")
    void deleteFolder() {
      CommonFolder folder =
          commonFolderRepository.save(CommonFolder.create("삭제", CommonFolderType.QUESTION_SET, 0));

      commonFolderService.deleteFolder(folder.getId());

      assertThat(commonFolderRepository.findById(folder.getId())).isEmpty();
    }
  }
}
