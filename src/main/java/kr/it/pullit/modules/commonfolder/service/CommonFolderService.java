package kr.it.pullit.modules.commonfolder.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import kr.it.pullit.modules.commonfolder.api.CommonFolderPublicApi;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import kr.it.pullit.modules.commonfolder.repository.CommonFolderRepository;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderRequest;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonFolderService implements CommonFolderPublicApi {

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
        .findByNameAndType(CommonFolder.DEFAULT_NAME, CommonFolderType.QUESTION_SET)
        .orElseGet(() -> createNewFolder(CommonFolder.DEFAULT_NAME, CommonFolderType.QUESTION_SET));
  }

  @Override
  @Transactional
  public CommonFolderResponse createQuestionSetFolder(CommonFolderRequest request) {
    CommonFolder folder = createNewFolder(request.name(), CommonFolderType.QUESTION_SET);
    return toDto(folder);
  }

  private CommonFolder createNewFolder(String name, CommonFolderType type) {
    int sortOrder = calculateNextSortOrder(type);
    CommonFolder folder = CommonFolder.create(name, type, sortOrder);
    return commonFolderRepository.save(folder);
  }

  private int calculateNextSortOrder(CommonFolderType type) {
    return commonFolderRepository
        .findFirstByTypeOrderBySortOrderDesc(type)
        .map(folder -> folder.getSortOrder() + 1)
        .orElse(0);
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
    return commonFolderRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("해당 ID의 폴더를 찾을 수 없습니다."));
  }

  private CommonFolderResponse toDto(CommonFolder folder) {
    return new CommonFolderResponse(
        folder.getId(), folder.getName(), folder.getType(), folder.getSortOrder());
  }
}
