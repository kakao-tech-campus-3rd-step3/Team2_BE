package kr.it.pullit.platform.storage.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import kr.it.pullit.platform.storage.api.S3PublicApi;
import kr.it.pullit.platform.storage.client.FileStorageClient;
import kr.it.pullit.platform.storage.common.FilePathPolicy;
import kr.it.pullit.platform.storage.common.FileValidation;
import kr.it.pullit.platform.storage.common.S3StorageProps;
import kr.it.pullit.platform.storage.dto.PresignedUrlResponse;
import kr.it.pullit.platform.storage.dto.S3FileMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class S3PresignedUrlService implements S3PublicApi {

  private final FileStorageClient fileStorageClient;
  private final FileValidation fileValidation;
  private final FilePathPolicy filePathPolicy;
  private final S3StorageProps s3StorageProps;

  @Override
  public PresignedUrlResponse generateUploadUrl(
      String fileName, String contentType, Long fileSize, Long memberId) {

    fileValidation.validatePdfFile(contentType, fileSize);

    String filePath = filePathPolicy.generateFilePath(fileName, memberId);

    URL presignedUrl =
        fileStorageClient.generatePresignedUploadUrl(
            filePath, contentType, s3StorageProps.getPresignedUrlExpiration());

    return new PresignedUrlResponse(presignedUrl.toString(), filePath);
  }

  @Override
  public InputStream downloadFileAsStream(String filePath) {
    return fileStorageClient.downloadFileAsStream(filePath);
  }

  @Override
  public S3FileMetadata getFileMetadata(String filePath) {
    return fileStorageClient.getFileMetadata(filePath);
  }

  /**
   * S3의 파일을 디스크의 임시 파일로 다운로드합니다. 디스크 공간 고갈을 방지하기 위해, 이 메서드를 호출한 측에서는 반환된 임시 파일을 반드시 사용 후 삭제해야 합니다.
   * Files.delete(path) 또는 Files.deleteIfExists(path)를 사용하세요.
   *
   * @param filePath 다운로드할 S3 파일 경로
   * @return 다운로드된 콘텐츠를 포함하는 임시 파일의 Path
   * @throws IOException 다운로드 또는 파일 생성 실패 시 발생
   */
  @Override
  public Path downloadFileToTemp(String filePath) throws IOException {
    Path tempFile = Files.createTempFile("question-generation-", ".pdf");
    try (InputStream inputStream = fileStorageClient.downloadFileAsStream(filePath)) {
      Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
    }
    return tempFile;
  }

  @Override
  public boolean fileExists(String filePath) {
    return fileStorageClient.fileExists(filePath);
  }

  @Override
  public void deleteFile(String filePath) {
    fileStorageClient.deleteFile(filePath);
  }
}
