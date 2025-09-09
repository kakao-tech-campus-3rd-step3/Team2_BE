package kr.it.pullit.modules.async.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class HelloWorldService {

  @Async("ioBoundTaskExecutor")
  public void printHelloWorldAsync() {
    log.info("비동기 작업 시작. 현재 스레드: {}", Thread.currentThread().getName());
    try {
      // 5초간의 시간이 걸리는 작업을 시뮬레이션합니다.
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("스레드 대기 중 오류 발생", e);
    }
    log.info("Hello World! - 5초 작업 완료. 실행 스레드: {}", Thread.currentThread().getName());
  }
}
