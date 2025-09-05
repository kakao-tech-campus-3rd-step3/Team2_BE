package kr.it.pullit.modules.auth.kakaoauth.web;

import java.net.URI;
import kr.it.pullit.modules.auth.kakaoauth.service.KakaoAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KakaoAuthController {

  private final KakaoAuthService kakaoAuthService;

  public KakaoAuthController(KakaoAuthService kakaoAuthService) {
    this.kakaoAuthService = kakaoAuthService;
  }

  @GetMapping("/oauth/authorize/kakao")
  public ResponseEntity<Void> authorize(
      @RequestParam(name = "state", required = false) String state) {
    return ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create(kakaoAuthService.buildAuthorizeUrl(state)))
        .build();
  }
}
