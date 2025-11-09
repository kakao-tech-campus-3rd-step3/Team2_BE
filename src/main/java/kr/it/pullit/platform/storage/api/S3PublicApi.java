package kr.it.pullit.platform.storage.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import kr.it.pullit.platform.storage.dto.PresignedUrlResponse;
import kr.it.pullit.platform.storage.dto.S3FileMetadata;

public interface S3PublicApi {

  PresignedUrlResponse generateUploadUrl(
      String fileName, String contentType, Long fileSize, Long memberId);

  InputStream downloadFileAsStream(String filePath);

  Path downloadFileToTemp(String filePath) throws IOException;

  boolean fileExists(String filePath);

  void deleteFile(String filePath);

  S3FileMetadata getFileMetadata(String filePath);
}
