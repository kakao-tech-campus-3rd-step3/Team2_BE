package kr.it.pullit.modules.commonfolder.web;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import kr.it.pullit.modules.commonfolder.service.CommonFolderService;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderRequest;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/question-set-folders")
public class CommonFolderController {

  private final CommonFolderService commonFolderService;

  @GetMapping
  public ResponseEntity<List<CommonFolderResponse>> getFolders() {
    return ResponseEntity.ok(commonFolderService.getQuestionSetFolders());
  }

  @GetMapping("/{id}")
  public ResponseEntity<CommonFolderResponse> getFolderById(@PathVariable Long id) {
    return ResponseEntity.ok(commonFolderService.getFolder(id));
  }

  @PostMapping
  public ResponseEntity<Void> createFolder(@Valid @RequestBody CommonFolderRequest request) {
    CommonFolderResponse response = commonFolderService.createQuestionSetFolder(request);
    return ResponseEntity.created(URI.create("/api/question-set-folders/" + response.id())).build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<CommonFolderResponse> updateFolder(@PathVariable Long id,
      @Valid @RequestBody CommonFolderRequest request) {
    return ResponseEntity.ok(commonFolderService.updateFolder(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteFolder(@PathVariable Long id) {
    commonFolderService.deleteFolder(id);
    return ResponseEntity.noContent().build();
  }
}
