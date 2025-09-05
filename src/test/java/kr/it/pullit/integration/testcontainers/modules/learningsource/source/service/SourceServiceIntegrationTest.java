package kr.it.pullit.integration.testcontainers.modules.learningsource.source.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.web.dto.UploadResponse;
import kr.it.pullit.support.TestContainerTest;

public class SourceServiceIntegrationTest extends TestContainerTest {

  @Autowired
  private SourcePublicApi sourcePublicApi;

  @Test
  void PDF_파일_업로드_URL_생성_성공() {
    // given
    String fileName = "study-material.pdf";
    String contentType = "application/pdf";
    Long fileSize = 1024L;
    Long memberId = 12345L;

    // when
    UploadResponse result =
        sourcePublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getUploadUrl()).isNotNull();
    assertThat(result.getUploadUrl()).startsWith("https://");
    assertThat(result.getFilePath()).isNotNull();
    assertThat(result.getFilePath()).contains("learning-sources");
    assertThat(result.getFilePath()).endsWith(".pdf");
  }

  @Test
  void 비PDF_파일_업로드_시_검증_실패() {
    // given
    String fileName = "diagram.png";
    String contentType = "image/png";
    Long fileSize = 512L;
    Long memberId = 67890L;

    // when & then
    assertThatThrownBy(
        () -> sourcePublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId))
            .isInstanceOf(IllegalArgumentException.class).hasMessage("PDF 파일만 업로드 가능합니다.");
  }

  @Test
  void 큰_파일_업로드_URL_생성_성공() {
    // given
    String fileName = "large-document.pdf";
    String contentType = "application/pdf";
    Long fileSize = 50 * 1024 * 1024L; // 50MB
    Long memberId = 11111L;

    // when
    UploadResponse result =
        sourcePublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId);

    // then
    assertThat(result.getUploadUrl()).isNotNull();
    assertThat(result.getFilePath()).isNotNull();
    assertThat(result.getFilePath()).contains("learning-sources");
  }

  @Test
  void 다양한_회원의_파일_경로_구분() {
    // given
    String fileName = "test.pdf";
    String contentType = "application/pdf";
    Long fileSize = 1024L;
    Long memberId1 = 100L;
    Long memberId2 = 200L;

    // when
    UploadResponse result1 =
        sourcePublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId1);
    UploadResponse result2 =
        sourcePublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId2);

    // then
    assertThat(result1.getFilePath()).isNotEqualTo(result2.getFilePath());
    assertThat(result1.getUploadUrl()).isNotEqualTo(result2.getUploadUrl());
  }

  @Test
  void 동일한_파일명도_고유한_경로_생성() {
    // given
    String fileName = "duplicate.pdf";
    String contentType = "application/pdf";
    Long fileSize = 1024L;
    Long memberId = 300L;

    // when
    UploadResponse result1 =
        sourcePublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId);
    UploadResponse result2 =
        sourcePublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId);

    // then
    assertThat(result1.getFilePath()).isNotEqualTo(result2.getFilePath());
    assertThat(result1.getUploadUrl()).isNotEqualTo(result2.getUploadUrl());
  }

  @Test
  void 파일_크기_0_검증_실패() {
    // given
    String fileName = "zero-size.pdf";
    String contentType = "application/pdf";
    Long fileSize = 0L;
    Long memberId = 999L;

    // when & then
    assertThatThrownBy(
        () -> sourcePublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId))
            .isInstanceOf(IllegalArgumentException.class).hasMessage("유효하지 않은 파일 크기입니다.");
  }

  @Test
  void 파일_크기_초과_검증_실패() {
    // given
    String fileName = "too-large.pdf";
    String contentType = "application/pdf";
    Long fileSize = 51 * 1024 * 1024L; // 51MB (50MB 초과)
    Long memberId = 999L;

    // when & then
    assertThatThrownBy(
        () -> sourcePublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("파일 크기가 너무 큽니다. 최대 50MB까지 업로드 가능합니다.");
  }

  @Test
  void Presigned_URL_형식_검증() {
    // given
    String fileName = "validation-test.pdf";
    String contentType = "application/pdf";
    Long fileSize = 2048L;
    Long memberId = 999L;

    // when
    UploadResponse result =
        sourcePublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId);

    // then
    String uploadUrl = result.getUploadUrl();
    assertThat(uploadUrl).contains("pullit-uploaded-files");
    assertThat(uploadUrl).contains("X-Amz-Algorithm");
    assertThat(uploadUrl).contains("X-Amz-Credential");
    assertThat(uploadUrl).contains("X-Amz-Date");
    assertThat(uploadUrl).contains("X-Amz-Expires");
    assertThat(uploadUrl).contains("X-Amz-SignedHeaders");
    assertThat(uploadUrl).contains("X-Amz-Signature");
  }

  @Test
  void 파일_경로_정책_검증() {
    // given
    String fileName = "path-policy-test.pdf";
    String contentType = "application/pdf";
    Long fileSize = 1024L;
    Long memberId = 777L;

    // when
    UploadResponse result =
        sourcePublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId);

    // then
    String filePath = result.getFilePath();
    assertThat(filePath).startsWith("learning-sources/");
    assertThat(filePath).matches(".*\\d{4}/\\d{2}/\\d{2}/.*"); // 날짜 경로 포함 확인
    assertThat(filePath).endsWith(".pdf");
  }

  @Test
  void 실제_파일_업로드_테스트() {
    // given
    String fileName = "real-upload-test.pdf";
    String contentType = "application/pdf";
    byte[] testFileContent = "PDF 테스트 파일 내용입니다.".getBytes();
    Long fileSize = (long) testFileContent.length;
    Long memberId = 888L;

    // when: Presigned URL 생성
    UploadResponse result =
        sourcePublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId);

    // then: 생성된 URL로 실제 파일 업로드 시도
    String uploadUrl = result.getUploadUrl();
    String filePath = result.getFilePath();

    assertThat(uploadUrl).isNotNull();
    assertThat(filePath).isNotNull();

    // 실제 HTTP PUT 요청으로 파일 업로드
    boolean uploadSuccess = uploadFileToS3(uploadUrl, testFileContent, contentType);
    assertThat(uploadSuccess).isTrue();

    System.out.println("✅ 파일이 S3에 업로드되었습니다!");
    System.out.println("📁 파일 경로: " + filePath);
    System.out.println("🌐 업로드 URL: " + uploadUrl);
  }

  private boolean uploadFileToS3(String presignedUrl, byte[] fileContent, String contentType) {
    try {
      System.out.println("🔄 S3 업로드 시도 중...");
      System.out.println("📎 Content-Type: " + contentType);
      System.out.println("📦 File Size: " + fileContent.length + " bytes");
      System.out.println(
          "🌐 URL: " + presignedUrl.substring(0, Math.min(presignedUrl.length(), 100)) + "...");

      java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
      java.net.http.HttpRequest request =
          java.net.http.HttpRequest.newBuilder().uri(java.net.URI.create(presignedUrl))
              .PUT(java.net.http.HttpRequest.BodyPublishers.ofByteArray(fileContent))
              .header("Content-Type", contentType).build();

      java.net.http.HttpResponse<String> response =
          client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

      System.out.println("📊 HTTP Status: " + response.statusCode());
      System.out.println("📋 Response Headers: " + response.headers().map());
      System.out.println("📄 Response Body: " + response.body());

      if (response.statusCode() == 200) {
        System.out.println("✅ S3 업로드 성공!");
        return true;
      } else {
        System.err.println("❌ S3 업로드 실패 - HTTP " + response.statusCode());
        return false;
      }
    } catch (Exception e) {
      System.err.println("❌ S3 업로드 예외 발생: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }
}
