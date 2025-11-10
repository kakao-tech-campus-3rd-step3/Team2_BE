package kr.it.pullit.modules.questionset.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionResponse;
import org.springframework.http.ProblemDetail;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "문제 수정")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "수정 성공",
      content =
          @Content(
              schema = @Schema(implementation = QuestionResponse.class),
              examples =
                  @ExampleObject(
                      name = "문제 수정 응답 예시",
                      value =
                          """
                          {
                            "id": 101,
                            "questionType": "MULTIPLE_CHOICE",
                            "questionText": "다음 중 Java의 참조 타입인 것은?",
                            "options": ["int", "String", "boolean", "char"],
                            "answer": 2,
                            "explanation": "String은 참조 타입입니다. 나머지는 기본 타입입니다."
                          }
                          """))),
  @ApiResponse(
      responseCode = "400",
      description = "유효하지 않은 입력값입니다.",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
  @ApiResponse(
      responseCode = "404",
      description = "수정할 문제를 찾을 수 없는 경우",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
public @interface UpdateQuestionApiDocs {}
