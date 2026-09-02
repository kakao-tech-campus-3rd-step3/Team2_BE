package kr.it.pullit.platform.storage.client;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import kr.it.pullit.platform.storage.common.S3StorageProps;
import kr.it.pullit.platform.storage.dto.S3FileMetadata;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
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

    return toPublicUrl(s3Presigner.presignPutObject(presignRequest).url());
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
    String publicEndpoint = s3StorageProps.getPublicEndpoint();
    if (!StringUtils.hasText(publicEndpoint)) {
      return String.format(
          "https://%s.s3.%s.amazonaws.com/%s",
          s3StorageProps.getBucketName(), s3StorageProps.getRegion(), filePath);
    }
    return String.format(
        "%s/%s/%s", publicEndpoint.replaceAll("/+$", ""), s3StorageProps.getBucketName(), filePath);
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

  @Override
  public S3FileMetadata getFileMetadata(String filePath) {
    HeadObjectRequest headRequest =
        HeadObjectRequest.builder().bucket(s3StorageProps.getBucketName()).key(filePath).build();
    HeadObjectResponse response = s3Client.headObject(headRequest);
    return new S3FileMetadata(response.contentLength(), response.lastModified());
  }

  private S3Client createS3Client() {
    S3ClientBuilder builder =
        S3Client.builder()
            .region(Region.of(s3StorageProps.getRegion()))
            .credentialsProvider(createCredentialsProvider());
    applyEndpoint(builder);
    return builder.build();
  }

  private S3Presigner createS3Presigner() {
    S3Presigner.Builder builder =
        S3Presigner.builder()
            .region(Region.of(s3StorageProps.getRegion()))
            .credentialsProvider(createCredentialsProvider());

    String endpoint = s3StorageProps.getEndpoint();
    if (StringUtils.hasText(endpoint)) {
      builder
          .endpointOverride(URI.create(endpoint))
          .serviceConfiguration(
              software.amazon.awssdk.services.s3.S3Configuration.builder()
                  .pathStyleAccessEnabled(true)
                  .build());
    }
    return builder.build();
  }

  /**
   * 브라우저에는 portfolio 경로를 노출하되, 서명은 MinIO가 실제로 받는 내부 endpoint로 만든다. 리버스 프록시는 요청 URI와 Host를 내부
   * endpoint와 동일하게 복원해야 SigV4 검증이 일치한다.
   */
  private URL toPublicUrl(URL signedInternalUrl) {
    String publicEndpoint = s3StorageProps.getPublicEndpoint();
    if (!StringUtils.hasText(publicEndpoint)) {
      return signedInternalUrl;
    }

    try {
      URI signedUri = signedInternalUrl.toURI();
      String query = signedUri.getRawQuery();
      String externalUrl =
          publicEndpoint.replaceAll("/+$", "")
              + signedUri.getRawPath()
              + (query == null ? "" : "?" + query);
      return URI.create(externalUrl).toURL();
    } catch (Exception e) {
      throw new IllegalStateException("외부 MinIO 업로드 URL을 만들지 못했습니다.", e);
    }
  }

  private void applyEndpoint(S3ClientBuilder builder) {
    String endpoint = s3StorageProps.getEndpoint();
    if (!StringUtils.hasText(endpoint)) {
      return;
    }
    builder
        .endpointOverride(URI.create(endpoint))
        .serviceConfiguration(
            software.amazon.awssdk.services.s3.S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build());
  }

  private AwsCredentialsProvider createCredentialsProvider() {
    boolean hasAccessKey = StringUtils.hasText(s3StorageProps.getAccessKey());
    boolean hasSecretKey = StringUtils.hasText(s3StorageProps.getSecretKey());

    if (hasAccessKey != hasSecretKey) {
      throw new IllegalStateException("S3 access key와 secret key는 함께 설정해야 합니다.");
    }

    if (hasAccessKey) {
      return StaticCredentialsProvider.create(
          AwsBasicCredentials.create(s3StorageProps.getAccessKey(), s3StorageProps.getSecretKey()));
    }

    return DefaultCredentialsProvider.create();
  }
}
