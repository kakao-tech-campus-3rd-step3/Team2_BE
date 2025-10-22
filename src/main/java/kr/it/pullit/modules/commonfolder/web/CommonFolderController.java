package kr.it.pullit.modules.commonfolder.web;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import kr.it.pullit.modules.commonfolder.api.CommonFolderPublicApi;
import kr.it.pullit.modules.commonfolder.api.FolderFacade;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderRequest;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderResponse;
import kr.it.pullit.modules.commonfolder.web.dto.FolderDeleteWarningResponse;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/question-set-folders")
public class CommonFolderController {

  private final CommonFolderPublicApi commonFolderPublicApi;
  private final QuestionSetPublicApi questionSetPublicApi;
  private final FolderFacade folderFacade;

  @GetMapping("/{id}/delete-warning")
  public ResponseEntity<FolderDeleteWarningResponse> getFolderDeleteWarning(@PathVariable Long id) {
    long count = folderFacade.getQuestionSetCountInFolder(id);
    return ResponseEntity.ok(new FolderDeleteWarningResponse(count));
  }

  @GetMapping
  public ResponseEntity<List<CommonFolderResponse>> getFolders() {
    return ResponseEntity.ok(commonFolderPublicApi.getQuestionSetFolders());
  }

  @GetMapping("/{id}")
  public ResponseEntity<CommonFolderResponse> getFolderById(@PathVariable Long id) {
    return ResponseEntity.ok(commonFolderPublicApi.getFolder(id));
  }

  @GetMapping("/{folderId}/question-sets")
  public ResponseEntity<List<MyQuestionSetsResponse>> getQuestionSetsByFolder(
      @AuthenticationPrincipal Long memberId, @PathVariable Long folderId) {
    return ResponseEntity.ok(questionSetPublicApi.getQuestionSetsByFolder(memberId, folderId));
  }

  @PostMapping
  public ResponseEntity<Void> createFolder(@Valid @RequestBody CommonFolderRequest request) {
    CommonFolderResponse response = commonFolderPublicApi.createQuestionSetFolder(request);
    return ResponseEntity.created(URI.create("/api/question-set-folders/" + response.id())).build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<CommonFolderResponse> updateFolder(@PathVariable Long id,
      @Valid @RequestBody CommonFolderRequest request) {
    return ResponseEntity.ok(commonFolderPublicApi.updateFolder(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteFolder(@PathVariable Long id) {
    folderFacade.deleteFolderAndContents(id);
    return ResponseEntity.noContent().build();
  }
}
