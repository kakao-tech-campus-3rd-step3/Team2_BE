package kr.it.pullit.modules.home.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "헬스 체크", description = "서버의 상태를 확인하는 헬스 체크 엔드포인트입니다.")
@ApiResponse(responseCode = "200", description = "서버 정상")
public @interface HealthCheckApiDocs {}
