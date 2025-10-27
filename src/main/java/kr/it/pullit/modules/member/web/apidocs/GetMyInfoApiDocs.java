package kr.it.pullit.modules.member.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "내 정보 조회",
    description =
        "인증된 사용자의 본인 정보를 조회합니다.\n\n"
            + "[Request]\n"
            + "- 인증 토큰 필요 (Bearer)\n\n"
            + "[Response]\n"
            + "- 성공 시, 사용자의 상세 정보를 반환합니다.",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "성공적으로 회원 정보를 조회함"),
  @ApiResponse(responseCode = "404", description = "해당 ID의 회원을 찾을 수 없음")
})
public @interface GetMyInfoApiDocs {}
