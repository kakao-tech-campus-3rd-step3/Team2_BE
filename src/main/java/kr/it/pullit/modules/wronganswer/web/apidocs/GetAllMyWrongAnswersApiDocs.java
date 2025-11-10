package kr.it.pullit.modules.wronganswer.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import kr.it.pullit.modules.wronganswer.web.dto.WrongAnswerSetResponse;
import org.springframework.http.ProblemDetail;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "나의 모든 오답노트 조회 (간략 정보)",
    description =
        """
            인증된 사용자의 모든 오답노트(문제집 단위)에 대한 간략한 정보 목록을 조회합니다.
            오답노트 복습을 위해 특정 문제집을 선택하는 UI(예: 드롭다운)를 구현할 때 사용됩니다.

            [Request]
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, 오답노트 목록을 배열 형태로 반환합니다.
            - 오답노트가 하나도 없을 경우 빈 배열을 반환합니다.""")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "조회 성공",
      content =
          @Content(
              array = @ArraySchema(schema = @Schema(implementation = WrongAnswerSetResponse.class)),
              examples =
                  @ExampleObject(
                      name = "오답노트 목록 예시",
                      value =
                          """
                          [
                            {
                              "questionSetId": 1,
                              "questionSetTitle": "Java 기초 문제집",
                              "sourceNames": ["Java.pdf", "객체지향.pdf"],
                              "difficulty": "EASY",
                              "majorTopic": "Java",
                              "incorrectCount": 5,
                              "category": "MULTIPLE_CHOICE"
                            },
                            {
                              "questionSetId": 2,
                              "questionSetTitle": "Spring Core",
                              "sourceNames": ["Spring.pdf"],
                              "difficulty": "NORMAL",
                              "majorTopic": "Spring",
                              "incorrectCount": 3,
                              "category": "SHORT_ANSWER"
                            }
                          ]
                          """))),
  @ApiResponse(
      responseCode = "404",
      description = "회원을 찾을 수 없음",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
public @interface GetAllMyWrongAnswersApiDocs {}
