package kr.it.pullit.modules.questionset.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;

@Builder
public record QuestionCreateRequest(
    @NotNull(message = "문제집 ID는 필수입니다.") Long questionSetId,
    @NotBlank(message = "문제 내용은 필수입니다.") String questionText,
    List<String> options,
    @NotBlank(message = "정답은 필수입니다.") String answer,
    @NotBlank(message = "해설은 필수입니다.") String explanation) {}
