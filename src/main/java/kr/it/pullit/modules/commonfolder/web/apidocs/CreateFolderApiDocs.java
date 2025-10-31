package kr.it.pullit.modules.commonfolder.web.apidocs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.http.ProblemDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "새 폴더 생성",
    description =
        """
            인증된 사용자의 새 폴더를 생성합니다.

            [Request]
            - `name`: 생성할 폴더의 이름 (Body, 필수)
            - `type`: `QUESTION_SET` 또는 `LEARNING_SOURCE` (Body, 필수)
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, `201 Created`와 함께 생성된 리소스의 URI를 `Location` 헤더에 반환합니다.""",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponses({
  @ApiResponse(
      responseCode = "201",
      description = "폴더 생성 성공",
      content = @Content(mediaType = "application/json")),
  @ApiResponse(
      responseCode = "400",
      description = "잘못된 요청 (예: 이름 누락)",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ProblemDetail.class),
              examples =
                  @ExampleObject(
                      name = "입력값 유효성 검증 실패",
                      value =
                          """
                              {
                                "type": "about:blank",
                                "title": "Bad Request",
                                "status": 400,
                                "detail": "name: 폴더 이름은 필수입니다.",
                                "instance": "/api/common-folders",
                                "code": "VALIDATION_ERROR"
                              }
                              """)))
})
public @interface CreateFolderApiDocs {}
