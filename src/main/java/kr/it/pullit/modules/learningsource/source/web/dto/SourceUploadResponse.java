package kr.it.pullit.modules.learningsource.source.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Getter;

/**
 * 소스 파일 업로드 응답 DTO
 *
 * <p>S3 Pre-signed URL을 이용한 파일 업로드 프로세스의 첫 번째 단계에서 백엔드가 프론트엔드에 반환하는 응답 데이터를 담고 있습니다. 프론트엔드는 이 응답에
 * 포함된 정보를 사용하여 S3에 직접 파일을 업로드할 수 있습니다.
 *
 * @author Pullit Development Team
 * @since 1.0.0
 */
@Getter
public class SourceUploadResponse {

  @Schema(
      description = "S3에 파일을 업로드할 수 있는 Pre-signed URL",
      example = "https://s3.ap-northeast-2.amazonaws.com/pullit/...")
  private final String uploadUrl;

  @Schema(description = "S3에 저장될 파일 경로", example = "source/1/2025/11/10/uuid-filename.pdf")
  private final String filePath;

  @Schema(description = "원본 파일명", example = "my-document.pdf")
  private final String originalName;

  @Schema(description = "파일의 MIME 타입", example = "application/pdf")
  private final String contentType;

  @Schema(description = "파일 크기 (바이트 단위)", example = "204800")
  private final Long fileSizeBytes;

  @Schema(description = "업로드 세션 식별자 (UUID)", example = "18c9c8be-e5d4-4c37-b0c4-ccf3c4fc4b65")
  private final String uploadId;

  public SourceUploadResponse(
      String uploadUrl,
      String filePath,
      String originalName,
      Long fileSizeBytes,
      String contentType) {
    this.uploadUrl = uploadUrl;
    this.filePath = filePath;
    this.originalName = originalName;
    this.contentType = contentType;
    this.fileSizeBytes = fileSizeBytes;
    this.uploadId = UUID.randomUUID().toString();
  }
}
