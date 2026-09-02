package kr.it.pullit.platform.storage.common;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.storage.s3")
@Getter
@Setter
public class S3StorageProps {

  private String bucketName;
  private String region = "ap-northeast-2";
  private String accessKey;
  private String secretKey;

  /** Pi 내부 S3 호환 저장소(MinIO)의 endpoint. */
  private String endpoint;

  /** 브라우저가 접근할 수 있는 MinIO reverse proxy endpoint. */
  private String publicEndpoint;

  private Duration presignedUrlExpiration = Duration.ofMinutes(15);
  private String basePath = "learning-sources";
}
