package kr.it.pullit.modules.questionset.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import kr.it.pullit.modules.questionset.api.LlmClient;
import kr.it.pullit.modules.questionset.api.SseDataCallback;
import kr.it.pullit.modules.questionset.client.dto.LlmGeneratedQuestionDto;

public class GeminiClient implements LlmClient {
  // TODO: config로 빼기
  final int MIN_OPTION_COUNT = 4;
  final int MAX_OPTION_COUNT = 4;
  final String questionId = LlmGeneratedQuestionDto.Fields.id;
  final String questionTextFieldName = LlmGeneratedQuestionDto.Fields.questionText;
  final String wrongsFieldName = LlmGeneratedQuestionDto.Fields.wrongs;
  final String answerFieldName = LlmGeneratedQuestionDto.Fields.answer;
  final String explanationFieldName = LlmGeneratedQuestionDto.Fields.explanation;
  private final Client client = new Client();
  private final ObjectMapper mapper = new ObjectMapper();

  private GenerateContentConfig getConfig(int questionCount) {
    ImmutableMap<String, Object> schema =
        ImmutableMap.of(
            "type",
            "array",
            "minItems",
            questionCount,
            "maxItems",
            questionCount,
            "items",
            ImmutableMap.of(
                "type",
                "object",
                "properties",
                ImmutableMap.of(
                    questionId,
                    ImmutableMap.of("type", "integer"),
                    questionTextFieldName,
                    ImmutableMap.of("type", "string"),
                    wrongsFieldName,
                    ImmutableMap.of(
                        "type",
                        "array",
                        "items",
                        ImmutableMap.of("type", "string"),
                        "minItems",
                        MIN_OPTION_COUNT,
                        "maxItems",
                        MAX_OPTION_COUNT),
                    answerFieldName,
                    ImmutableMap.of("type", "string"),
                    explanationFieldName,
                    ImmutableMap.of("type", "string")),
                "required",
                ImmutableList.of(
                    questionId,
                    questionTextFieldName,
                    wrongsFieldName,
                    answerFieldName,
                    explanationFieldName)));

    return GenerateContentConfig.builder()
        .responseMimeType("application/json")
        .candidateCount(1)
        .responseJsonSchema(schema)
        .build();
  }

  // TODO: SuppressWarnings 제거
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  @Override
  public List<LlmGeneratedQuestionDto> getLlmGeneratedQuestionContent(
      String prompt, String pdfFilePath, String model) {
    if (model == null) {
      model = "gemini-2.5-flash-lite";
    }

    try {
      byte[] pdfData = Files.readAllBytes(Paths.get(pdfFilePath));
      Content content =
          Content.fromParts(Part.fromBytes(pdfData, "application/pdf"), Part.fromText(prompt));

      GenerateContentResponse response =
          client.models.generateContent(model, content, this.getConfig(5));

      String result = response.text();

      return mapper.readValue(result, new TypeReference<List<LlmGeneratedQuestionDto>>() {});
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void getLlmGeneratedQuestionStream(
      String prompt, String filePath, int questionCount, String model, SseDataCallback callback) {
    if (prompt == null) {
      throw new IllegalArgumentException("prompt is null");
    }
    if (filePath == null) {
      throw new IllegalArgumentException("filePath is null");
    }
    if (callback == null) {
      throw new IllegalArgumentException("callback is null");
    }
    if (model == null) {
      model = "gemini-2.5-flash-lite";
    }

    byte[] pdfData;
    try {
      pdfData = Files.readAllBytes(Paths.get(filePath));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Content content =
        Content.fromParts(Part.fromBytes(pdfData, "application/pdf"), Part.fromText(prompt));

    ResponseStream<GenerateContentResponse> responseStream =
        client.models.generateContentStream(model, content, this.getConfig(questionCount));
    JsonStreamParser jsonStreamParser = new JsonStreamParser();

    for (GenerateContentResponse res : responseStream) {
      List<String> jsonStrings = jsonStreamParser.findCompleteJsonObject(res.text());
      for (String jsonStr : jsonStrings) {
        callback.onData(jsonStr);
      }
    }
    responseStream.close();
  }
}
