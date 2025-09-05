package kr.it.pullit.modules.learningsource.source.service;

import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.web.dto.UploadResponse;
import kr.it.pullit.platform.storage.api.S3PublicApi;
import kr.it.pullit.platform.storage.s3.dto.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SourceService implements SourcePublicApi {

  private final S3PublicApi s3PublicApi;

  @Override
  public UploadResponse generateUploadUrl(
      String fileName, String contentType, Long fileSize, Long memberId) {
    PresignedUrlResponse response =
        s3PublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId);

    return new UploadResponse(response.getUploadUrl(), response.getFilePath());
  }
}
