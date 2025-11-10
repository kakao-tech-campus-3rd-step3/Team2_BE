package kr.it.pullit.platform.migration.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "학습 통계 보정 실행", description = "모든 회원의 학습 통계(총 문제, 주간 문제, 연속 학습일)를 재계산하여 보정합니다.")
@ApiResponse(responseCode = "200", description = "보정 작업이 성공적으로 시작되었습니다.")
public @interface RunLearnStatsRecalibrationApiDocs {}
