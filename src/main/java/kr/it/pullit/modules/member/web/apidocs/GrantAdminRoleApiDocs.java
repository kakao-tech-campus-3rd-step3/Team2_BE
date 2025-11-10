package kr.it.pullit.modules.member.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.http.ProblemDetail;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "관리자 권한 부여", description = "특정 회원에게 관리자(ADMIN) 역할을 부여합니다.")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "관리자 권한 부여 성공",
      content = @Content(schema = @Schema(hidden = true))),
  @ApiResponse(
      responseCode = "404",
      description = "회원을 찾을 수 없음",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
public @interface GrantAdminRoleApiDocs {}
