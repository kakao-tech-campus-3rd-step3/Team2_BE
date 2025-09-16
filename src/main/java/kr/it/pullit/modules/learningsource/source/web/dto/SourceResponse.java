package kr.it.pullit.modules.learningsource.source.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import kr.it.pullit.modules.learningsource.source.constant.SourceStatus;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;

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
    Long id,
    String originalName,
    String sourceFolderName,
    SourceStatus status,
    Integer questionSetCount,
    Integer pageCount,
    Long fileSizeBytes,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDateTime createdAt,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDateTime recentQuestionGeneratedAt) {
  public static SourceResponse from(Source source) {
    LocalDateTime recentQuestionGeneratedAt =
        source.getQuestionSets().stream()
            .map(QuestionSet::getCreatedAt)
            .max(LocalDateTime::compareTo)
            .orElse(null);

    return new SourceResponse(
        source.getId(),
        source.getOriginalName(),
        source.getSourceFolder() != null ? source.getSourceFolder().getName() : null,
        source.getStatus(),
        source.getQuestionSets().size(),
        source.getPageCount(),
        source.getFileSizeBytes(),
        source.getCreatedAt(),
        recentQuestionGeneratedAt);
  }
}
