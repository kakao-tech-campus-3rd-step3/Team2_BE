package kr.it.pullit.platform.storage.s3.client;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import kr.it.pullit.platform.storage.core.S3StorageProps;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
public class S3FileStorageClientManagingClient implements FileStorageClient {

  private final S3StorageProps s3StorageProps;
  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  public S3FileStorageClientManagingClient(S3StorageProps s3StorageProps) {
    this.s3StorageProps = s3StorageProps;
    this.s3Client = createS3Client();
    this.s3Presigner = createS3Presigner();
  }

  @Override
  public URL generatePresignedUploadUrl(String filePath, String contentType, Duration expiration) {
    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder()
            .bucket(s3StorageProps.getBucketName())
            .key(filePath)
            .contentType(contentType)
            .build();

    PutObjectPresignRequest presignRequest =
        PutObjectPresignRequest.builder()
            .signatureDuration(expiration)
            .putObjectRequest(putObjectRequest)
            .build();

    return s3Presigner.presignPutObject(presignRequest).url();
  }

  @Override
  public void deleteFile(String filePath) {
    DeleteObjectRequest deleteRequest =
        DeleteObjectRequest.builder().bucket(s3StorageProps.getBucketName()).key(filePath).build();

    s3Client.deleteObject(deleteRequest);
  }

  @Override
  public boolean fileExists(String filePath) {
    try (S3Client s3Client = createS3Client()) {
      HeadObjectRequest headRequest =
          HeadObjectRequest.builder().bucket(s3StorageProps.getBucketName()).key(filePath).build();

      s3Client.headObject(headRequest);
      return true;
    } catch (NoSuchKeyException e) {
      return false;
    }
  }

  @Override
  public String getFileUrl(String filePath) {
    return String.format(
        "https://%s.s3.%s.amazonaws.com/%s",
        s3StorageProps.getBucketName(), s3StorageProps.getRegion(), filePath);
  }

  @Override
  public InputStream downloadFileAsStream(String filePath) {
    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder().bucket(s3StorageProps.getBucketName()).key(filePath).build();

    int maxRetries = 3;
    int attempt = 0;
    long delay = 1000; // 1초부터 시작

    while (true) {
      try {
        return s3Client.getObject(getObjectRequest);
      } catch (NoSuchKeyException e) {
        attempt++;
        if (attempt >= maxRetries) {
          throw e;
        }
        try {
          Thread.sleep(delay * attempt); // 재시도 간격 증가
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          throw new RuntimeException("S3 파일 다운로드 재시도 중 스레드 인터럽트 발생", ie);
        }
      }
    }
  }

  private S3Client createS3Client() {
    return S3Client.builder()
        .region(Region.of(s3StorageProps.getRegion()))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    s3StorageProps.getAccessKey(), s3StorageProps.getSecretKey())))
        .build();
  }

  private S3Presigner createS3Presigner() {
    return S3Presigner.builder()
        .region(Region.of(s3StorageProps.getRegion()))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    s3StorageProps.getAccessKey(), s3StorageProps.getSecretKey())))
        .build();
  }
}
