package kr.it.pullit.modules.questionset.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import kr.it.pullit.modules.questionset.client.exception.LlmException;
import kr.it.pullit.modules.questionset.client.exception.LlmResponseParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GeminiClient implements LlmClient {

  private final Client client;
  private final ObjectMapper mapper = new ObjectMapper();

  private final GeminiConfigBuilder configBuilder;

  public GeminiClient(GeminiProperties geminiProperties, GeminiConfigBuilder configBuilder) {
    this.client = new Client.Builder().apiKey(geminiProperties.getApiKey()).build();
    this.configBuilder = configBuilder;
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

  @Override
  public List<LlmGeneratedQuestionResponse> getLlmGeneratedQuestionContent(
      LlmGeneratedQuestionRequest request) {

    validateRequest(request);

    String model = determineModel(request.model());
    Content content = buildContent(request);
    GenerateContentConfig config = configBuilder.build(request.questionCount());

    logRequestDetails(model, request);

    try {
      GenerateContentResponse response = callGeminiApi(model, content, config);
      validateResponse(response);
      return parseResponse(response);

    } catch (IOException e) {
      throw new LlmResponseParseException(e.getMessage(), e);
    } catch (Exception e) {
      throw new LlmException("LLM 콘텐츠 생성 실패", e);
    }
  }

  private void validateRequest(LlmGeneratedQuestionRequest request) {
    Objects.requireNonNull(request, "request cannot be null");
  }

  private String determineModel(String model) {
    if (model == null) {
      return "gemini-2.5-flash-lite";
    }
    return model;
  }

  private Content buildContent(LlmGeneratedQuestionRequest request) {
    return getGeminiContent(request.fileDataList(), request.prompt());
  }

  private GenerateContentResponse callGeminiApi(
      String model, Content content, GenerateContentConfig config) {
    return client.models.generateContent(model, content, config);
  }

  private void validateResponse(GenerateContentResponse response) {
    if (response.finishReason().knownEnum() != FinishReason.Known.STOP) {
      handleResponseFinishReason(response);
    }
  }

  private List<LlmGeneratedQuestionResponse> parseResponse(GenerateContentResponse response)
      throws IOException {
    return mapper.readValue(response.text(), new TypeReference<>() {});
  }

  private void logRequestDetails(String model, LlmGeneratedQuestionRequest request) {
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
}
