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
    summary = "나의 모든 문제집 조회 (간략 정보)",
    description = "내가 만든 모든 문제집의 간략한 정보(ID, 이름) 목록을 조회합니다.")
@ApiResponse(responseCode = "200", description = "조회 성공")
public @interface GetAllMyQuestionSetsApiDocs {}
