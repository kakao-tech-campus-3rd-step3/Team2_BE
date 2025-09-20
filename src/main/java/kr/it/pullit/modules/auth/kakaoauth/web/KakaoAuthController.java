package kr.it.pullit.modules.auth.kakaoauth.web;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class KakaoAuthController {

  @GetMapping("/oauth/authorize/kakao")
  public ResponseEntity<Void> authorize(
      @RequestParam(name = "state", required = false) String state) {
    String authorizeUrl =
        UriComponentsBuilder.fromPath("/oauth2/authorization/kakao")
            .queryParam("state", state)
            .toUriString();

    return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(authorizeUrl)).build();
  }
}
