package kr.it.pullit.modules.questionset.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.genai.types.GenerateContentConfig;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionResponse;
import org.springframework.stereotype.Component;

@Component
public class GeminiConfigBuilder {

  private static final String TYPE = "type";
  private static final String ARRAY = "array";
  private static final String OBJECT = "object";
  private static final String INTEGER = "integer";
  private static final String STRING = "string";
  private static final String MIN_ITEMS = "minItems";
  private static final String MAX_ITEMS = "maxItems";
  private static final String ITEMS = "items";
  private static final String PROPERTIES = "properties";
  private static final String REQUIRED = "required";

  // TODO: config로 빼기
  @SuppressWarnings({"checkstyle:AbbreviationAsWordInName", "checkstyle:MemberName"})
  final int MIN_OPTION_COUNT = 4;

  @SuppressWarnings({"checkstyle:AbbreviationAsWordInName", "checkstyle:MemberName"})
  final int MAX_OPTION_COUNT = 4;

  public GenerateContentConfig build(int questionCount) {
    ImmutableMap<String, Object> schema =
        ImmutableMap.<String, Object>builder()
            .put(TYPE, ARRAY)
            .put(MIN_ITEMS, questionCount)
            .put(MAX_ITEMS, questionCount)
            .put(ITEMS, buildItemsSchema())
            .build();

    return GenerateContentConfig.builder()
        .responseMimeType("application/json")
        .candidateCount(1)
        .responseJsonSchema(schema)
        .build();
  }

  private ImmutableMap<String, Object> buildItemsSchema() {
    return ImmutableMap.of(
        TYPE, OBJECT, PROPERTIES, buildPropertiesMap(), REQUIRED, buildRequiredList());
  }

  private ImmutableMap<String, Object> buildPropertiesMap() {
    return ImmutableMap.<String, Object>builder()
        .put(LlmGeneratedQuestionResponse.Fields.id, ImmutableMap.of(TYPE, INTEGER))
        .put(LlmGeneratedQuestionResponse.Fields.questionText, ImmutableMap.of(TYPE, STRING))
        .put(
            LlmGeneratedQuestionResponse.Fields.options,
            ImmutableMap.of(
                TYPE,
                ARRAY,
                ITEMS,
                ImmutableMap.of(TYPE, STRING),
                MIN_ITEMS,
                MIN_OPTION_COUNT,
                MAX_ITEMS,
                MAX_OPTION_COUNT))
        .put(LlmGeneratedQuestionResponse.Fields.answer, ImmutableMap.of(TYPE, STRING))
        .put(LlmGeneratedQuestionResponse.Fields.explanation, ImmutableMap.of(TYPE, STRING))
        .build();
  }

  private ImmutableList<String> buildRequiredList() {
    return ImmutableList.of(
        LlmGeneratedQuestionResponse.Fields.id,
        LlmGeneratedQuestionResponse.Fields.questionText,
        LlmGeneratedQuestionResponse.Fields.options,
        LlmGeneratedQuestionResponse.Fields.answer,
        LlmGeneratedQuestionResponse.Fields.explanation);
  }
}
