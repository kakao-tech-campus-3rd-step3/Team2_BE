package kr.it.pullit.modules.learningsource.source.web.dto;

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

  @NotBlank(message = "업로드 ID는 필수입니다")
  private String uploadId;

  @NotBlank(message = "파일 경로는 필수입니다")
  private String filePath;

  @NotBlank(message = "원본 파일명은 필수입니다")
  private String originalName;

  @NotBlank(message = "콘텐츠 타입은 필수입니다")
  private String contentType;

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
