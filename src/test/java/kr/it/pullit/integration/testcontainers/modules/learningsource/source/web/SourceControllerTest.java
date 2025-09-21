package kr.it.pullit.integration.testcontainers.modules.learningsource.source.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadRequest;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadResponse;
import kr.it.pullit.support.TestContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class SourceControllerTest extends TestContainerTest {

  @Autowired private TestRestTemplate restTemplate;

  @MockitoBean private SourcePublicApi sourcePublicApi;

  @Test
  void shouldReturnPresignedUrlForValidRequest() {
    // given
    SourceUploadRequest request = new SourceUploadRequest("test.pdf", "application/pdf", 1024L);
    SourceUploadResponse mockResponse =
        new SourceUploadResponse(
            "https://s3.example.com/upload",
            "/uploads/test.pdf",
            "test.pdf",
            1234L,
            "application/pdf");
    given(sourcePublicApi.generateUploadUrl("test.pdf", "application/pdf", 1024L, 1L))
        .willReturn(mockResponse);

    // when
    ResponseEntity<SourceUploadResponse> response =
        restTemplate.postForEntity(
            "/api/learning/source/upload", request, SourceUploadResponse.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getUploadUrl()).isEqualTo("https://s3.example.com/upload");
    assertThat(response.getBody().getFilePath()).isEqualTo("/uploads/test.pdf");
  }

  @Test
  void shouldReturnBadRequestWhenFileNameIsMissing() {
    // given
    SourceUploadRequest request = new SourceUploadRequest("", "application/pdf", 1024L);

    // when
    ResponseEntity<String> response =
        restTemplate.postForEntity("/api/learning/source/upload", request, String.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldReturnBadRequestWhenContentTypeIsMissing() {
    // given
    SourceUploadRequest request = new SourceUploadRequest("test.pdf", "", 1024L);

    // when
    ResponseEntity<String> response =
        restTemplate.postForEntity("/api/learning/source/upload", request, String.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldReturnBadRequestWhenFileSizeIsZeroOrLess() {
    // given
    SourceUploadRequest request = new SourceUploadRequest("test.pdf", "application/pdf", 0L);

    // when
    ResponseEntity<String> response =
        restTemplate.postForEntity("/api/learning/source/upload", request, String.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }
}
