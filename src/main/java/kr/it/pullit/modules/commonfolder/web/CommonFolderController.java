package kr.it.pullit.modules.commonfolder.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import kr.it.pullit.modules.commonfolder.api.CommonFolderPublicApi;
import kr.it.pullit.modules.commonfolder.api.FolderFacade;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import kr.it.pullit.modules.commonfolder.web.apidocs.CommonFolderApiDocs;
import kr.it.pullit.modules.commonfolder.web.apidocs.CreateFolderApiDocs;
import kr.it.pullit.modules.commonfolder.web.apidocs.DeleteFolderApiDocs;
import kr.it.pullit.modules.commonfolder.web.apidocs.GetFolderByIdApiDocs;
import kr.it.pullit.modules.commonfolder.web.apidocs.GetFolderDeleteWarningApiDocs;
import kr.it.pullit.modules.commonfolder.web.apidocs.GetFoldersApiDocs;
import kr.it.pullit.modules.commonfolder.web.apidocs.UpdateFolderApiDocs;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderResponse;
import kr.it.pullit.modules.commonfolder.web.dto.FolderDeleteWarningResponse;
import kr.it.pullit.modules.commonfolder.web.dto.QuestionSetFolderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Common Folder API", description = "공통 폴더 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common-folders")
@CommonFolderApiDocs
public class CommonFolderController {

  private final CommonFolderPublicApi commonFolderPublicApi;
  private final FolderFacade folderFacade;

  @GetFoldersApiDocs
  @GetMapping
  public ResponseEntity<List<CommonFolderResponse>> getQuestionSetFolders(
      @AuthenticationPrincipal Long memberId, @RequestParam("type") CommonFolderType type) {
    return ResponseEntity.ok(commonFolderPublicApi.getFolders(memberId, type));
  }

  @GetFolderByIdApiDocs
  @GetMapping("/{id}")
  public ResponseEntity<CommonFolderResponse> getQuestionSetFolderById(
      @AuthenticationPrincipal Long memberId, @PathVariable Long id) {
    return ResponseEntity.ok(commonFolderPublicApi.getFolder(memberId, id));
  }

  @CreateFolderApiDocs
  @PostMapping
  public ResponseEntity<Void> createFolder(
      @AuthenticationPrincipal Long memberId,
      @Valid @RequestBody QuestionSetFolderRequest request) {
    CommonFolderResponse response = commonFolderPublicApi.createFolder(memberId, request);
    return ResponseEntity.created(URI.create("/api/common-folders/" + response.id())).build();
  }

  @UpdateFolderApiDocs
  @PatchMapping("/{id}")
  public ResponseEntity<CommonFolderResponse> updateFolder(
      @AuthenticationPrincipal Long memberId,
      @PathVariable Long id,
      @Valid @RequestBody QuestionSetFolderRequest request) {
    return ResponseEntity.ok(commonFolderPublicApi.updateFolder(memberId, id, request));
  }

  @GetFolderDeleteWarningApiDocs
  @GetMapping("/{id}/delete-warning")
  public ResponseEntity<FolderDeleteWarningResponse> getFolderDeleteWarning(
      @AuthenticationPrincipal Long memberId, @PathVariable Long id) {
    long count = folderFacade.getQuestionSetCountInFolder(memberId, id);
    return ResponseEntity.ok(new FolderDeleteWarningResponse(count));
  }

  @DeleteFolderApiDocs
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteFolder(
      @AuthenticationPrincipal Long memberId, @PathVariable Long id) {
    folderFacade.deleteFolderAndContents(memberId, id);
    return ResponseEntity.noContent().build();
  }
}
