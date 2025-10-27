package kr.it.pullit.modules.home.web;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Home API", description = "홈 화면 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HomeController {

  @GetMapping("/")
  public ResponseEntity<String> home() {
    return ResponseEntity.ok("Pullit API Server is running!");
  }

  @GetMapping("/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("OK");
  }

  @PostMapping("/echo")
  public ResponseEntity<Map<String, Object>> echo(@RequestBody Map<String, Object> body) {
    return ResponseEntity.ok(body);
  }

  @PostMapping("/echo2")
  public ResponseEntity<Map<String, Object>> echo2(@RequestBody Map<String, Object> body) {
    return ResponseEntity.ok(Map.of("received", body, "size", body.size()));
  }
}
