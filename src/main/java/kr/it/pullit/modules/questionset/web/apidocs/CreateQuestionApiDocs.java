package kr.it.pullit.modules.questionset.web.apidocs;

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
import org.springframework.http.ProblemDetail;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "문제 생성",
    description =
        """
            문제집에 새 문제를 추가합니다.

            [Request]
            - `questionSetId`: 문제집 ID (Body, 필수)
            - `questionType`: 문제 유형 (`MULTIPLE_CHOICE`, `TRUE_FALSE`, `SHORT_ANSWER`) (Body, 필수)
            - `questionText`: 문제 내용 (Body, 필수)
            - `options`: 객관식 보기 목록 (객관식 문제에만 필수)
            - `answer`: 정답 (Body, 필수)
            - `explanation`: 해설 (Body, 필수)
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, `201 Created`와 함께 생성된 문제의 URI를 Location 헤더에 반환합니다.""",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponses(
    value = {
      @ApiResponse(
          responseCode = "201",
          description = "문제 생성 성공",
          content = @Content(mediaType = "application/json")),
      @ApiResponse(
          responseCode = "400",
          description = "요청 필드 누락 또는 타입 불일치",
          content =
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class),
                  examples = {
                    @ExampleObject(
                        name = "입력값 유효성 검증 실패",
                        value =
                            """
                                  {
                                    \"type\": \"about:blank\",
                                    \"title\": \"Bad Request\",
                                    \"status\": 400,
                                    \"detail\": \"questionText: 문제 내용은 필수입니다.\",
                                    \"instance\": \"/api/question\",
                                    \"code\": \"VALIDATION_ERROR\"
                                  }
                                  """),
                    @ExampleObject(
                        name = "인수 타입 불일치",
                        ref = "#/components/examples/argumentTypeMismatchExample")
                  })),
      @ApiResponse(
          responseCode = "404",
          description = "문제집을 찾을 수 없음",
          content =
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class),
                  examples =
                      @ExampleObject(
                          name = "문제집 조회 실패",
                          value =
                              """
                                  {
                                    \"type\": \"about:blank\",
                                    \"title\": \"Not Found\",
                                    \"status\": 404,
                                    \"detail\": \"문제집을 찾을 수 없습니다. (ID: 99)\",
                                    \"instance\": \"/api/question\",
                                    \"code\": \"QSE_001\"
                                  }
                                  """)))
    })
public @interface CreateQuestionApiDocs {}
