package kr.it.pullit.modules.projection.learnstats.web.dto;

import java.time.LocalDate;

public record DailyStatsResponse(LocalDate date, int count) {}
