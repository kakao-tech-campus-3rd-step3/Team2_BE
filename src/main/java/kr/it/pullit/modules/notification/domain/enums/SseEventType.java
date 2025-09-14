package kr.it.pullit.modules.notification.domain.enums;

public enum SseEventType {
  HAND_SHAKE_COMPLETE("handShakeComplete"),
  QUESTION_SET_CREATION_COMPLETE("questionSetCreationComplete");

  private final String code;

  SseEventType(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }
}
