package kr.it.pullit.modules.commonfolder.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kr.it.pullit.modules.commonfolder.api.CommonFolderPublicApi;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import kr.it.pullit.modules.commonfolder.repository.CommonFolderRepository;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderRequest;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonFolderService implements CommonFolderPublicApi {

  private static final String DEFAULT_FOLDER_NAME = "전체";
  private final CommonFolderRepository commonFolderRepository;

  @Override
  public List<CommonFolderResponse> getQuestionSetFolders() {
    List<CommonFolder> folders =
        commonFolderRepository.findByTypeOrderBySortOrderAsc(CommonFolderType.QUESTION_SET);
    return folders.stream().map(this::toDto).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public CommonFolder getOrCreateDefaultQuestionSetFolder() {
    return commonFolderRepository
        .findByNameAndType(DEFAULT_FOLDER_NAME, CommonFolderType.QUESTION_SET).orElseGet(() -> {
          int sortOrder = calculateNextSortOrder();
          CommonFolder defaultFolder =
              CommonFolder.create(DEFAULT_FOLDER_NAME, CommonFolderType.QUESTION_SET, sortOrder);
          return commonFolderRepository.save(defaultFolder);
        });
  }

  @Override
  @Transactional
  public CommonFolderResponse createQuestionSetFolder(CommonFolderRequest request) {
    int sortOrder = calculateNextSortOrder();

    CommonFolder folder =
        CommonFolder.create(request.name(), CommonFolderType.QUESTION_SET, sortOrder);

    return toDto(commonFolderRepository.save(folder));
  }

  private int calculateNextSortOrder() {
    return commonFolderRepository.findFirstByTypeOrderBySortOrderDesc(CommonFolderType.QUESTION_SET)
        .map(folder -> folder.getSortOrder() + 1).orElse(0);
  }

  @Override
  @Transactional(readOnly = true)
  public CommonFolderResponse getFolder(Long id) {
    return toDto(findFolderById(id));
  }

  @Override
  public Optional<CommonFolder> findFolderEntityById(Long id) {
    return commonFolderRepository.findById(id);
  }

  @Override
  @Transactional
  public CommonFolderResponse updateFolder(Long id, CommonFolderRequest request) {
    CommonFolder folder = findFolderById(id);
    folder.update(request.name());
    return toDto(commonFolderRepository.save(folder));
  }

  @Override
  @Transactional
  public void deleteFolder(Long id) {
    commonFolderRepository.deleteById(id);
  }

  private CommonFolder findFolderById(Long id) {
    return commonFolderRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("해당 ID의 폴더를 찾을 수 없습니다."));
  }

  private CommonFolderResponse toDto(CommonFolder folder) {
    return new CommonFolderResponse(folder.getId(), folder.getName(), folder.getType(),
        folder.getSortOrder());
  }
}
