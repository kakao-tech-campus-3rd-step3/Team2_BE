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
import kr.it.pullit.modules.questionset.web.dto.response.MarkQuestionsResponse;
import kr.it.pullit.shared.apidocs.ApiDocsGroup;
import org.springframework.http.ProblemDetail;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiDocsGroup
@Operation(
    summary = "문제 채점",
    description =
        """
            인증된 사용자가 제출한 문제 답변을 채점하고 결과를 저장합니다.

            [Request]
            - `List<MarkingRequest>`: 채점할 문제 목록 (Body, 필수)
              - `questionId`: 문제 ID
              - `memberAnswer`: 사용자 답변
              - `memberAnswerType`: 답변 타입 (`boolean`, `string` 등)
            - `isReviewing`: 오답노트 복습 모드 여부 (Query Param, 옵션)
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, `200 OK`와 함께 채점 결과를 반환합니다.""",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "채점 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = MarkQuestionsResponse.class),
              examples =
                  @ExampleObject(
                      name = "채점 결과 응답",
                      value =
                          """
                                {
                                  "results": [
                                    { "questionId": 1, "isCorrect": true },
                                    { "questionId": 2, "isCorrect": false }
                                  ],
                                  "totalQuestions": 2,
                                  "correctCount": 1
                                }
                                """))),
  @ApiResponse(
      responseCode = "400",
      description = "잘못된 요청 (예: 값 누락, 타입 불일치)",
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
                                "type": "about:blank",
                                "title": "Bad Request",
                                "status": 400,
                                "detail": "Validation failure",
                                "instance": "/api/marking"
                              }
                              """),
                @ExampleObject(
                    name = "인수 타입 불일치",
                    ref = "#/components/examples/argumentTypeMismatchExample")
              })),
  @ApiResponse(
      responseCode = "404",
      description = "존재하지 않는 문제",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ProblemDetail.class),
              examples =
                  @ExampleObject(
                      name = "문제 조회 실패",
                      value =
                          """
                              {
                                "type": "about:blank",
                                "title": "Not Found",
                                "status": 404,
                                "detail": "문제를 찾을 수 없습니다.",
                                "instance": "/api/marking",
                                "code": "Q_006"
                              }
                              """)))
})
public @interface MarkQuestionsApiDocs {}
