package kr.it.pullit.platform.migration.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "소스 상태 마이그레이션 실행", description = "오래된 소스 데이터의 상태를 최신 상태로 마이그레이션합니다.")
@ApiResponse(responseCode = "200", description = "마이그레이션 작업이 성공적으로 실행되었습니다.")
public @interface RunSourceStatusMigrationApiDocs {}
