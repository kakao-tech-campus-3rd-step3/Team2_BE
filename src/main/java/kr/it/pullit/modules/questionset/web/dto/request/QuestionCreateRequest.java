package kr.it.pullit.modules.questionset.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import lombok.Builder;

@Builder
public record QuestionCreateRequest(
    @Schema(
            description = "문제가 추가될 문제집 ID",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "1")
        @NotNull(message = "문제집 ID는 필수입니다.")
        Long questionSetId,
    @Schema(
            description = "문제 유형",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "MULTIPLE_CHOICE")
        @NotNull(message = "문제 유형은 필수입니다.")
        QuestionType questionType,
    @Schema(
            description = "문제 내용 (텍스트)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "다음 중 Java의 기본 타입이 아닌 것은?")
        @NotBlank(message = "문제 내용은 필수입니다.")
        String questionText,
    @Schema(
            description = "객관식 문제의 보기 목록 (객관식 유형일 때 필수)",
            example = "[\"int\", \"String\", \"boolean\", \"char\"]")
        List<String> options,
    @Schema(description = "문제의 정답", requiredMode = Schema.RequiredMode.REQUIRED, example = "String")
        @NotBlank(message = "정답은 필수입니다.")
        String answer,
    @Schema(
            description = "문제에 대한 해설",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "String은 Java의 참조 타입입니다.")
        @NotBlank(message = "해설은 필수입니다.")
        String explanation) {}
