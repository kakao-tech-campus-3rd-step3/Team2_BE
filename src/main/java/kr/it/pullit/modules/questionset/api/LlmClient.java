package kr.it.pullit.modules.questionset.api;

import java.util.List;
import kr.it.pullit.modules.questionset.client.dto.LlmGeneratedQuestionDto;

public interface LlmClient {
  List<LlmGeneratedQuestionDto> getLlmGeneratedQuestionContent(
      String prompt, byte[] fileData, int questionCount, String model);

  void getLlmGeneratedQuestionStream(
      String prompt, byte[] fileData, int questionCount, String model, SseDataCallback callback);
}
