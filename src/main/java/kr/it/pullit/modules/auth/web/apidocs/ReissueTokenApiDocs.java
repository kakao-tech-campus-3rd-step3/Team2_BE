package kr.it.pullit.modules.auth.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import kr.it.pullit.shared.apidocs.ApiDocsGroup;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiDocsGroup
@Operation(summary = "액세스 토큰 재발급", description = "유효한 리프레시 토큰(쿠키)으로 새로운 액세스 토큰을 발급받습니다.")
@ApiResponse(responseCode = "200", description = "액세스 토큰 재발급 성공")
public @interface ReissueTokenApiDocs {}
