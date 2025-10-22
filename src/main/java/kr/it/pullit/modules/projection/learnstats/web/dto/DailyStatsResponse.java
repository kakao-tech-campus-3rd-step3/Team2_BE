package kr.it.pullit.modules.projection.learnstats.web.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record DailyStatsResponse(
    @NotNull LocalDate date, // 날짜
    @PositiveOrZero int count // 풀이한 문제 수
    ) {}
