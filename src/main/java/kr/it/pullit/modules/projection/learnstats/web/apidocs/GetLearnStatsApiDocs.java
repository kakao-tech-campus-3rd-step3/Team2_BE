package kr.it.pullit.modules.projection.learnstats.web.apidocs;

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
import kr.it.pullit.modules.projection.learnstats.web.dto.LearnStatsResponse;
import org.springframework.http.ProblemDetail;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "종합 학습 통계 조회",
    description =
        """
            특정 회원의 종합적인 학습 통계(총 학습일, 연속 학습일, 풀이한 문제 수 등)를 조회합니다.

            [Request]
            - `memberId`: 조회할 회원의 ID (Path Variable, 필수)
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, 회원의 종합 학습 통계 정보를 담은 `LearnStatsResponse` 객체를 반환합니다.""")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "학습 통계 조회 성공",
      content =
          @Content(
              schema = @Schema(implementation = LearnStatsResponse.class),
              examples =
                  @ExampleObject(
                      name = "종합 학습 통계 예시",
                      value =
                          """
                          {
                            "totalQuestionSetCount": 15,
                            "totalSolvedQuestionSetCount": 10,
                            "totalQuestionCount": 300,
                            "totalSolvedQuestionCount": 250,
                            "totalCorrectQuestionCount": 200,
                            "weeklySolvedQuestionCount": 50,
                            "consecutiveLearningDays": 7,
                            "lastLearningDate": "2025-11-10"
                          }
                          """))),
  @ApiResponse(
      responseCode = "404",
      description = "회원을 찾을 수 없음",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
public @interface GetLearnStatsApiDocs {}
