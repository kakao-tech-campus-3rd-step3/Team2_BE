package kr.it.pullit.modules.learningsource.source.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceResponse;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadCompleteRequest;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadRequest;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Learning Source API", description = "학습 자료 소스 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/source")
public class SourceController {

  private final SourcePublicApi sourcePublicApi;

  @PostMapping("/upload")
  public ResponseEntity<SourceUploadResponse> generateUploadUrl(
      @AuthenticationPrincipal Long memberId, @Valid @RequestBody SourceUploadRequest request) {
    SourceUploadResponse uploadResponse =
        sourcePublicApi.generateUploadUrl(
            request.fileName(), request.contentType(), request.fileSize(), memberId);

    return ResponseEntity.ok(uploadResponse);
  }

  @PostMapping("/upload-complete")
  public ResponseEntity<Void> processUploadComplete(
      @AuthenticationPrincipal Long memberId,
      @Valid @RequestBody SourceUploadCompleteRequest request) {
    sourcePublicApi.processUploadComplete(request, memberId);
    return ResponseEntity.ok().build();
  }

  @GetMapping
  public ResponseEntity<List<SourceResponse>> getMySources(@AuthenticationPrincipal Long memberId) {
    return ResponseEntity.ok(sourcePublicApi.getMySources(memberId));
  }

  @DeleteMapping("/{sourceId}")
  public ResponseEntity<Void> deleteSource(
      @PathVariable Long sourceId, @AuthenticationPrincipal Long memberId) {
    sourcePublicApi.deleteSource(sourceId, memberId);
    return ResponseEntity.noContent().build();
  }
}
