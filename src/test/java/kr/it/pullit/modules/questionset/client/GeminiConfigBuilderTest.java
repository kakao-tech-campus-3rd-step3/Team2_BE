package kr.it.pullit.modules.questionset.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.genai.types.GenerateContentConfig;
import java.util.List;
import java.util.Map;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionResponse;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionSetResponse;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GeminiConfigBuilder 단위 테스트")
class GeminiConfigBuilderTest {

  private final GeminiConfigBuilder builder = new GeminiConfigBuilder();

  @Test
  @DisplayName("객관식 문제 스키마에는 options 속성과 최소/최대 개수가 포함된다")
  void multipleChoiceSchemaIncludesOptions() {
    GenerateContentConfig config = builder.build(2, QuestionType.MULTIPLE_CHOICE);

    Map<String, Object> schema = cast(config.responseJsonSchema().orElseThrow());
    Map<String, Object> questionSchema = extractQuestionSchema(schema);
    Map<String, Object> questionProperties = cast(questionSchema.get("properties"));

    assertThat(questionProperties).containsKey(LlmGeneratedQuestionResponse.Fields.options);

    Map<String, Object> options =
        cast(questionProperties.get(LlmGeneratedQuestionResponse.Fields.options));
    assertThat(options)
        .containsEntry("type", "array")
        .containsEntry("minItems", builder.MIN_OPTION_COUNT)
        .containsEntry("maxItems", builder.MAX_OPTION_COUNT);

    List<String> required = cast(questionSchema.get("required"));
    assertThat(required).contains(LlmGeneratedQuestionResponse.Fields.options);
  }

  @Test
  @DisplayName("참거짓 문제 스키마의 answer는 boolean 타입이다")
  void trueFalseSchemaUsesBooleanAnswer() {
    GenerateContentConfig config = builder.build(3, QuestionType.TRUE_FALSE);

    Map<String, Object> schema = cast(config.responseJsonSchema().orElseThrow());
    Map<String, Object> questionSchema = extractQuestionSchema(schema);
    Map<String, Object> properties = cast(questionSchema.get("properties"));
    Map<String, Object> answer = cast(properties.get(LlmGeneratedQuestionResponse.Fields.answer));

    assertThat(answer).containsEntry("type", "boolean");
  }

  @Test
  @DisplayName("단답형 문제 스키마에는 옵션이 필수 항목으로 포함되지 않는다")
  void shortAnswerSchemaDoesNotRequireOptions() {
    GenerateContentConfig config = builder.build(1, QuestionType.SHORT_ANSWER);

    Map<String, Object> schema = cast(config.responseJsonSchema().orElseThrow());
    Map<String, Object> questionSchema = extractQuestionSchema(schema);

    List<String> required = cast(questionSchema.get("required"));
    assertThat(required).doesNotContain(LlmGeneratedQuestionResponse.Fields.options);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> extractQuestionSchema(Map<String, Object> rootSchema) {
    Map<String, Object> properties = cast(rootSchema.get("properties"));
    Map<String, Object> questions =
        cast(properties.get(LlmGeneratedQuestionSetResponse.Fields.questions));
    return cast(questions.get("items"));
  }

  @SuppressWarnings("unchecked")
  private <T> T cast(Object value) {
    return (T) value;
  }
}
