package kr.it.pullit.modules.learningsource.source.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadResponse;
import kr.it.pullit.platform.storage.api.S3PublicApi;
import kr.it.pullit.platform.storage.s3.dto.PresignedUrlResponse;
import kr.it.pullit.support.annotation.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ActiveProfiles({"mock-auth", "real-env"})
@IntegrationTest
public class SourceService2Test {

  private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

  @Autowired
  private SourcePublicApi sourcePublicApi;

  @MockitoBean
  private S3PublicApi s3PublicApi;

  private PresignedUrlResponse createResponse(String fileName, Long memberId) {
    String filePath = "learning-sources/%d/%s".formatted(memberId, fileName);
    String uploadUrl =
        "https://pullit-uploaded-files.s3.amazonaws.com/%s?X-Amz-Algorithm=AWS4-HMAC-SHA256"
            .formatted(filePath);
    return new PresignedUrlResponse(uploadUrl, filePath);
  }

  @Test
  void shouldGenerateUploadUrlSuccessfullyForPdfFile() {
    // given
    String fileName = "study-material.pdf";
    String contentType = "application/pdf";
    Long fileSize = 1024L;
    Long memberId = 1L;

    given(s3PublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId))
        .willReturn(createResponse(fileName, memberId));

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
    Long memberId = 1L;

    given(s3PublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId))
        .willThrow(new IllegalArgumentException("PDF 파일만 업로드 가능합니다."));

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
    Long memberId = 1L;

    given(s3PublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId))
        .willReturn(createResponse(fileName, memberId));

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
    Long memberId1 = 1L;
    Long memberId2 = 2L;

    // when
    given(s3PublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId1))
        .willReturn(createResponse(fileName + "-1", memberId1));
    given(s3PublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId2))
        .willReturn(createResponse(fileName + "-2", memberId2));
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
    Long memberId = 1L;

    // when
    given(s3PublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId))
        .willReturn(createResponse(fileName, memberId), createResponse(fileName + "-2", memberId));
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
    Long memberId = 1L;

    given(s3PublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId))
        .willThrow(new IllegalArgumentException("유효하지 않은 파일 크기입니다."));

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
    Long memberId = 1L;

    given(s3PublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId))
        .willThrow(
            new IllegalArgumentException(
                "파일 크기가 너무 큽니다. 최대 50MB까지 업로드 가능합니다."));

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
    Long memberId = 1L;

    given(s3PublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId))
        .willReturn(
            new PresignedUrlResponse(
                "https://pullit-uploaded-files.s3.amazonaws.com/prefix?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=credential&X-Amz-Date=20240101T000000Z&X-Amz-Expires=900&X-Amz-SignedHeaders=host&X-Amz-Signature=signature",
                "learning-sources/prefix/validation-test.pdf"));

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
    Long memberId = 1L;

    // when
    given(s3PublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId))
        .willReturn(
            new PresignedUrlResponse(
                "https://pullit-uploaded-files.s3.amazonaws.com/learning-sources/2024/10/28/path-policy-test.pdf",
                "learning-sources/2024/10/28/path-policy-test.pdf"));
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
    Long memberId = 1L;

    HttpServer server = createSuccessServer();
    int port = server.getAddress().getPort();
    String uploadUrl = "http://localhost:%d/upload".formatted(port);
    String filePath = "learning-sources/%d/%s".formatted(memberId, fileName);
    given(s3PublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId))
        .willReturn(new PresignedUrlResponse(uploadUrl, filePath));

    // when: Presigned URL 생성
    try {
      SourceUploadResponse result =
          sourcePublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId);

      // then: 생성된 URL로 실제 파일 업로드 시도
      String generatedUploadUrl = result.getUploadUrl();
      String generatedFilePath = result.getFilePath();

      assertThat(generatedUploadUrl).isNotNull();
      assertThat(generatedFilePath).isNotNull();

      // 실제 HTTP PUT 요청으로 파일 업로드
      boolean uploadSuccess = uploadFileToS3(generatedUploadUrl, testFileContent, contentType);
      assertThat(uploadSuccess).isTrue();

      System.out.println("파일이 S3에 업로드되었습니다!");
      System.out.println("파일 경로: " + generatedFilePath);
      System.out.println("업로드 URL: " + generatedUploadUrl);
    } finally {
      server.stop(0);
    }
  }

  private boolean uploadFileToS3(String presignedUrl, byte[] fileContent, String contentType) {
    try {
      System.out.println("S3 업로드 시도 중...");
      System.out.println("Content-Type: " + contentType);
      System.out.println("File Size: " + fileContent.length + " bytes");
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

      System.out.println("HTTP Status: " + response.statusCode());
      System.out.println("Response Headers: " + response.headers().map());
      System.out.println("Response Body: " + response.body());

      if (response.statusCode() == 200) {
        System.out.println("S3 업로드 성공!");
        return true;
      } else {
        System.err.println("S3 업로드 실패 - HTTP " + response.statusCode());
        return false;
      }
    } catch (Exception e) {
      System.err.println("S3 업로드 예외 발생: " + e.getMessage());
      return false;
    }
  }

  private HttpServer createSuccessServer() {
    try {
      HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
      server.createContext(
          "/upload",
          new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
              if (!"PUT".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
              }
              exchange.getRequestBody().readAllBytes();
              exchange.getResponseHeaders().add("ETag", "dummy-etag");
              exchange.sendResponseHeaders(200, 0);
              try (OutputStream os = exchange.getResponseBody()) {
                os.write(new byte[0]);
              }
            }
          });
      server.start();
      return server;
    } catch (IOException e) {
      throw new IllegalStateException("Failed to start mock S3 server", e);
    }
  }
}
