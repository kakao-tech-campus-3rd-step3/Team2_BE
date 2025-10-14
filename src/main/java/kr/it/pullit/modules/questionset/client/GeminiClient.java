package kr.it.pullit.modules.questionset.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.FinishReason;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

  private static final int GEMINI_API_TIMEOUT_MINUTES = 2;
  private final Client client;
  private final ObjectMapper mapper = new ObjectMapper();
  private final GeminiConfigBuilder configBuilder;

  public GeminiClient(GeminiProperties geminiProperties, GeminiConfigBuilder configBuilder) {
    HttpOptions httpOptions =
        HttpOptions.builder()
            .timeout((int) Duration.ofMinutes(GEMINI_API_TIMEOUT_MINUTES).toMillis())
            .build();
    this.client =
        Client.builder().apiKey(geminiProperties.getApiKey()).httpOptions(httpOptions).build();
    this.configBuilder = configBuilder;
  }

  // TODO: 리팩토링 대상.
  @Override
  public LlmGeneratedQuestionSetResponse getLlmGeneratedQuestionContent(
      LlmGeneratedQuestionRequest request) {

    validateRequest(request);

    String model = determineModel(request.model());
    Content content = buildContent(request);
    GenerateContentConfig config =
        configBuilder.build(
            request.specification().questionCount(), request.specification().questionType());

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

  private Content getGeminiContent(List<InputStream> fileDataList, String prompt) {
    List<Part> parts = getByteParts(fileDataList);
    parts.add(Part.fromText(prompt));
    Part[] partArray = parts.toArray(new Part[0]);
    return Content.fromParts(partArray);
  }

  private List<Part> getByteParts(List<InputStream> fileDataList) {
    List<Part> parts = new ArrayList<>();
    // TODO: 여러 파일 지원
    for (InputStream fileData : fileDataList) {
      try {
        parts.add(Part.fromBytes(fileData.readAllBytes(), "application/pdf"));
      } catch (IOException e) {
        throw LlmException.withCause(e);
      }
    }

    return parts;
  }

  private void validateResponse(GenerateContentResponse response) {
    if (response.finishReason().knownEnum() != FinishReason.Known.STOP) {
      throw LlmException.generationFailed(
          "AI 모델이 비정상적으로 응답 생성을 중단했습니다. (사유: " + response.finishReason() + ")");
    }
  }

  private void logRequestDetails(String model, LlmGeneratedQuestionRequest request) {
    log.info(
        """

        --- Gemini API Request Parameters ---
        [Model Name] : {}
        [Question Count] : {}
        [Prompt Length] : {} characters
        [File Count] : {}{}
        --- End of Parameters ---""",
        model,
        request.specification().questionCount(),
        request.prompt() != null ? request.prompt().length() : "null",
        request.fileDataList() != null ? request.fileDataList().size() : "null",
        request.fileDataList() != null && !request.fileDataList().isEmpty()
            ? "\n[File Details] : " + "Streaming input, size and hash not calculated."
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
}
