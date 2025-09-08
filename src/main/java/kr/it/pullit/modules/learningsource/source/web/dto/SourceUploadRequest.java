package kr.it.pullit.modules.learningsource.source.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소스 파일 업로드 요청 DTO
 *
 * <p>
 * 사용자가 파일을 업로드하기 위해 필요한 메타데이터를 담는 요청 객체입니다.
 * 프론트엔드에서 HTML5 File API를 통해 얻은 파일 정보를 백엔드로 전송합니다.
 * </p>
 *
 * @author Pullit Development Team
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
public class SourceUploadRequest {

  /**
   * 업로드할 파일의 원본 이름
   *
   * <p>
   * 브라우저에서 file.name 속성을 통해 얻은 값입니다.
   * 예: "PS-0-1. Orientation 및 코딩 테스트 준비하기.pdf"
   * </p>
   */
  @NotBlank(message = "파일명은 필수입니다")
  private String fileName;

  /**
   * 파일의 MIME 타입
   *
   * <p>
   * 브라우저에서 file.type 속성을 통해 얻은 값입니다.
   * 예: "application/pdf", "image/jpeg"
   * </p>
   */
  @NotBlank(message = "콘텐츠 타입은 필수입니다")
  private String contentType;

  /**
   * 파일의 크기 (바이트 단위)
   *
   * <p>
   * 브라우저에서 file.size 속성을 통해 얻은 값입니다.
   * S3 업로드 제한 검증에 사용됩니다.
   * </p>
   */
  @NotNull(message = "파일 크기는 필수입니다")
  @Positive(message = "파일 크기는 양수여야 합니다")
  private Long fileSize;

  /**
   * SourceUploadRequest 생성자
   *
   * @param fileName 업로드할 파일의 원본 이름
   * @param contentType 파일의 MIME 타입
   * @param fileSize 파일의 크기 (바이트)
   */
  public SourceUploadRequest(String fileName, String contentType, Long fileSize) {
    this.fileName = fileName;
    this.contentType = contentType;
    this.fileSize = fileSize;
  }
}
