package kr.it.pullit.platform.storage.core;

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
  private Duration presignedUrlExpiration = Duration.ofMinutes(15);
  private String basePath = "learning-sources";
}
