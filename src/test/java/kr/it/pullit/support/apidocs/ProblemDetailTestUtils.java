package kr.it.pullit.support.apidocs;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.Arrays;
import java.util.Optional;
import kr.it.pullit.shared.apidocs.ApiDocsGroup;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 * API 응답이 OpenAPI 문서의 ExampleObject와 일치하는지 검증하는 유틸리티 클래스입니다.
 *
 * <p>이 클래스는 테스트에서 실제 API 응답(ProblemDetail 형식)이 API 문서에 명시된 ExampleObject와 일치하는지 자동으로 검증합니다.
 * ExampleObject는 {@link ApiDocsGroup} 어노테이션이 적용된 클래스에서 자동으로 스캔됩니다.
 *
 * <p>사용 예시:
 *
 * <pre>{@code
 * mockMvc.perform(get("/api/common-folders/999"))
 *     .andExpect(
 *         ProblemDetailTestUtils.conformToApiDocs(
 *             "/api/common-folders/999",
 *             "폴더 조회 실패"));
 * }</pre>
 *
 * <p>위 예시는 다음과 같이 동작합니다:
 *
 * <ol>
 *   <li>exampleName("폴더 조회 실패")으로 ExampleObject를 찾습니다
 *   <li>ExampleObject의 instance 필드를 instanceUrl로 치환합니다
 *   <li>실제 응답의 status, content-type, body를 ExampleObject와 비교합니다
 * </ol>
 *
 * @author Hyeonjun0527
 */
public class ProblemDetailTestUtils {

  /**
   * API 응답이 지정된 ExampleObject와 일치하는지 검증하는 ResultMatcher를 생성합니다.
   *
   * @param instanceUrl 실제 요청 URL (ExampleObject의 instance 필드를 치환하는 데 사용됨)
   * @param exampleObjectName ExampleObject의 name (API 문서에서 찾을 예시 이름)
   * @return API 응답을 검증하는 ResultMatcher
   */
  public static ResultMatcher conformToApiDocs(String instanceUrl, String exampleObjectName) {
    return new ProblemDetailNApiDocsMatcher(instanceUrl, exampleObjectName);
  }

  private record ProblemDetailNApiDocsMatcher(String instanceUrl, String exampleName)
      implements ResultMatcher {

    @Override
    public void match(@NotNull MvcResult result) throws Exception {
      ExampleObject spec = findSpecification();
      String expectedJson = prepareExpectedJson(spec);

      assertStatus(result, expectedJson);
      assertContentType(result);
      assertBody(result, expectedJson);
    }

    private ExampleObject findSpecification() {
      // exampleName으로 ExampleObject를 찾기
      return findExampleObjectByName(exampleName)
          .orElseThrow(
              () ->
                  new AssertionError(
                      "예시 이름 '%s'에 해당하는 @ExampleObject를 @ApiDocsGroup 어노테이션이나 컨트롤러 메서드의 ApiDocs 어노테이션에서 찾을 수 없습니다."
                          .formatted(exampleName)));
    }

    /**
     * ExampleObject의 JSON에서 instance 필드를 테스트에서 사용하는 실제 instanceUrl로 치환합니다.
     *
     * <p>ExampleObject에는 특정 경로가 하드코딩되어 있지만, 테스트마다 다른 ID나 경로를 사용할 수 있기 때문에 치환이 필요합니다.
     *
     * @param spec ExampleObject
     * @return instance 필드가 instanceUrl로 치환된 JSON 문자열
     */
    private String prepareExpectedJson(ExampleObject spec) {
      String json = spec.value();

      String currentInstance;
      try {
        currentInstance = JsonPath.read(json, "$.instance");
      } catch (Exception e) {
        currentInstance = null;
      }

      if (currentInstance != null) {
        json = json.replace("\"" + currentInstance + "\"", "\"" + instanceUrl + "\"");
      }

      return json;
    }

    private void assertStatus(MvcResult result, String expectedJson) throws Exception {
      int expectedStatus = JsonPath.read(expectedJson, "$.status");
      status().is(expectedStatus).match(result);
    }

    private void assertContentType(MvcResult result) throws Exception {
      content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON).match(result);
    }

    private void assertBody(MvcResult result, String expectedJson) throws Exception {
      content().json(expectedJson, true).match(result);
    }

    private Optional<ExampleObject> findExampleObjectByName(String exampleName) {
      Reflections reflections = new Reflections("kr.it.pullit", Scanners.TypesAnnotated);

      return reflections.getTypesAnnotatedWith(ApiDocsGroup.class, true).stream()
          .map(Class::getAnnotations)
          .flatMap(Arrays::stream)
          .filter(annotation -> annotation instanceof ApiResponses)
          .map(annotation -> (ApiResponses) annotation)
          .flatMap(apiResponses -> Arrays.stream(apiResponses.value()))
          .map(ApiResponse::content)
          .flatMap(Arrays::stream)
          .flatMap(content -> Arrays.stream(content.examples()))
          .filter(example -> example.name().equals(exampleName))
          .findFirst();
    }
  }
}
