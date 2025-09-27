package kr.it.pullit.modules.auth.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import kr.it.pullit.modules.auth.service.AuthService;
import kr.it.pullit.modules.auth.web.dto.AccessTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
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
  public ResponseEntity<Void> logout(
      @AuthenticationPrincipal Long memberId, HttpServletResponse response) {
    authService.logout(memberId);

    Cookie cookie = new Cookie("refresh_token", null);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    response.addCookie(cookie);

    return ResponseEntity.noContent().build();
  }
}
