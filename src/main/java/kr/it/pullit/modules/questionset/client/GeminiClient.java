package kr.it.pullit.modules.questionset.client;

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
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionSetResponse;
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

  @Override
  public LlmGeneratedQuestionSetResponse getLlmGeneratedQuestionContent(
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
      throw LlmResponseParseException.create(e);
    } catch (Exception e) {
      throw LlmException.withCause(e);
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

  private Content getGeminiContent(List<byte[]> fileDataList, String prompt) {
    List<Part> parts = getByteParts(fileDataList);
    parts.add(Part.fromText(prompt));
    Part[] partArray = parts.toArray(new Part[0]);
    return Content.fromParts(partArray);
  }

  private List<Part> getByteParts(List<byte[]> fileDataList) {
    List<Part> parts = new ArrayList<>();
    // TODO: 여러 파일 지원
    for (byte[] fileData : fileDataList) {
      parts.add(Part.fromBytes(fileData, "application/pdf"));
    }

    return parts;
  }

  private void validateResponse(GenerateContentResponse response) {
    if (response.finishReason().knownEnum() != FinishReason.Known.STOP) {
      handleResponseFinishReason(response);
    }
  }

  private void handleResponseFinishReason(GenerateContentResponse response) {
    switch (response.finishReason().knownEnum()) {
      case BLOCKLIST -> throw LlmException.generationFailed("Content was filtered");
      case FINISH_REASON_UNSPECIFIED ->
          throw LlmException.generationFailed("Finish reason unspecified");
      case IMAGE_SAFETY -> throw LlmException.generationFailed("Image safety triggered");
      case LANGUAGE -> throw LlmException.generationFailed("Not allowed language");
      case MALFORMED_FUNCTION_CALL ->
          throw LlmException.generationFailed("Malformed function call");
      case MAX_TOKENS -> throw LlmException.generationFailed("Max tokens exceeded");
      case OTHER -> throw LlmException.generationFailed("Other finish reason");
      case PROHIBITED_CONTENT -> throw LlmException.generationFailed("Prohibited content");
      case RECITATION -> throw LlmException.generationFailed("Recitation");
      case SAFETY -> throw LlmException.generationFailed("Safety triggered");
      case SPII -> throw LlmException.generationFailed("Spii triggered");
      case UNEXPECTED_TOOL_CALL -> throw LlmException.generationFailed("Unexpected tool call");
      default ->
          throw LlmException.generationFailed("Unknown finish reason: " + response.finishReason());
    }
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

  private GenerateContentResponse callGeminiApi(
      String model, Content content, GenerateContentConfig config) {
    return client.models.generateContent(model, content, config);
  }

  private LlmGeneratedQuestionSetResponse parseResponse(GenerateContentResponse response)
      throws IOException {
    return mapper.readValue(response.text(), LlmGeneratedQuestionSetResponse.class);
  }

  private String calculateSha256(byte[] data) {
    if (data == null) {
      return "null";
    }
    return toHexString(generateSha256(data));
  }

  private byte[] generateSha256(byte[] data) {
    try {
      return MessageDigest.getInstance("SHA-256").digest(data);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 algorithm not found", e);
    }
  }

  private String toHexString(byte[] hash) {
    String hex = new java.math.BigInteger(1, hash).toString(16);
    // BigInteger가 앞쪽의 0을 생략할 수 있으므로, 64자리(256비트)를 채우도록 패딩
    while (hex.length() < 64) {
      hex = "0" + hex;
    }
    return hex;
  }
}
