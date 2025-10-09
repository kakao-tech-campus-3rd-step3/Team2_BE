package kr.it.pullit.modules.questionset.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.genai.types.GenerateContentConfig;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionResponse;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionSetResponse;
import org.springframework.stereotype.Component;

/**
 * Gemini API의 {@link GenerateContentConfig}를 생성하는 빌더 클래스 이 클래스는 AI가 반환해야 할 JSON의 스키마를 정의한다.
 *
 * <h3>예상 AI 응답 JSON 구조:</h3>
 *
 * <pre>{@code
 * {
 *   "title": "AI가 생성한 문제집 제목",
 *   "questions": [
 *     {
 *       "id": 1,
 *       "questionText": "첫 번째 문제의 내용입니다.",
 *       "options": ["선택지 1", "선택지 2", "선택지 3", "선택지 4"],
 *       "answer": "정답 선택지",
 *       "explanation": "이 문제에 대한 상세한 해설입니다."
 *     },
 *     {
 *       "id": 2,
 *       "questionText": "두 번째 문제의 내용입니다.",
 *       "options": ["선택지 A", "선택지 B", "선택지 C", "선택지 D"],
 *       "answer": "정답 선택지",
 *       "explanation": "이 문제에 대한 상세한 해설입니다."
 *     }
 *   ]
 * }
 * }</pre>
 */
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
    ImmutableMap<String, Object> schema = buildRootSchema(questionCount);

    return GenerateContentConfig.builder()
        .responseMimeType("application/json")
        .candidateCount(1)
        .responseJsonSchema(schema)
        .build();
  }

  private ImmutableMap<String, Object> buildRootSchema(int questionCount) {
    return ImmutableMap.<String, Object>builder()
        .put(TYPE, OBJECT)
        .put(
            PROPERTIES,
            ImmutableMap.of(
                LlmGeneratedQuestionSetResponse.Fields.title,
                buildTitleSchema(),
                LlmGeneratedQuestionSetResponse.Fields.questions,
                buildQuestionsSchema(questionCount)))
        .put(
            REQUIRED,
            ImmutableList.of(
                LlmGeneratedQuestionSetResponse.Fields.title,
                LlmGeneratedQuestionSetResponse.Fields.questions))
        .build();
  }

  private ImmutableMap<String, Object> buildQuestionsSchema(int questionCount) {
    return ImmutableMap.<String, Object>builder()
        .put(TYPE, ARRAY)
        .put(MIN_ITEMS, questionCount)
        .put(MAX_ITEMS, questionCount)
        .put(ITEMS, buildQuestionSchema())
        .build();
  }

  private ImmutableMap<String, Object> buildTitleSchema() {
    return ImmutableMap.of(TYPE, STRING);
  }

  private ImmutableMap<String, Object> buildQuestionSchema() {
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
