package kr.it.pullit.modules.questionset.api;

import kr.it.pullit.modules.questionset.client.dto.request.LlmGeneratedQuestionRequest;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionSetResponse;

/** AI LLM 모델 클라이언트 인터페이스 */
public interface LlmClient {

  /**
   * AI LLM 모델로부터 문제 생성
   *
   * @param request 문제 생성 요청
   * @return 생성된 문제 목록
   */
  LlmGeneratedQuestionSetResponse getLlmGeneratedQuestionContent(
      LlmGeneratedQuestionRequest request);
}
