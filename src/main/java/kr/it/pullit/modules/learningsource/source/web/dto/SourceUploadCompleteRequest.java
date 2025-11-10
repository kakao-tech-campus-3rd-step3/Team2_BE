package kr.it.pullit.modules.learningsource.source.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소스 파일 업로드 완료 요청 DTO
 *
 * <p>S3에 파일 업로드가 완료되었음을 알리는 요청 객체입니다. 프론트엔드는 업로드 URL 요청 시 받은 정보를 이 객체에 담아 전송합니다.
 *
 * @author Pullit Development Team
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
public class SourceUploadCompleteRequest {

  @Schema(
      description = "업로드 세션 식별자 (UUID)",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "18c9c8be-e5d4-4c37-b0c4-ccf3c4fc4b65")
  @NotBlank(message = "업로드 ID는 필수입니다")
  private String uploadId;

  @Schema(
      description = "S3에 저장된 파일 경로",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "source/1/2025/11/10/uuid-filename.pdf")
  @NotBlank(message = "파일 경로는 필수입니다")
  private String filePath;

  @Schema(
      description = "원본 파일명",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "my-document.pdf")
  @NotBlank(message = "원본 파일명은 필수입니다")
  private String originalName;

  @Schema(
      description = "파일의 MIME 타입",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "application/pdf")
  @NotBlank(message = "콘텐츠 타입은 필수입니다")
  private String contentType;

  @Schema(
      description = "파일 크기 (바이트 단위)",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "204800")
  @NotNull(message = "파일 크기는 필수입니다")
  @Positive(message = "파일 크기는 양수여야 합니다")
  private Long fileSizeBytes;

  /**
   * SourceUploadCompleteRequest 생성자
   *
   * @param uploadId 업로드 세션 식별자
   * @param filePath S3 파일 경로
   * @param originalName 원본 파일명
   * @param contentType 파일 MIME 타입
   * @param fileSizeBytes 파일 크기 (바이트)
   */
  public SourceUploadCompleteRequest(
      String uploadId,
      String filePath,
      String originalName,
      String contentType,
      Long fileSizeBytes) {
    this.uploadId = uploadId;
    this.filePath = filePath;
    this.originalName = originalName;
    this.contentType = contentType;
    this.fileSizeBytes = fileSizeBytes;
  }
}
