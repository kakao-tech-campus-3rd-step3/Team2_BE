package kr.it.pullit.modules.questionset.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.ResponseStream;
import com.google.genai.types.Candidate;
import com.google.genai.types.Content;
import com.google.genai.types.FinishReason;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import java.util.List;
import kr.it.pullit.modules.questionset.client.dto.request.LlmGeneratedQuestionRequest;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionSetResponse;
import kr.it.pullit.modules.questionset.client.exception.LlmException;
import kr.it.pullit.modules.questionset.client.exception.LlmResponseParseException;
import kr.it.pullit.modules.questionset.domain.entity.QuestionGenerationSpecification;
import kr.it.pullit.modules.questionset.enums.DifficultyType;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class GeminiClientTest {

  private final QuestionGenerationSpecification specification =
      new QuestionGenerationSpecification(DifficultyType.HARD, QuestionType.TRUE_FALSE, 1);

  private GeminiClient geminiClient;
  private Models models;

  @BeforeEach
  void setUp() {
    GeminiProperties properties = new GeminiProperties();
    properties.setApiKey("dummy-key");

    geminiClient = new GeminiClient(properties, new GeminiConfigBuilder());

    Client client = Mockito.mock(Client.class);
    models = Mockito.mock(Models.class);

    ReflectionTestUtils.setField(geminiClient, "client", client);
    ReflectionTestUtils.setField(client, "models", models);
  }

  @Test
  @DisplayName("Gemini API 응답을 파싱하여 DTO로 반환한다")
  void returnsParsedResponse() {
    String responseJsonChunk1 = "{\"title\":\"AI Quiz\",";
    String responseJsonChunk2 = "\"questions\":[]}";

    List<GenerateContentResponse> responses =
        List.of(successResponse(responseJsonChunk1), successResponse(responseJsonChunk2));

    ResponseStream<GenerateContentResponse> mockResponseStream = Mockito.mock(ResponseStream.class);
    when(mockResponseStream.iterator()).thenReturn(responses.iterator());
    when(mockResponseStream.spliterator()).thenReturn(responses.spliterator());

    when(models.generateContentStream(
            anyString(), any(Content.class), any(GenerateContentConfig.class)))
        .thenReturn(mockResponseStream);

    LlmGeneratedQuestionRequest request =
        new LlmGeneratedQuestionRequest("prompt", List.of(), null, specification);

    LlmGeneratedQuestionSetResponse result = geminiClient.getLlmGeneratedQuestionContent(request);

    assertThat(result.title()).isEqualTo("AI Quiz");
    assertThat(result.questions()).isEmpty();
  }

  @Test
  @DisplayName("FinishReason이 STOP이 아니면 예외를 던진다")
  void throwsWhenFinishReasonIsNotStop() {
    GenerateContentResponse response =
        responseWithFinishReason(new FinishReason(FinishReason.Known.MAX_TOKENS), "{}");

    List<GenerateContentResponse> responses = List.of(response);
    ResponseStream<GenerateContentResponse> mockResponseStream = Mockito.mock(ResponseStream.class);
    when(mockResponseStream.iterator()).thenReturn(responses.iterator());
    when(mockResponseStream.spliterator()).thenReturn(responses.spliterator());

    when(models.generateContentStream(
            anyString(), any(Content.class), any(GenerateContentConfig.class)))
        .thenReturn(mockResponseStream);

    LlmGeneratedQuestionRequest request =
        new LlmGeneratedQuestionRequest("prompt", List.of(), null, specification);

    assertThatThrownBy(() -> geminiClient.getLlmGeneratedQuestionContent(request))
        .isInstanceOf(LlmException.class)
        .hasMessageContaining("AI 모델이 비정상적으로 응답 생성을 중단했습니다.");
  }

  @Test
  @DisplayName("응답 본문이 잘못되면 파싱 예외를 던진다")
  void throwsWhenResponseBodyInvalid() {
    GenerateContentResponse response = successResponse("not-json");

    List<GenerateContentResponse> responses = List.of(response);
    ResponseStream<GenerateContentResponse> mockResponseStream = Mockito.mock(ResponseStream.class);
    when(mockResponseStream.iterator()).thenReturn(responses.iterator());
    when(mockResponseStream.spliterator()).thenReturn(responses.spliterator());

    when(models.generateContentStream(
            anyString(), any(Content.class), any(GenerateContentConfig.class)))
        .thenReturn(mockResponseStream);

    LlmGeneratedQuestionRequest request =
        new LlmGeneratedQuestionRequest("prompt", List.of(), null, specification);

    assertThatThrownBy(() -> geminiClient.getLlmGeneratedQuestionContent(request))
        .isInstanceOf(LlmResponseParseException.class);
  }

  @Test
  @DisplayName("Gemini API 호출 중 예외가 발생하면 LlmException으로 감싼다")
  void wrapsUnexpectedExceptions() {
    when(models.generateContentStream(
            anyString(), any(Content.class), any(GenerateContentConfig.class)))
        .thenThrow(new IllegalStateException("API unavailable"));

    LlmGeneratedQuestionRequest request =
        new LlmGeneratedQuestionRequest("prompt", List.of(), null, specification);

    assertThatThrownBy(() -> geminiClient.getLlmGeneratedQuestionContent(request))
        .isInstanceOf(LlmException.class)
        .hasMessageContaining("API unavailable");
  }

  private GenerateContentResponse successResponse(String responseJson) {
    return responseWithFinishReason(new FinishReason(FinishReason.Known.STOP), responseJson);
  }

  private GenerateContentResponse responseWithFinishReason(
      FinishReason finishReason, String responseJson) {
    Candidate candidate =
        Candidate.builder()
            .finishReason(finishReason)
            .content(Content.fromParts(Part.fromText(responseJson)))
            .build();

    return GenerateContentResponse.builder().candidates(List.of(candidate)).build();
  }
}
