package kr.it.pullit.modules.learningsource.source.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadRequest;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadResponse;
import kr.it.pullit.support.annotation.AuthenticatedMvcSliceTest;
import kr.it.pullit.support.security.WithMockMember;
import kr.it.pullit.support.test.ControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@AuthenticatedMvcSliceTest(controllers = SourceController.class)
public class SourceControllerTest extends ControllerTest {

  @MockitoBean private SourcePublicApi sourcePublicApi;

  @Test
  @WithMockMember
  @DisplayName("학습 자료 업로드 URL을 성공적으로 생성한다")
  void shouldGenerateUploadUrlSuccessfully() throws Exception {
    // given
    var request = new SourceUploadRequest("test.pdf", "application/pdf", 1234L);
    var mockResponse =
        new SourceUploadResponse(
            "https://s3.example.com/upload",
            "/uploads/test.pdf",
            "test.pdf",
            1234L,
            "application/pdf");

    given(
            sourcePublicApi.generateUploadUrl(
                request.fileName(), request.contentType(), request.fileSize(), 1L))
        .willReturn(mockResponse);

    // when & then
    mockMvc
        .perform(
            post("/api/learning/source/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.uploadUrl").value("https://s3.example.com/upload"))
        .andExpect(jsonPath("$.filePath").value("/uploads/test.pdf"));
  }

  @Test
  @WithMockMember
  @DisplayName("파일 이름이 누락된 경우 400 Bad Request를 반환한다")
  void shouldReturnBadRequestWhenFileNameIsMissing() throws Exception {
    // given
    SourceUploadRequest request = new SourceUploadRequest("", "application/pdf", 1024L);

    // when & then
    mockMvc
        .perform(
            post("/api/learning/source/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockMember
  @DisplayName("Content Type이 누락된 경우 400 Bad Request를 반환한다")
  void shouldReturnBadRequestWhenContentTypeIsMissing() throws Exception {
    // given
    SourceUploadRequest request = new SourceUploadRequest("test.pdf", "", 1024L);

    // when & then
    mockMvc
        .perform(
            post("/api/learning/source/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockMember
  @DisplayName("파일 크기가 0 이하인 경우 400 Bad Request를 반환한다")
  void shouldReturnBadRequestWhenFileSizeIsZeroOrLess() throws Exception {
    // given
    SourceUploadRequest request = new SourceUploadRequest("test.pdf", "application/pdf", 0L);

    // when & then
    mockMvc
        .perform(
            post("/api/learning/source/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
