package kr.it.pullit.modules.questionset.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsResponse;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "나의 모든 문제집 조회 (간략 정보)",
    description =
        """
            [Legacy] 페이지네이션 기반 조회 API가 생기기 전 사용하던 과거의 API이며, 현재는 미사용중입니다.
            인증된 사용자가 생성한 모든 문제집의 정보(ID, 이름 등) 목록을 조회합니다.

            [Request]
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, 문제집 목록을 배열 형태로 반환합니다.
            - 문제집이 하나도 없을 경우 빈 배열을 반환합니다.""")
@ApiResponse(
    responseCode = "200",
    description = "조회 성공",
    content =
        @Content(
            array = @ArraySchema(schema = @Schema(implementation = MyQuestionSetsResponse.class)),
            examples =
                @ExampleObject(
                    name = "나의 모든 문제집 조회 예시",
                    value =
                        """
                        [
                          {
                            "questionSetId": 1,
                            "title": "Java 기초 문제집",
                            "sourceIds": [1, 2],
                            "sourceNames": ["Java.pdf", "객체지향.pdf"],
                            "questionCount": 20,
                            "difficultyType": "EASY",
                            "questionType": "MULTIPLE_CHOICE",
                            "status": "COMPLETE",
                            "learningStatus": "IN_PROGRESS",
                            "commonFolderId": 5,
                            "commonFolderName": "기본 폴더",
                            "createdAt": "2025-01-01T12:00:00"
                          }
                        ]
                        """)))
public @interface GetAllMyQuestionSetsApiDocs {}
