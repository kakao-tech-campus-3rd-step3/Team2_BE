package kr.it.pullit.platform.storage.api;

import kr.it.pullit.platform.storage.s3.dto.PresignedUrlResponse;

public interface S3PublicApi {
  PresignedUrlResponse generateUploadUrl(
      String fileName, String contentType, Long fileSize, Long memberId);
}
