package kr.it.pullit.modules.home.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "루트 경로 조회", description = "API 서버의 동작 여부를 확인하는 기본 엔드포인트입니다.")
@ApiResponse(responseCode = "200", description = "서버 동작 중")
public @interface HomeApiDocs {}
