package kr.it.pullit.modules.learningsource.source.web.dto;

import java.util.UUID;
import lombok.Getter;

/**
 * 소스 파일 업로드 응답 DTO
 *
 * <p>
 * S3 Pre-signed URL을 이용한 파일 업로드 프로세스의 첫 번째 단계에서 백엔드가 프론트엔드에 반환하는 응답 데이터를 담고 있습니다.
 * 프론트엔드는 이 응답에 포함된 정보를 사용하여 S3에 직접 파일을 업로드할 수 있습니다.
 * </p>
 *
 * @author Pullit Development Team
 * @since 1.0.0
 */
@Getter
public class SourceUploadResponse {

  /**
   * S3에 파일을 업로드할 수 있는 Pre-signed URL
   *
   * <p>
   * 이 URL은 제한된 시간 동안만 유효하며, 프론트엔드는 이 URL을 사용하여 PUT 요청으로 파일을 S3에 직접 업로드해야 합니다.
   * </p>
   */
  private final String uploadUrl;

  /**
   * S3에 저장될 파일 경로
   *
   * <p>
   * 실제 S3 버킷 내에서 파일이 저장되는 경로입니다. 보안을 위해 원본 파일명 대신 UUID가 포함된 경로가 사용됩니다.
   * </p>
   */
  private final String filePath;

  /**
   * 원본 파일명
   *
   * <p>
   * 사용자가 업로드한 파일의 원래 이름입니다. 데이터베이스에 저장되어 사용자에게 표시됩니다.
   * </p>
   */
  private final String originalName;

  /**
   * 파일의 MIME 타입
   *
   * "application/pdf" S3 업로드 시 Content-Type 헤더로 사용되어야 합니다.
   * </p>
   */
  private final String contentType;

  /**
   * 파일 크기 (바이트 단위)
   *
   * <p>
   * 파일의 실제 크기를 나타냅니다. S3 업로드 검증 및 사용자 표시를 위해 사용됩니다.
   * </p>
   */
  private final Long fileSizeBytes;

  /**
   * 업로드 세션 식별자
   *
   * <p>
   * 각 업로드 요청마다 고유하게 생성되는 UUID입니다. 업로드 완료 알림 시 세션 유효성을 검증하는 데 사용됩니다.
   * </p>
   */
  private final String uploadId;

  public SourceUploadResponse(String uploadUrl, String filePath, String originalName, String contentType,
      Long fileSizeBytes) {
    this.uploadUrl = uploadUrl;
    this.filePath = filePath;
    this.originalName = originalName;
    this.contentType = contentType;
    this.fileSizeBytes = fileSizeBytes;
    this.uploadId = UUID.randomUUID().toString();
  }
}
