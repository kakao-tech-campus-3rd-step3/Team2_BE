package kr.it.pullit.integration.testcontainers.modules.learningsource.source.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.web.dto.UploadRequest;
import kr.it.pullit.modules.learningsource.source.web.dto.UploadResponse;
import kr.it.pullit.support.TestContainerTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class SourceControllerTest extends TestContainerTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @MockitoBean
  private SourcePublicApi sourcePublicApi;

  @Test
  void 정상적인_업로드_URL_생성_요청() {
    // given
    UploadRequest request = new UploadRequest("test.pdf", "application/pdf", 1024L);
    UploadResponse mockResponse =
        new UploadResponse("https://s3.example.com/upload", "/uploads/test.pdf");
    given(sourcePublicApi.generateUploadUrl("test.pdf", "application/pdf", 1024L, 1L))
        .willReturn(mockResponse);

    // when
    ResponseEntity<UploadResponse> response =
        restTemplate.postForEntity("/api/learning/source/upload", request, UploadResponse.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getUploadUrl()).isEqualTo("https://s3.example.com/upload");
    assertThat(response.getBody().getFilePath()).isEqualTo("/uploads/test.pdf");
  }

  @Test
  void 파일명이_없는_경우_400_오류() {
    // given
    UploadRequest request = new UploadRequest("", "application/pdf", 1024L);

    // when
    ResponseEntity<String> response =
        restTemplate.postForEntity("/api/learning/source/upload", request, String.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void 콘텐츠_타입이_없는_경우_400_오류() {
    // given
    UploadRequest request = new UploadRequest("test.pdf", "", 1024L);

    // when
    ResponseEntity<String> response =
        restTemplate.postForEntity("/api/learning/source/upload", request, String.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void 파일_크기가_0_이하인_경우_400_오류() {
    // given
    UploadRequest request = new UploadRequest("test.pdf", "application/pdf", 0L);

    // when
    ResponseEntity<String> response =
        restTemplate.postForEntity("/api/learning/source/upload", request, String.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }
}
