package kr.it.pullit.platform.storage.s3.service;

import java.net.URL;
import kr.it.pullit.platform.storage.api.S3PublicApi;
import kr.it.pullit.platform.storage.core.FilePathPolicy;
import kr.it.pullit.platform.storage.core.FileValidation;
import kr.it.pullit.platform.storage.core.StorageProps;
import kr.it.pullit.platform.storage.s3.client.FileStorageClient;
import kr.it.pullit.platform.storage.s3.dto.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class S3PresignedUrlService implements S3PublicApi {

  private final FileStorageClient fileStorageClient;
  private final FileValidation fileValidation;
  private final FilePathPolicy filePathPolicy;
  private final StorageProps storageProps;

  public PresignedUrlResponse generateUploadUrl(
      String fileName, String contentType, Long fileSize, Long memberId) {
    // 파일 검증
    fileValidation.validatePdfFile(fileName, contentType, fileSize);

    // 파일 경로 생성
    String filePath = filePathPolicy.generateFilePath(fileName, memberId);

    // Presigned URL 생성
    URL presignedUrl =
        fileStorageClient.generatePresignedUploadUrl(
            filePath, contentType, storageProps.getPresignedUrlExpiration());

    return new PresignedUrlResponse(presignedUrl.toString(), filePath);
  }


}
