package kr.it.pullit.modules.learningsource.source.web;

import jakarta.validation.Valid;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.web.dto.UploadRequest;
import kr.it.pullit.modules.learningsource.source.web.dto.UploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<UploadResponse> generateUploadUrl(
      @Valid @RequestBody UploadRequest request) {
    // TODO: 실제 인증 구현 후 현재 사용자 ID 가져오기
    Long memberId = 1L;

    return ResponseEntity.ok(
        sourcePublicApi.generateUploadUrl(
            request.getFileName(), request.getContentType(), request.getFileSize(), memberId));
  }
}
