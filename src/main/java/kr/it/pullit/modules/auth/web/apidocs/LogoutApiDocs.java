package kr.it.pullit.modules.auth.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import kr.it.pullit.shared.apidocs.ApiDocsGroup;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiDocsGroup
@Operation(
    summary = "로그아웃",
    description = "서버에 저장된 리프레시 토큰을 삭제하고, 클라이언트의 리프레시 토큰 쿠키를 만료시킵니다.",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponse(responseCode = "204", description = "로그아웃 성공")
public @interface LogoutApiDocs {}
