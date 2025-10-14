package kr.it.pullit.platform.storage.api;

import java.io.InputStream;
import kr.it.pullit.platform.storage.s3.dto.PresignedUrlResponse;

public interface S3PublicApi {

  PresignedUrlResponse generateUploadUrl(
      String fileName, String contentType, Long fileSize, Long ownerId);

  InputStream downloadFileAsStream(String filePath);

  boolean fileExists(String filePath);
}
