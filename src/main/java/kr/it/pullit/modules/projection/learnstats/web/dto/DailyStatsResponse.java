package kr.it.pullit.modules.projection.learnstats.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

/**
 * 일일 학습 통계 응답 DTO
 *
 * @param date 날짜
 * @param count 풀이한 문제 수
 */
public record DailyStatsResponse(
    @Schema(description = "날짜", example = "2025-11-10") @NotNull LocalDate date,
    @Schema(description = "해당 날짜에 풀이한 문제 수", example = "25") @PositiveOrZero int count) {}
