package kr.it.pullit.modules.questionset.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.FinishReason;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import kr.it.pullit.modules.questionset.api.LlmClient;
import kr.it.pullit.modules.questionset.client.dto.request.LlmGeneratedQuestionRequest;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionResponse;
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

  final String questionId = LlmGeneratedQuestionResponse.Fields.id;
  final String questionTextFieldName = LlmGeneratedQuestionResponse.Fields.questionText;
  final String wrongsFieldName = LlmGeneratedQuestionResponse.Fields.options;
  final String answerFieldName = LlmGeneratedQuestionResponse.Fields.answer;
  final String explanationFieldName = LlmGeneratedQuestionResponse.Fields.explanation;
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
    return Content.fromParts(
        Part.fromBytes(fileDataList.getFirst(), "application/pdf"), Part.fromText(prompt));
  }

  private void handleResponseFinishReason(GenerateContentResponse response) {
    switch (response.finishReason().knownEnum()) {
      case BLOCKLIST -> throw new RuntimeException("Content was filtered");
      case FINISH_REASON_UNSPECIFIED -> throw new RuntimeException("Finish reason unspecified");
      case IMAGE_SAFETY -> throw new RuntimeException("Image safety triggered");
      case LANGUAGE -> throw new RuntimeException("Not allowed language");
      case MALFORMED_FUNCTION_CALL -> throw new RuntimeException("Malformed function call");
      case MAX_TOKENS -> throw new RuntimeException("Max tokens exceeded");
      case OTHER -> throw new RuntimeException("Other finish reason");
      case PROHIBITED_CONTENT -> throw new RuntimeException("Prohibited content");
      case RECITATION -> throw new RuntimeException("Recitation");
      case SAFETY -> throw new RuntimeException("Safety triggered");
      case SPII -> throw new RuntimeException("Spii triggered");
      case UNEXPECTED_TOOL_CALL -> throw new RuntimeException("Unexpected tool call");
      default -> throw new RuntimeException("Unknown finish reason: " + response.finishReason());
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
  public List<LlmGeneratedQuestionResponse> getLlmGeneratedQuestionContent(
      LlmGeneratedQuestionRequest request) {
    Objects.requireNonNull(request, "request cannot be null");

    String model = request.model();
    if (model == null) {
      model = "gemini-2.5-flash-lite";
    }
    Content content = getGeminiContent(request.fileDataList(), request.prompt());

    log.info(
        "\n--- Gemini API Request Parameters ---\n[Model Name] : {}\n[Question Count] : {}\n"
            + "[Prompt Length] : {} characters\n[File Count] : {}{}\n--- End of Parameters ---",
        model,
        request.questionCount(),
        request.prompt() != null ? request.prompt().length() : "null",
        request.fileDataList() != null ? request.fileDataList().size() : "null",
        request.fileDataList() != null && !request.fileDataList().isEmpty()
            ? "\n[File Details] : \n"
                + request.fileDataList().stream()
                    .map(
                        data ->
                            "  - Size: "
                                + (data != null ? data.length : "null")
                                + " bytes, SHA-256: "
                                + calculateSha256(data))
                    .collect(Collectors.joining("\n"))
            : "");

    try {
      GenerateContentResponse response =
          client.models.generateContent(model, content, this.getConfig(request.questionCount()));
      if (response.finishReason().knownEnum() != FinishReason.Known.STOP) {
        handleResponseFinishReason(response);
      }
      return mapper.readValue(response.text(), new TypeReference<>() {});
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse LLM response", e);
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate content", e);
    }
  }
}
