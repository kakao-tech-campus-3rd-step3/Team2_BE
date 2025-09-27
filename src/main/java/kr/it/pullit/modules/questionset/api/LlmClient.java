package kr.it.pullit.modules.questionset.api;

import java.util.List;
import kr.it.pullit.modules.questionset.client.dto.request.LlmGeneratedQuestionRequest;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionResponse;
import org.springframework.stereotype.Component;

@Component
public interface LlmClient {
  List<LlmGeneratedQuestionResponse> getLlmGeneratedQuestionContent(
      LlmGeneratedQuestionRequest request);
}
