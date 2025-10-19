package kr.it.pullit.modules.questionset.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.FinishReason;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import java.io.IOException;
import java.time.Duration;
import kr.it.pullit.modules.questionset.api.LlmClient;
import kr.it.pullit.modules.questionset.client.dto.request.GeminiRequest;
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

  @Override
  public LlmGeneratedQuestionSetResponse getLlmGeneratedQuestionContent(
      LlmGeneratedQuestionRequest request) {
    try {
      GeminiRequest geminiRequest = GeminiRequest.from(request, configBuilder);
      logRequestDetails(geminiRequest, request);

      GenerateContentResponse response = callGeminiApi(geminiRequest);
      validateResponse(response);

      return parseResponse(response);
    } catch (IOException e) {
      throw LlmResponseParseException.create(e);
    } catch (Exception e) {
      throw LlmException.withCause(e);
    }
  }

  private GenerateContentResponse callGeminiApi(GeminiRequest geminiRequest) {
    return client.models.generateContent(
        geminiRequest.model(), geminiRequest.content(), geminiRequest.config());
  }

  private void validateResponse(GenerateContentResponse response) {
    if (response.finishReason().knownEnum() != FinishReason.Known.STOP) {
      throw LlmException.generationFailed(
          "AI 모델이 비정상적으로 응답 생성을 중단했습니다. (사유: " + response.finishReason() + ")");
    }
  }

  private void logRequestDetails(GeminiRequest geminiRequest, LlmGeneratedQuestionRequest request) {
    log.info(
        """

        --- Gemini API 요청 정보 ---
        [모델명] : {}
        [요청 질문 수] : {}
        [프롬프트 길이] : {} 자
        [파일 개수] : {}{}
        --- 요청 정보 끝 ---""",
        geminiRequest.model(),
        request.specification().questionCount(),
        request.prompt() != null ? request.prompt().length() : "null",
        request.fileDataList() != null ? request.fileDataList().size() : "null",
        request.fileDataList() != null && !request.fileDataList().isEmpty()
            ? "\n[파일 상세] : " + "스트리밍 입력으로 파일 크기 및 해시는 계산되지 않음."
            : "");
  }

  private LlmGeneratedQuestionSetResponse parseResponse(GenerateContentResponse response)
      throws IOException {
    return mapper.readValue(response.text(), LlmGeneratedQuestionSetResponse.class);
  }
}
