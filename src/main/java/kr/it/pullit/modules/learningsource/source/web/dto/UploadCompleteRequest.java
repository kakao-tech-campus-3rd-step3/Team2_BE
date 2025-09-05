package kr.it.pullit.modules.learningsource.source.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UploadCompleteRequest {

  @NotBlank(message = "파일 경로는 필수입니다")
  private String filePath;

  @NotBlank(message = "원본 파일명은 필수입니다")
  private String originalName;

  @NotBlank(message = "콘텐츠 타입은 필수입니다")
  private String contentType;

  @NotNull(message = "파일 크기는 필수입니다")
  @Positive(message = "파일 크기는 양수여야 합니다")
  private Long fileSizeBytes;

  public UploadCompleteRequest(
      String filePath, String originalName, String contentType, Long fileSizeBytes) {
    this.filePath = filePath;
    this.originalName = originalName;
    this.contentType = contentType;
    this.fileSizeBytes = fileSizeBytes;
  }
}

