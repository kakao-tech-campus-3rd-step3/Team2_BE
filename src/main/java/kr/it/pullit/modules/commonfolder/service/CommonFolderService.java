package kr.it.pullit.modules.commonfolder.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import kr.it.pullit.modules.commonfolder.api.CommonFolderPublicApi;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import kr.it.pullit.modules.commonfolder.domain.enums.FolderScope;
import kr.it.pullit.modules.commonfolder.exception.CommonFolderErrorCode;
import kr.it.pullit.modules.commonfolder.exception.FolderNotFoundException;
import kr.it.pullit.modules.commonfolder.exception.InvalidFolderOperationException;
import kr.it.pullit.modules.commonfolder.repository.CommonFolderRepository;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderResponse;
import kr.it.pullit.modules.commonfolder.web.dto.CreateFolderRequest;
import kr.it.pullit.modules.commonfolder.web.dto.UpdateFolderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonFolderService implements CommonFolderPublicApi {

  private final CommonFolderRepository commonFolderRepository;

  @Override
  @Transactional
  public void createInitialFolders(Long ownerId) {
    createNewFolder(
        CommonFolder.DEFAULT_NAME, ownerId, CommonFolderType.QUESTION_SET, FolderScope.ALL, true);
    createNewFolder(
        CommonFolder.DEFAULT_NAME, ownerId, CommonFolderType.WRONG_ANSWER, FolderScope.ALL, true);
  }

  @Override
  public List<CommonFolderResponse> getFolders(Long ownerId, CommonFolderType type) {
    List<CommonFolder> folders =
        commonFolderRepository.findByOwnerIdAndTypeOrderBySortOrderAsc(ownerId, type);
    return folders.stream().map(this::toDto).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public CommonFolder getOrCreateDefaultQuestionSetFolder(Long ownerId) {
    return commonFolderRepository
        .findByOwnerIdAndTypeAndScope(ownerId, CommonFolderType.QUESTION_SET, FolderScope.ALL)
        .orElseGet(
            () ->
                createNewFolder(
                    CommonFolder.DEFAULT_NAME,
                    ownerId,
                    CommonFolderType.QUESTION_SET,
                    FolderScope.ALL,
                    false));
  }

  @Override
  @Transactional
  public CommonFolderResponse createFolder(Long ownerId, CreateFolderRequest request) {
    CommonFolder folder =
        createNewFolder(request.name(), ownerId, request.type(), FolderScope.CUSTOM, false);
    return toDto(folder);
  }

  private CommonFolder createNewFolder(
      String name, Long ownerId, CommonFolderType type, FolderScope scope, boolean isInitial) {
    int sortOrder = calculateNextSortOrder(ownerId, type, isInitial);
    CommonFolder folder = CommonFolder.create(name, type, scope, sortOrder, ownerId);
    return commonFolderRepository.save(folder);
  }

  private int calculateNextSortOrder(Long ownerId, CommonFolderType type, boolean isInitial) {
    if (isInitial) {
      return 0;
    }
    return commonFolderRepository
        .findFirstByOwnerIdAndTypeOrderBySortOrderDesc(ownerId, type)
        .map(folder -> folder.getSortOrder() + 1)
        .orElse(0);
  }

  @Override
  @Transactional(readOnly = true)
  public CommonFolderResponse getFolder(Long ownerId, Long id) {
    return toDto(getFolderByIdAndOwner(id, ownerId));
  }

  @Override
  public Optional<CommonFolder> findFolderEntityById(Long ownerId, Long id) {
    return commonFolderRepository
        .findById(id)
        .filter(folder -> folder.getOwnerId().equals(ownerId));
  }

  @Override
  @Transactional
  public CommonFolderResponse updateFolder(Long ownerId, Long id, UpdateFolderRequest request) {
    CommonFolder folder = getFolderByIdAndOwner(id, ownerId);

    if (folder.getScope() == FolderScope.ALL) {
      throw new InvalidFolderOperationException(CommonFolderErrorCode.CANNOT_UPDATE_DEFAULT_FOLDER);
    }

    String newName = request.name() != null ? request.name() : folder.getName();
    CommonFolderType newType = request.type() != null ? request.type() : folder.getType();

    folder.update(newName, newType);

    return toDto(commonFolderRepository.save(folder));
  }

  @Override
  @Transactional
  public void deleteFolder(Long ownerId, Long id) {
    CommonFolder folder = getFolderByIdAndOwner(id, ownerId);

    if (folder.getScope() == FolderScope.ALL) {
      throw new InvalidFolderOperationException(CommonFolderErrorCode.CANNOT_DELETE_DEFAULT_FOLDER);
    }
    commonFolderRepository.deleteById(id);
  }

  private CommonFolder getFolderByIdAndOwner(Long id, Long ownerId) {
    return commonFolderRepository
        .findByIdAndOwnerId(id, ownerId)
        .orElseThrow(() -> FolderNotFoundException.byId(id));
  }

  private CommonFolderResponse toDto(CommonFolder folder) {
    return new CommonFolderResponse(
        folder.getId(),
        folder.getName(),
        folder.getType(),
        folder.getScope(),
        folder.getSortOrder());
  }
}
