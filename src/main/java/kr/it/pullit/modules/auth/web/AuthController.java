package kr.it.pullit.modules.auth.web;

import io.sentry.Sentry;
import kr.it.pullit.modules.auth.service.AuthService;
import kr.it.pullit.modules.auth.web.dto.AccessTokenResponse;
import kr.it.pullit.platform.web.interceptor.annotation.ClearCookie;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @PostMapping("/refresh")
  public ResponseEntity<AccessTokenResponse> refresh(
      @CookieValue("refresh_token") String refreshToken) {
    String accessToken = authService.reissueAccessToken(refreshToken);
    return ResponseEntity.ok(new AccessTokenResponse(accessToken));
  }

  @PostMapping("/logout")
  @ClearCookie(name = "refresh_token")
  public ResponseEntity<Void> logout(@AuthenticationPrincipal Long memberId) {
    authService.logout(memberId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/test")
  public ResponseEntity<String> test() {

    try {
      throw new Exception("This is a test.");
    } catch (Exception e) {
      Sentry.captureException(e);
    }

    return ResponseEntity.ok("test");
  }
}
