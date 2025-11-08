package kr.it.pullit.modules.questionset.client.exception;

public enum LlmErrorType {
  PERMANENT, // 예: 페이지 제한 초과
  TEMPORARY // 예: 요청량 제한, 네트워크 문제
}
