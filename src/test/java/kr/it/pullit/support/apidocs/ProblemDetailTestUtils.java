package kr.it.pullit.support.apidocs;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import kr.it.pullit.shared.apidocs.ApiDocsGroup;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

public class ProblemDetailTestUtils {

  public static ResultMatcher conformToApiDocs(String instanceUrl, String exampleName) {
    return new ProblemDetailNApiDocsMatcher(instanceUrl, exampleName);
  }

  private record ProblemDetailNApiDocsMatcher(String instanceUrl, String exampleName)
      implements ResultMatcher {

    @Override
    public void match(@NotNull MvcResult result) throws Exception {
      ExampleObject spec = findSpecification(result);
      String expectedJson = prepareExpectedJson(spec);

      assertStatus(result, expectedJson);
      assertContentType(result);
      assertBody(result, expectedJson);
    }

    private ExampleObject findSpecification(MvcResult result) throws Exception {
      String jsonResponse = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
      String actualCode = JsonPath.read(jsonResponse, "$.code");

      return findExampleObjectByErrorCode(actualCode)
          .orElseThrow(
              () ->
                  new AssertionError(
                      "\"코드 '%s'에 해당하는 @ExampleObject를 @ApiDocsGroup 어노테이션에서 찾을 수 없습니다.\""
                          .formatted(actualCode)));
    }

    private String prepareExpectedJson(ExampleObject spec) {
      return spec.value().replaceAll("\"/api/some-path\"", "\"" + instanceUrl + "\"");
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

    private Optional<ExampleObject> findExampleObjectByErrorCode(String errorCode) {
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
          .filter(
              example -> {
                String exampleErrorCode = JsonPath.read(example.value(), "$.code");
                return exampleErrorCode.equals(errorCode);
              })
          .findFirst();
    }
  }
}
