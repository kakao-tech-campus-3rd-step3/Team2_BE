package kr.it.pullit.platform.storage.client;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import kr.it.pullit.platform.storage.dto.S3FileMetadata;

public interface FileStorageClient {

  URL generatePresignedUploadUrl(String filePath, String contentType, Duration expiration);

  void deleteFile(String filePath);

  boolean fileExists(String filePath);

  String getFileUrl(String filePath);

  InputStream downloadFileAsStream(String filePath);

  S3FileMetadata getFileMetadata(String filePath);
}
