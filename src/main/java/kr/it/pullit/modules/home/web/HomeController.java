package kr.it.pullit.modules.home.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** 홈 컨트롤러 - 헬스 체크용 */
@RestController
public class HomeController {

  @GetMapping("/")
  public ResponseEntity<String> home() {
    return ResponseEntity.ok("Pullit API Server is running!");
  }

  @GetMapping("/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("OK");
  }
}
