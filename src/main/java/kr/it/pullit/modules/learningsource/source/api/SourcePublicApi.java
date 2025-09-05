package kr.it.pullit.modules.learningsource.source.api;

import kr.it.pullit.modules.learningsource.source.web.dto.UploadResponse;

public interface SourcePublicApi {

  UploadResponse generateUploadUrl(
      String fileName, String contentType, Long fileSize, Long memberId);
}
