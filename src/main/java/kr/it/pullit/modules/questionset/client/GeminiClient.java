package kr.it.pullit.modules.questionset.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.FinishReason.Known;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import java.io.IOException;
import java.time.Duration;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
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

      ResponseStream<GenerateContentResponse> responseStream = callGeminiApiStream(geminiRequest);
      String aggregatedResponse = aggregateStreamResponse(responseStream);

      return parseResponse(aggregatedResponse);
    } catch (IOException e) {
      throw LlmResponseParseException.create(e);
    } catch (Exception e) {
      String message = e.getMessage();
      if (message != null && message.contains("exceeds the supported page limit")) {
        throw LlmException.permanent(e);
      }
      throw LlmException.ofTemporary(e);
    }
  }

  private ResponseStream<GenerateContentResponse> callGeminiApiStream(GeminiRequest geminiRequest) {
    return client.models.generateContentStream(
        geminiRequest.model(), geminiRequest.content(), geminiRequest.config());
  }

  private String aggregateStreamResponse(ResponseStream<GenerateContentResponse> responseStream) {
    return StreamSupport.stream(responseStream.spliterator(), false)
        .peek(
            response -> {
              var finishReason = response.finishReason();
              if (finishReason == null) {
                return;
              }
              var knownReason = finishReason.knownEnum();
              if (knownReason == Known.STOP || knownReason == Known.FINISH_REASON_UNSPECIFIED) {
                return;
              }

              String reasonText =
                  "AI 모델이 비정상적으로 응답 생성을 중단했습니다. (사유: " + response.finishReason() + ")";

              if (knownReason == Known.MAX_TOKENS
                  || knownReason == Known.SAFETY
                  || knownReason == Known.RECITATION) {
                throw LlmException.permanent(reasonText);
              }

              throw LlmException.ofTemporary(reasonText);
            })
        .map(GenerateContentResponse::text)
        .filter(text -> text != null && !text.isEmpty())
        .collect(Collectors.joining());
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

  private LlmGeneratedQuestionSetResponse parseResponse(String rawResponse) throws IOException {
    try {
      return mapper.readValue(rawResponse, LlmGeneratedQuestionSetResponse.class);
    } catch (IOException e) {
      log.error(
          "Gemini API 응답 파싱에 실패했습니다. 원본 응답을 에러 로그에 첨부합니다.\n--- 원본 응답 ---\n{}\n--- 원본 응답 끝 ---",
          rawResponse,
          e);
      throw e;
    }
  }
}
