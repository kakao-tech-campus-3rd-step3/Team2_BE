package kr.it.pullit.modules.wronganswer.web.apidocs;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import kr.it.pullit.shared.apidocs.ApiDocsGroup;
import org.springframework.http.ProblemDetail;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiDocsGroup
@ApiResponses(
    value = {
        @ApiResponse(
            responseCode = "404",
            description = "요청한 오답 데이터를 찾을 수 없음",
            content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProblemDetail.class),
                examples = {
                    @ExampleObject(
                        name = "WRONG_ANSWER_NOT_FOUND",
                        summary = "오답 문제를 찾을 수 없음 (WAE_001)",
                        value =
                            """
                                {
                                  "type": "about:blank",
                                  "title": "Not Found",
                                  "status": 404,
                                  "detail": "오답을 찾을 수 없습니다. (Member ID, Question ID: 91, 100)",
                                  "instance": "/api/wronganswers",
                                  "code": "WAE_001"
                                }
                                """),
                    @ExampleObject(
                        name = "NO_WRONG_ANSWERS_TO_REVIEW",
                        summary = "복습할 오답이 없음 (WAN_002)",
                        value =
                            """
                                {
                                  "type": "about:blank",
                                  "title": "Not Found",
                                  "status": 404,
                                  "detail": "복습할 오답이 없습니다.",
                                  "instance": "/api/wrong-anwsers",
                                  "code": "WAN_002"
                                }
                                """)
                }))
    })
public @interface WrongAnswerApiDocs {
}
