package kr.it.pullit.modules.questionset.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JsonStreamParserTest {

  private final JsonStreamParser parser = new JsonStreamParser();

  @BeforeEach
  void setUp() {
    parser.reset();
  }

  @Test
  @DisplayName("null 입력은 빈 결과를 반환한다")
  void returnsEmptyListForNullInput() {
    List<String> results = parser.findCompleteJsonObject(null);

    assertThat(results).isEmpty();
  }

  @Test
  @DisplayName("스트림 조각을 누적하여 JSON 객체를 추출한다")
  void accumulatesJsonFragments() {
    String fragment1 = "{\"id\":1,\"text\":\"hello";
    String fragment2 = " world\",\"nested\":{\"key\":\"value\"}}";
    String fragment3 = "{\"id\":2}";

    List<String> firstBatch = parser.findCompleteJsonObject(fragment1);
    List<String> secondBatch = parser.findCompleteJsonObject(fragment2);
    List<String> thirdBatch = parser.findCompleteJsonObject(fragment3);

    assertThat(firstBatch).isEmpty();
    assertThat(secondBatch)
        .containsExactly("{\"id\":1,\"text\":\"hello world\",\"nested\":{\"key\":\"value\"}}");
    assertThat(thirdBatch).containsExactly("{\"id\":2}");
  }

  @Test
  @DisplayName("문자열 내부의 중괄호는 깊이 계산에 영향을 주지 않는다")
  void ignoresBracesInsideStrings() {
    String jsonWithBracesInString = "{\"text\":\"value { not a brace }\"}";

    List<String> results = parser.findCompleteJsonObject(jsonWithBracesInString);

    assertThat(results).containsExactly(jsonWithBracesInString);
  }

  @Test
  @DisplayName("이스케이프된 따옴표는 문자열 종료로 처리되지 않는다")
  void respectsEscapedQuotesInsideString() {
    String jsonWithEscapedQuotes = "{\"text\":\"value with \\\"quoted\\\" braces {braces}\"}";
    List<String> results = parser.findCompleteJsonObject(jsonWithEscapedQuotes);
    assertThat(results).containsExactly(jsonWithEscapedQuotes);
  }

  @Test
  @DisplayName("현재까지 누적된 스트림을 조회할 수 있다")
  void exposesCurrentAccumulatedStream() {
    parser.findCompleteJsonObject("{\"key\":1");

    assertThat(parser.getJsonObjStream()).contains("{\"key\":1");
  }

  @Test
  @DisplayName("reset 호출 시 누적된 상태가 초기화된다")
  void resetClearsAccumulatedState() {
    parser.findCompleteJsonObject("{\"first\":true");

    parser.reset();

    assertThat(parser.getJsonObjStream()).hasValue("");

    List<String> results = parser.findCompleteJsonObject("{\"second\":false}");

    assertThat(results).containsExactly("{\"second\":false}");
  }
}
