package kr.it.pullit.modules.learningsource.source.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 소스 파일 업로드 요청 DTO
 *
 * <p>사용자가 파일을 업로드하기 위해 필요한 메타데이터를 담는 요청 객체입니다. 프론트엔드에서 HTML5 File API를 통해 얻은 파일 정보를 백엔드로 전송합니다.
 *
 * <p><b>필드 설명</b>
 *
 * <ul>
 *   <li><b>fileName</b> : 업로드할 파일의 원본 이름 (브라우저의 file.name 속성 값, 예: "PS-0-1. Orientation 및 코딩 테스트
 *       준비하기.pdf")
 *   <li><b>contentType</b> : 파일의 MIME 타입 (브라우저의 file.type 속성 값, 예: "application/pdf")
 *   <li><b>fileSize</b> : 파일 크기 (바이트 단위) (브라우저의 file.size 속성 값, S3 업로드 제한 검증에 사용)
 * </ul>
 *
 * @author Hyeonjun0527
 */
public record SourceUploadRequest(
    @NotBlank(message = "파일명은 필수입니다") String fileName,
    @NotBlank(message = "콘텐츠 타입은 필수입니다") String contentType,
    @NotNull(message = "파일 크기는 필수입니다") @Positive(message = "파일 크기는 양수여야 합니다") Long fileSize) {}
