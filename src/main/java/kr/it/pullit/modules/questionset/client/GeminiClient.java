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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import kr.it.pullit.modules.questionset.api.LlmClient;
import kr.it.pullit.modules.questionset.api.SseDataCallback;
import kr.it.pullit.modules.questionset.client.dto.LlmGeneratedQuestionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GeminiClient implements LlmClient {

  // TODO: config로 빼기
  @SuppressWarnings({"checkstyle:AbbreviationAsWordInName", "checkstyle:MemberName"})
  final int MIN_OPTION_COUNT = 3;

  @SuppressWarnings({"checkstyle:AbbreviationAsWordInName", "checkstyle:MemberName"})
  final int MAX_OPTION_COUNT = 3;

  final String questionId = LlmGeneratedQuestionDto.Fields.id;
  final String questionTextFieldName = LlmGeneratedQuestionDto.Fields.questionText;
  final String wrongsFieldName = LlmGeneratedQuestionDto.Fields.options;
  final String answerFieldName = LlmGeneratedQuestionDto.Fields.answer;
  final String explanationFieldName = LlmGeneratedQuestionDto.Fields.explanation;
  private final Client client;
  private final ObjectMapper mapper = new ObjectMapper();

  public GeminiClient(GeminiProperties geminiProperties) {
    this.client = new Client.Builder().apiKey(geminiProperties.getApiKey()).build();
  }

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

  private List<Part> getByteParts(List<byte[]> fileDataList) {
    List<Part> parts = new ArrayList<>();
    // TODO: 여러 파일 지원
    for (byte[] fileData : fileDataList) {
      parts.add(Part.fromBytes(fileData, "application/pdf"));
    }

    return parts;
  }

  private Content getGeminiContent(List<byte[]> fileDataList, String prompt) {
    List<Part> byteParts = this.getByteParts(fileDataList);
    byteParts.add(Part.fromText(prompt));
    // return Content.fromParts(byteParts.toArray(new Part[0]));
    return Content.fromParts(
        Part.fromBytes(fileDataList.getFirst(), "application/pdf"), Part.fromText(prompt));
  }

  @Override
  public List<LlmGeneratedQuestionDto> getLlmGeneratedQuestionContent(
      String prompt, List<byte[]> fileDataList, int questionCount, String model) {

    log.info(
        "\n--- Gemini API Request Parameters ---"
            + "\n[Model Name] : "
            + model
            + "\n[Question Count] : "
            + questionCount
            + "\n[Prompt Length] : "
            + (prompt != null ? prompt.length() : "null")
            + " characters"
            + "\n[File Count] : "
            + (fileDataList != null ? fileDataList.size() : "null")
            + (fileDataList != null && !fileDataList.isEmpty()
                ? "\n[File Details] : \n"
                    + fileDataList.stream()
                        .map(
                            data ->
                                "  - Size: "
                                    + (data != null ? data.length : "null")
                                    + " bytes, SHA-256: "
                                    + calculateSha256(data))
                        .collect(Collectors.joining("\n"))
                : "")
            + "\n--- End of Parameters ---");

    if (model == null) {
      model = "gemini-2.5-flash-lite";
    }

    Content content = getGeminiContent(fileDataList, prompt);

    GenerateContentResponse response =
        client.models.generateContent(model, content, this.getConfig(questionCount));
    String result = response.text();

    try {
      return mapper.readValue(result, new TypeReference<>() {});
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse LLM response", e);
    }
  }

  private String calculateSha256(byte[] data) {
    if (data == null) {
      return "null";
    }
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(data);
      StringBuilder hexString = new StringBuilder();
      for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      return "Hashing failed";
    }
  }

  @Override
  public void getLlmGeneratedQuestionStream(
      String prompt,
      List<byte[]> fileDataList,
      int questionCount,
      String model,
      SseDataCallback callback) {
    if (prompt == null) {
      throw new IllegalArgumentException("prompt is null");
    }
    if (callback == null) {
      throw new IllegalArgumentException("callback is null");
    }
    if (model == null) {
      model = "gemini-2.5-flash-lite";
    }

    Content content = getGeminiContent(fileDataList, prompt);

    GenerateContentResponse response =
        client.models.generateContent(model, content, this.getConfig(questionCount));

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
