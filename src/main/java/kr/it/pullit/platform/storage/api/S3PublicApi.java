package kr.it.pullit.platform.storage.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import kr.it.pullit.platform.storage.s3.dto.PresignedUrlResponse;

public interface S3PublicApi {

  PresignedUrlResponse generateUploadUrl(
      String fileName, String contentType, Long fileSize, Long ownerId);

  InputStream downloadFileAsStream(String filePath);

  Path downloadFileToTemp(String filePath) throws IOException;

  boolean fileExists(String filePath);

  void deleteFile(String filePath);
}
