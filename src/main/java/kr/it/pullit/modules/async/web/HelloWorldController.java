package kr.it.pullit.modules.async.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import kr.it.pullit.modules.async.service.HelloWorldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HelloWorldController {

  private final HelloWorldService helloWorldService;

  @GetMapping("/async-hello")
  public ResponseEntity<String> asyncHello() {
    log.info("컨트롤러 요청 수신. 현재 스레드: {}", Thread.currentThread().getName());
    helloWorldService.printHelloWorldAsync();
    log.info("컨트롤러가 즉시 응답을 반환합니다.");
    return ResponseEntity.ok("비동기 작업이 시작되었습니다! (5초 후에 로그를 확인하세요)");
  }
}
