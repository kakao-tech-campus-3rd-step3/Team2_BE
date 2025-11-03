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
import kr.it.pullit.modules.questionset.web.dto.response.QuestionResponse;
import org.springframework.http.ProblemDetail;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "문제 수정",
    description =
        """
            문제 내용을 수정합니다.

            [Request]
            - `id`: 수정할 문제 ID (Path, 필수)
            - `questionText`: 수정할 문제 내용 (Body, 필수)
            - `options`: 수정된 보기 목록 (객관식인 경우 필수)
            - `answer`: 수정된 정답 (Body, 필수)
            - `explanation`: 수정된 해설 (Body, 필수)
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, 수정된 문제 정보를 반환합니다.""",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponses(
    value = {
      @ApiResponse(
          responseCode = "200",
          description = "문제 수정 성공",
          content =
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = QuestionResponse.class),
                  examples =
                      @ExampleObject(
                          name = "문제 수정 응답",
                          value =
                              """
                                  {
                                    \"id\": 10,
                                    \"questionType\": \"SHORT_ANSWER\",
                                    \"questionText\": \"Spring Data JPA의 Repository 인터페이스를 상속하면 자동으로 구현되는 기능은?\",
                                    \"answer\": \"기본 CRUD 메서드\",
                                    \"explanation\": \"JpaRepository를 상속하면 save, findAll 등 기본 메서드를 사용할 수 있습니다.\"
                                  }
                                  """))),
      @ApiResponse(
          responseCode = "400",
          description = "요청 본문 검증 실패",
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
                                    \"type\": \"about:blank\",
                                    \"title\": \"Bad Request\",
                                    \"status\": 400,
                                    \"detail\": \"answer: 정답은 필수입니다.\",
                                    \"instance\": \"/api/question/10\",
                                    \"code\": \"VALIDATION_ERROR\"
                                  }
                                  """))),
      @ApiResponse(
          responseCode = "404",
          description = "문제를 찾을 수 없음",
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
                                    \"type\": \"about:blank\",
                                    \"title\": \"Not Found\",
                                    \"status\": 404,
                                    \"detail\": \"문제를 찾을 수 없습니다. (ID: 10)\",
                                    \"instance\": \"/api/question/10\",
                                    \"code\": \"Q_006\"
                                  }
                                  """)))
    })
public @interface UpdateQuestionApiDocs {}
