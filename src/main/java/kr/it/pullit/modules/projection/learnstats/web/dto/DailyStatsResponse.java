package kr.it.pullit.modules.projection.learnstats.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

/**
 * 일일 학습 통계 응답 DTO
 *
 * @param date 날짜
 * @param count 풀이한 문제 수
 */
public record DailyStatsResponse(@NotNull LocalDate date, @PositiveOrZero int count) {}
