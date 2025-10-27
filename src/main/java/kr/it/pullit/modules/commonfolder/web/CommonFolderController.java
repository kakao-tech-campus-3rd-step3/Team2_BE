package kr.it.pullit.modules.commonfolder.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import kr.it.pullit.modules.commonfolder.api.CommonFolderPublicApi;
import kr.it.pullit.modules.commonfolder.api.FolderFacade;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderResponse;
import kr.it.pullit.modules.commonfolder.web.dto.FolderDeleteWarningResponse;
import kr.it.pullit.modules.commonfolder.web.dto.QuestionSetFolderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
public class CommonFolderController {

  private final CommonFolderPublicApi commonFolderPublicApi;
  private final FolderFacade folderFacade;

  @GetMapping
  public ResponseEntity<List<CommonFolderResponse>> getQuestionSetFolders(
      @RequestParam("type") CommonFolderType type) {
    return ResponseEntity.ok(commonFolderPublicApi.getFolders(type));
  }

  @GetMapping("/{id}")
  public ResponseEntity<CommonFolderResponse> getQuestionSetFolderById(@PathVariable Long id) {
    return ResponseEntity.ok(commonFolderPublicApi.getFolder(id));
  }

  @PostMapping
  public ResponseEntity<Void> createFolder(@Valid @RequestBody QuestionSetFolderRequest request) {
    CommonFolderResponse response = commonFolderPublicApi.createFolder(request);
    return ResponseEntity.created(URI.create("/api/common-folders/" + response.id())).build();
  }

  @PatchMapping("/{id}")
  public ResponseEntity<CommonFolderResponse> updateFolder(
      @PathVariable Long id, @Valid @RequestBody QuestionSetFolderRequest request) {
    return ResponseEntity.ok(commonFolderPublicApi.updateFolder(id, request));
  }

  @GetMapping("/{id}/delete-warning")
  public ResponseEntity<FolderDeleteWarningResponse> getFolderDeleteWarning(@PathVariable Long id) {
    long count = folderFacade.getQuestionSetCountInFolder(id);
    return ResponseEntity.ok(new FolderDeleteWarningResponse(count));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteFolder(@PathVariable Long id) {
    folderFacade.deleteFolderAndContents(id);
    return ResponseEntity.noContent().build();
  }
}
