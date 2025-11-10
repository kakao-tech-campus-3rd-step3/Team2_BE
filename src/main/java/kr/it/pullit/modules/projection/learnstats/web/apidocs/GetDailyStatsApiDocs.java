package kr.it.pullit.modules.projection.learnstats.web.apidocs;

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
import kr.it.pullit.modules.projection.learnstats.web.dto.DailyStatsResponse;
import org.springframework.http.ProblemDetail;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "일별 학습 통계 조회",
    description =
        """
            특정 기간 동안의 일별 학습 통계(푼 문제 수)를 조회합니다.

            [Request]
            - `memberId`: 조회할 회원의 ID (Path Variable, 필수)
            - `from`: 조회 시작일 (Query Param, 필수, 형식: `YYYY-MM-DD`)
            - `to`: 조회 종료일 (Query Param, 필수, 형식: `YYYY-MM-DD`)
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, 각 날짜별 문제 풀이 수를 담은 `DailyStatsResponse` 객체 배열을 반환합니다.""")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "일별 학습 통계 조회 성공",
      content =
          @Content(
              array = @ArraySchema(schema = @Schema(implementation = DailyStatsResponse.class)),
              examples =
                  @ExampleObject(
                      name = "일별 학습 통계 예시",
                      value =
                          """
                          [
                            {
                              "date": "2025-11-09",
                              "count": 15
                            },
                            {
                              "date": "2025-11-10",
                              "count": 25
                            }
                          ]
                          """))),
  @ApiResponse(
      responseCode = "404",
      description = "회원을 찾을 수 없음",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
public @interface GetDailyStatsApiDocs {}
