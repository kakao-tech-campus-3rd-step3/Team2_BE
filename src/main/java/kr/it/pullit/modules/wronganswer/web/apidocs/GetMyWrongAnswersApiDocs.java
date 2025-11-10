package kr.it.pullit.modules.wronganswer.web.apidocs;

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
import kr.it.pullit.shared.paging.dto.CursorPageResponse;
import org.springframework.http.ProblemDetail;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "나의 오답노트 목록 조회 (페이지네이션)",
    description =
        """
            인증된 사용자의 오답노트 목록을 커서 기반 페이지네이션으로 조회합니다.
            `getAllMyWrongAnswers`와 달리, 페이지네이션을 지원하여 성능을 개선한 최신 API입니다.

            [Request]
            - `cursor`: 다음 페이지 조회를 위한 커서 ID (옵션)
            - `size`: 페이지당 오답노트 개수 (기본값: 20)
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, 오답노트 목록과 다음 페이지 조회를 위한 커서 정보를 반환합니다.""")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "조회 성공. 응답 본문은 `WrongAnswerSetResponse` 객체를 포함하는 커서 페이지네이션 형식입니다.",
      content =
          @Content(
              schema = @Schema(implementation = CursorPageResponse.class),
              examples =
                  @ExampleObject(
                      name = "오답노트 페이징 조회 예시",
                      value =
                          """
                          {
                            "data": [
                              {
                                "questionSetId": 1,
                                "questionSetTitle": "Java 기초 문제집",
                                "sourceNames": ["Java.pdf", "객체지향.pdf"],
                                "difficulty": "EASY",
                                "majorTopic": "Java",
                                "incorrectCount": 5,
                                "category": "MULTIPLE_CHOICE"
                              }
                            ],
                            "nextCursor": 2,
                            "hasNext": true
                          }
                          """))),
  @ApiResponse(
      responseCode = "404",
      description = "회원을 찾을 수 없음",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
public @interface GetMyWrongAnswersApiDocs {}
