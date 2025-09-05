package kr.it.pullit.platform.storage.s3.client;

import java.net.URL;
import java.time.Duration;
import kr.it.pullit.platform.storage.core.StorageProps;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
public class S3FileStorageClientManagingClient implements FileStorageClient {

  private final StorageProps storageProps;
  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  public S3FileStorageClientManagingClient(StorageProps storageProps) {
    this.storageProps = storageProps;
    this.s3Client = createS3Client();
    this.s3Presigner = createS3Presigner();
  }

  @Override
  public URL generatePresignedUploadUrl(String filePath, String contentType, Duration expiration) {
    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder()
            .bucket(storageProps.getBucketName())
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
        DeleteObjectRequest.builder().bucket(storageProps.getBucketName()).key(filePath).build();

    s3Client.deleteObject(deleteRequest);
  }

  @Override
  public boolean fileExists(String filePath) {
    try (S3Client s3Client = createS3Client()) {
      HeadObjectRequest headRequest =
          HeadObjectRequest.builder().bucket(storageProps.getBucketName()).key(filePath).build();

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
        storageProps.getBucketName(), storageProps.getRegion(), filePath);
  }

  private S3Client createS3Client() {
    return S3Client.builder()
        .region(Region.of(storageProps.getRegion()))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    storageProps.getAccessKey(), storageProps.getSecretKey())))
        .build();
  }

  private S3Presigner createS3Presigner() {
    return S3Presigner.builder()
        .region(Region.of(storageProps.getRegion()))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    storageProps.getAccessKey(), storageProps.getSecretKey())))
        .build();
  }
}
