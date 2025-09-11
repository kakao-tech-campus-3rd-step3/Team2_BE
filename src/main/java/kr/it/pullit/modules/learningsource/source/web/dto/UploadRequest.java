package kr.it.pullit.modules.learningsource.source.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UploadRequest {

  @NotBlank(message = "파일명은 필수입니다")
  private String fileName;

  @NotBlank(message = "콘텐츠 타입은 필수입니다")
  private String contentType;

  @NotNull(message = "파일 크기는 필수입니다")
  @Positive(message = "파일 크기는 양수여야 합니다")
  private Long fileSize;

  public UploadRequest(String fileName, String contentType, Long fileSize) {
    this.fileName = fileName;
    this.contentType = contentType;
    this.fileSize = fileSize;
  }
}
