package kr.it.pullit.modules.questionset.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Builder;

@Builder
public record QuestionUpdateRequestDto(
    @Schema(
            description = "수정할 문제 내용",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Java에서 'final' 키워드의 역할이 아닌 것은?")
        @NotBlank(message = "문제 내용은 필수입니다.")
        String questionText,
    @Schema(
            description = "수정할 보기 목록 (객관식 유형일 때 필수)",
            example = "[\"클래스 상속 방지\", \"메소드 오버라이딩 방지\", \"값 변경 방지\", \"객체 생성 방지\"]")
        List<String> options,
    @Schema(
            description = "수정할 정답",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "객체 생성 방지")
        @NotBlank(message = "정답은 필수입니다.")
        String answer,
    @Schema(
            description = "수정할 해설",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "'final' 키워드는 객체 생성을 막는 기능이 없습니다.")
        @NotBlank(message = "해설은 필수입니다.")
        String explanation) {}
