package kr.it.pullit.modules.questionset.client.dto.request;

import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import kr.it.pullit.modules.questionset.client.GeminiConfigBuilder;
import kr.it.pullit.modules.questionset.client.exception.LlmException;
import kr.it.pullit.modules.questionset.enums.QuestionType;

public record GeminiRequest(String model, Content content, GenerateContentConfig config) {

  private static final String DEFAULT_MODEL = "gemini-2.5-flash-lite";

  public static GeminiRequest from(
      LlmGeneratedQuestionRequest request, GeminiConfigBuilder configBuilder) {
    validateRequest(request);

    String model = determineModel(request.model());
    Content content = buildContent(request);
    GenerateContentConfig config =
        buildConfig(
            configBuilder,
            request.specification().questionCount(),
            request.specification().questionType());

    return new GeminiRequest(model, content, config);
  }

  private static void validateRequest(LlmGeneratedQuestionRequest request) {
    Objects.requireNonNull(request, "LLM 요청 객체는 null일 수 없습니다.");
  }

  private static String determineModel(String model) {
    return (model != null) ? model : DEFAULT_MODEL;
  }

  private static Content buildContent(LlmGeneratedQuestionRequest request) {
    List<Part> parts = convertInputStreamsToParts(request.fileDataList());
    parts.add(Part.fromText(request.prompt()));
    return Content.fromParts(parts.toArray(new Part[0]));
  }

  private static List<Part> convertInputStreamsToParts(List<InputStream> fileDataList) {
    List<Part> parts = new ArrayList<>();
    // TODO: 여러 파일 및 다양한 MIME 타입 지원
    for (InputStream fileData : fileDataList) {
      try {
        parts.add(Part.fromBytes(fileData.readAllBytes(), "application/pdf"));
      } catch (IOException e) {
        throw LlmException.withCause(e);
      }
    }
    return parts;
  }

  private static GenerateContentConfig buildConfig(
      GeminiConfigBuilder configBuilder, Integer questionCount, QuestionType questionType) {
    return configBuilder.build(questionCount, questionType);
  }
}
