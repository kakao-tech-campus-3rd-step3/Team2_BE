package kr.it.pullit.modules.questionset.client.exception;

public class LlmResponseParseException extends RuntimeException {

  public LlmResponseParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
