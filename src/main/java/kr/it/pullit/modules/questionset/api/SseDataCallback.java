package kr.it.pullit.modules.questionset.api;

@FunctionalInterface
public interface SseDataCallback {

  void onData(String jsonStr);

  default void onComplete() {}

  default void onError(Throwable t) {}
}
