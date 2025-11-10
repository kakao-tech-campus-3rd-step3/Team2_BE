package kr.it.pullit.modules.learningsource.source.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import kr.it.pullit.modules.learningsource.source.constant.SourceStatus;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;

/**
 * 내 학습 소스 조회 응답 DTO
 *
 * @param id 소스 ID
 * @param originalName 소스 명 (원본 파일명)
 * @param sourceFolderName 소스가 포함된 폴더 명
 * @param status 학습 소스 상태
 * @param questionSetCount 해당 소스로 만들어진 문제집 개수
 * @param pageCount PDF 등 문서의 페이지 수
 * @param fileSizeBytes 파일 크기 (byte 단위)
 * @param createdAt 소스 업로드 날짜
 * @param recentQuestionGeneratedAt 해당 소스로 문제집을 생성한 가장 최근 날짜
 */
public record SourceResponse(
    @Schema(description = "소스 ID", example = "1") Long id,
    @Schema(description = "소스 명 (원본 파일명)", example = "my-document.pdf") String originalName,
    @Schema(description = "소스가 포함된 폴더 명", example = "기본 폴더") String sourceFolderName,
    @Schema(description = "학습 소스 상태", example = "READY") SourceStatus status,
    @Schema(description = "해당 소스로 만들어진 문제집 개수", example = "3") Integer questionSetCount,
    @Schema(description = "PDF 등 문서의 페이지 수", example = "50") Integer pageCount,
    @Schema(description = "파일 크기 (byte 단위)", example = "204800") Long fileSizeBytes,
    @Schema(description = "소스 업로드 날짜", example = "2025-01-01")
        @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd",
            timezone = "Asia/Seoul")
        LocalDateTime createdAt,
    @Schema(description = "해당 소스로 문제집을 생성한 가장 최근 날짜", example = "2025-01-10")
        @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd",
            timezone = "Asia/Seoul")
        LocalDateTime recentQuestionGeneratedAt) {
  public static SourceResponse from(Source source) {
    return new SourceResponse(
        source.getId(),
        source.getOriginalName(),
        source.getSourceFolder() != null ? source.getSourceFolder().getName() : null,
        source.getStatus(),
        source.getQuestionSets().size(),
        source.getPageCount(),
        source.getFileSizeBytes(),
        source.getCreatedAt(),
        source.getRecentQuestionGeneratedAt());
  }
}
