package kr.it.pullit.modules.questionset.api;

import kr.it.pullit.modules.questionset.client.dto.LlmGeneratedQuestionDto;

public interface LlmClient {
  LlmGeneratedQuestionDto getLlmGeneratedQuestion(String prompt, String filePath, String model);
}
