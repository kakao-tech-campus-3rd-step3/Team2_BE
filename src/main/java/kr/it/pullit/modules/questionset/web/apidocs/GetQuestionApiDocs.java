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
    summary = "문제 단건 조회",
    description =
        """
            문제 ID로 단일 문제를 조회합니다.

            [Request]
            - `id`: 조회할 문제 ID (Path, 필수)
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, 문제의 상세 정보를 반환합니다.""",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponses(
    value = {
      @ApiResponse(
          responseCode = "200",
          description = "문제 조회 성공",
          content =
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = QuestionResponse.class),
                  examples =
                      @ExampleObject(
                          name = "문제 응답 예시",
                          value =
                              """
                                  {
                                    \"id\": 10,
                                    \"questionType\": \"MULTIPLE_CHOICE\",
                                    \"questionText\": \"Spring Bean의 기본 스코프는 무엇인가요?\",
                                    \"options\": [\"singleton\", \"prototype\"],
                                    \"answer\": \"singleton\",
                                    \"explanation\": \"Spring Bean은 기본적으로 싱글톤으로 관리됩니다.\"
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
public @interface GetQuestionApiDocs {}
