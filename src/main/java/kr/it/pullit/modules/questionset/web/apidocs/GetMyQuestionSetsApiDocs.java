package kr.it.pullit.modules.questionset.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "나의 문제집 목록 조회 (통계 포함)",
    description = "내가 만든 문제집 목록을 페이지네이션으로 조회합니다. 각 문제집의 진행률 통계가 포함됩니다.")
@ApiResponse(responseCode = "200", description = "조회 성공")
public @interface GetMyQuestionSetsApiDocs {}
