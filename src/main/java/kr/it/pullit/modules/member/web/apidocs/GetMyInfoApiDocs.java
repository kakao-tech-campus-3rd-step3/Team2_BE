package kr.it.pullit.modules.member.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import kr.it.pullit.modules.member.web.dto.MemberInfoResponse;
import org.springframework.http.ProblemDetail;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "내 정보 조회",
    description =
        """
            현재 인증된 사용자의 본인 정보(ID, 이름, 이메일)를 조회합니다.

            [Request]
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, 사용자의 상세 정보를 반환합니다.""",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "내 정보 조회 성공",
      content =
          @Content(
              schema = @Schema(implementation = MemberInfoResponse.class),
              examples =
                  @ExampleObject(
                      name = "내 정보 조회 예시",
                      value =
                          """
                          {
                            "id": 1,
                            "name": "홍길동",
                            "email": "gildong@example.com"
                          }
                          """))),
  @ApiResponse(
      responseCode = "404",
      description = "회원을 찾을 수 없음",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
public @interface GetMyInfoApiDocs {}
