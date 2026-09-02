package kr.it.pullit.platform.storage.client;

import static org.assertj.core.api.Assertions.assertThat;

import kr.it.pullit.platform.storage.common.S3StorageProps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class S3FileStorageClientManagingClientTest {

  @Test
  @DisplayName("public endpoint가 없으면 기존 AWS S3 파일 URL을 사용한다")
  void getFileUrlFallsBackToAwsWhenPublicEndpointIsBlank() {
    S3StorageProps props = storageProps();
    props.setPublicEndpoint("");
    S3FileStorageClientManagingClient client = new S3FileStorageClientManagingClient(props);

    assertThat(client.getFileUrl("learning-sources/example.pdf"))
        .isEqualTo(
            "https://pullit-test.s3.ap-northeast-2.amazonaws.com/learning-sources/example.pdf");
  }

  @Test
  @DisplayName("public endpoint가 있으면 portfolio proxy URL을 사용한다")
  void getFileUrlUsesConfiguredPublicEndpoint() {
    S3StorageProps props = storageProps();
    props.setPublicEndpoint("https://portfolio.yeon.world/pull-it/storage/");
    S3FileStorageClientManagingClient client = new S3FileStorageClientManagingClient(props);

    assertThat(client.getFileUrl("learning-sources/example.pdf"))
        .isEqualTo(
            "https://portfolio.yeon.world/pull-it/storage/pullit-test/learning-sources/example.pdf");
  }

  private S3StorageProps storageProps() {
    S3StorageProps props = new S3StorageProps();
    props.setBucketName("pullit-test");
    props.setRegion("ap-northeast-2");
    props.setAccessKey("test-access-key");
    props.setSecretKey("test-secret-key");
    return props;
  }
}
