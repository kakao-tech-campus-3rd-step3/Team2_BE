package kr.it.pullit.modules.learningsource.source.web;

import jakarta.validation.Valid;
import java.util.List;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceResponse;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadCompleteRequest;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadRequest;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/learning/source")
@RequiredArgsConstructor
public class SourceController {

  private final SourcePublicApi sourcePublicApi;

  @PostMapping("/upload")
  public ResponseEntity<SourceUploadResponse> generateUploadUrl(
      @Valid @RequestBody SourceUploadRequest request) {
    // TODO: 실제 인증 구현 후 현재 사용자 ID 가져오기
    Long memberId = 1L;

    SourceUploadResponse uploadResponse = sourcePublicApi.generateUploadUrl(request.getFileName(),
        request.getContentType(), request.getFileSize(), memberId);

    return ResponseEntity.ok(uploadResponse);
  }

  @PostMapping("/upload-complete")
  public ResponseEntity<Void> processUploadComplete(
      @Valid @RequestBody SourceUploadCompleteRequest request) {
    // TODO: 실제 인증 구현 후 현재 사용자 ID 가져오기
    Long memberId = 1L;

    sourcePublicApi.processUploadComplete(request, memberId);
    return ResponseEntity.ok().build();
  }

  @GetMapping
  public ResponseEntity<List<SourceResponse>> getMySources() {
    // TODO: 실제 인증 구현 후 현재 사용자 ID 가져오기
    Long memberId = 1L;

    return ResponseEntity.ok(sourcePublicApi.getMySources(memberId));
  }
}
