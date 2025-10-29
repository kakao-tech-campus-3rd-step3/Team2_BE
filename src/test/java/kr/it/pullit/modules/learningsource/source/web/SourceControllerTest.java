package kr.it.pullit.modules.learningsource.source.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.constant.SourceStatus;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceResponse;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadRequest;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadCompleteRequest;
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

  @Test
  @WithMockMember
  @DisplayName("업로드 완료 요청을 처리하고 200 OK를 반환한다")
  void shouldProcessUploadCompleteSuccessfully() throws Exception {
    // given
    var request =
        new SourceUploadCompleteRequest(
            "upload-1",
            "learning-sources/completed.pdf",
            "completed.pdf",
            "application/pdf",
            2048L);

    willDoNothing()
        .given(sourcePublicApi)
        .processUploadComplete(any(SourceUploadCompleteRequest.class), eq(1L));

    // when & then
    mockMvc
        .perform(
            post("/api/learning/source/upload-complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    then(sourcePublicApi)
        .should()
        .processUploadComplete(
            argThat(r ->
                r.getUploadId().equals("upload-1")
                    && r.getFilePath().equals("learning-sources/completed.pdf")),
            eq(1L));
  }

  @Test
  @WithMockMember
  @DisplayName("업로드 완료 요청이 유효하지 않으면 400 Bad Request를 반환한다")
  void shouldReturnBadRequestWhenUploadCompletePayloadInvalid() throws Exception {
    // given
    String payload =
        """
        {
          "uploadId": "",
          "filePath": "",
          "originalName": "",
          "contentType": "",
          "fileSizeBytes": 0
        }
        """;

    // when & then
    mockMvc
        .perform(
            post("/api/learning/source/upload-complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockMember
  @DisplayName("내 학습 소스 목록을 조회한다")
  void shouldReturnMySources() throws Exception {
    // given
    var response =
        new SourceResponse(
            1L,
            "my.pdf",
            "기본 폴더",
            SourceStatus.READY,
            2,
            10,
            4096L,
            LocalDateTime.of(2024, 1, 1, 0, 0),
            LocalDateTime.of(2024, 1, 2, 0, 0));

    given(sourcePublicApi.getMySources(1L)).willReturn(List.of(response));

    // when & then
    mockMvc
        .perform(get("/api/learning/source"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].originalName").value("my.pdf"))
        .andExpect(jsonPath("$[0].sourceFolderName").value("기본 폴더"));
  }

  @Test
  @WithMockMember
  @DisplayName("소스를 삭제하면 204 No Content를 반환한다")
  void shouldDeleteSource() throws Exception {
    // given
    willDoNothing().given(sourcePublicApi).deleteSource(5L, 1L);

    // when & then
    mockMvc.perform(delete("/api/learning/source/{sourceId}", 5L))
        .andExpect(status().isNoContent());

    then(sourcePublicApi).should().deleteSource(5L, 1L);
  }
}
