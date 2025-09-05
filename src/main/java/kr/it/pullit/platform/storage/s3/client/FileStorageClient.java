package kr.it.pullit.platform.storage.s3.client;

import java.net.URL;
import java.time.Duration;

public interface FileStorageClient {

  URL generatePresignedUploadUrl(String filePath, String contentType, Duration expiration);

  void deleteFile(String filePath);

  boolean fileExists(String filePath);

  String getFileUrl(String filePath);
}
