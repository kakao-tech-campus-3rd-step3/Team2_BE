package kr.it.pullit.integration.testcontainers.modules.learningsource.source.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadResponse;
import kr.it.pullit.support.TestContainerTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Disabled("CI/CD 환경에서 실제 S3와 연동 테스트는 LocalStack 도입 후 재활성화 예정")
public class SourceServiceIntegrationTest extends TestContainerTest {

  private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

  @Autowired private SourcePublicApi sourcePublicApi;

  @Test
  void shouldGenerateUploadUrlSuccessfullyForPdfFile() {
    // given
    String fileName = "study-material.pdf";
    String contentType = "application/pdf";
    Long fileSize = 1024L;
    Long memberId = 12345L;

    // when
    SourceUploadResponse result =
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
  void shouldFailValidationForNonPdfFileUpload() {
    // given
    String fileName = "diagram.png";
    String contentType = "image/png";
    Long fileSize = 512L;
    Long memberId = 67890L;

    // when & then
    assertThatThrownBy(
            () -> sourcePublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("PDF 파일만 업로드 가능합니다.");
  }

  @Test
  void shouldGenerateUploadUrlSuccessfullyForLargeFile() {
    // given
    String fileName = "large-document.pdf";
    String contentType = "application/pdf";
    Long fileSize = 50 * 1024 * 1024L; // 50MB
    Long memberId = 11111L;

    // when
    SourceUploadResponse result =
        sourcePublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId);

    // then
    assertThat(result.getUploadUrl()).isNotNull();
    assertThat(result.getFilePath()).isNotNull();
    assertThat(result.getFilePath()).contains("learning-sources");
  }

  @Test
  void shouldCreateDistinctFilePathsForDifferentMembers() {
    // given
    String fileName = "test.pdf";
    String contentType = "application/pdf";
    Long fileSize = 1024L;
    Long memberId1 = 100L;
    Long memberId2 = 200L;

    // when
    SourceUploadResponse result1 =
        sourcePublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId1);
    SourceUploadResponse result2 =
        sourcePublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId2);

    // then
    assertThat(result1.getFilePath()).isNotEqualTo(result2.getFilePath());
    assertThat(result1.getUploadUrl()).isNotEqualTo(result2.getUploadUrl());
  }

  @Test
  void shouldCreateUniquePathForSameFileName() {
    // given
    String fileName = "duplicate.pdf";
    String contentType = "application/pdf";
    Long fileSize = 1024L;
    Long memberId = 300L;

    // when
    SourceUploadResponse result1 =
        sourcePublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId);
    SourceUploadResponse result2 =
        sourcePublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId);

    // then
    assertThat(result1.getFilePath()).isNotEqualTo(result2.getFilePath());
    assertThat(result1.getUploadUrl()).isNotEqualTo(result2.getUploadUrl());
  }

  @Test
  void shouldFailValidationForZeroFileSize() {
    // given
    String fileName = "zero-size.pdf";
    String contentType = "application/pdf";
    Long fileSize = 0L;
    Long memberId = 999L;

    // when & then
    assertThatThrownBy(
            () -> sourcePublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("유효하지 않은 파일 크기입니다.");
  }

  @Test
  void shouldFailValidationForExceededFileSize() {
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
  void shouldVerifyPresignedUrlFormat() {
    // given
    String fileName = "validation-test.pdf";
    String contentType = "application/pdf";
    Long fileSize = 2048L;
    Long memberId = 999L;

    // when
    SourceUploadResponse result =
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
  void shouldVerifyFilePathPolicy() {
    // given
    String fileName = "path-policy-test.pdf";
    String contentType = "application/pdf";
    Long fileSize = 1024L;
    Long memberId = 777L;

    // when
    SourceUploadResponse result =
        sourcePublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId);

    // then
    String filePath = result.getFilePath();
    assertThat(filePath).startsWith("learning-sources/");
    assertThat(filePath).matches(".*\\d{4}/\\d{2}/\\d{2}/.*"); // 날짜 경로 포함 확인
    assertThat(filePath).endsWith(".pdf");
  }

  @Test
  void shouldUploadFileToS3Successfully() {
    // given
    String fileName = "real-upload-test.pdf";
    String contentType = "application/pdf";
    byte[] testFileContent = "PDF 테스트 파일 내용입니다.".getBytes();
    Long fileSize = (long) testFileContent.length;
    Long memberId = 888L;

    // when: Presigned URL 생성
    SourceUploadResponse result =
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

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(java.net.URI.create(presignedUrl))
              .PUT(HttpRequest.BodyPublishers.ofByteArray(fileContent))
              .header("Content-Type", contentType)
              .build();

      HttpResponse<String> response =
          HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

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
      return false;
    }
  }
}
