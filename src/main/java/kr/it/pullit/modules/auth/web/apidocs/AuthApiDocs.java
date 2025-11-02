package kr.it.pullit.modules.auth.web.apidocs;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import kr.it.pullit.shared.apidocs.ApiDocsGroup;
import org.springframework.http.ProblemDetail;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiDocsGroup
@ApiResponses(
    value = {
      @ApiResponse(
          responseCode = "401",
          description = "인증 실패 (토큰 오류)",
          content =
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class),
                  examples = {
                    @ExampleObject(
                        name = "INVALID_ACCESS_TOKEN",
                        summary = "유효하지 않은 액세스 토큰 (AUTH007)",
                        value =
                            """
                            {
                              "type": "about:blank",
                              "title": "Unauthorized",
                              "status": 401,
                              "detail": "액세스 토큰이 유효하지 않습니다.",
                              "instance": "/api/some-path",
                              "code": "AUTH007"
                            }
                            """),
                    @ExampleObject(
                        name = "INVALID_REFRESH_TOKEN",
                        summary = "유효하지 않은 리프레시 토큰 (AUTH006)",
                        value =
                            """
                            {
                              "type": "about:blank",
                              "title": "Unauthorized",
                              "status": 401,
                              "detail": "리프레시 토큰이 유효하지 않습니다.",
                              "instance": "/auth/refresh",
                              "code": "AUTH006"
                            }
                            """)
                  }))
    })
public @interface AuthApiDocs {}
