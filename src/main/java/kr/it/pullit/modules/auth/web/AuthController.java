package kr.it.pullit.modules.auth.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.it.pullit.modules.auth.service.AuthService;
import kr.it.pullit.modules.auth.web.dto.AccessTokenResponse;
import kr.it.pullit.platform.web.cookie.CookieManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final CookieManager cookieManager;

  @PostMapping("/refresh")
  public ResponseEntity<AccessTokenResponse> refresh(
      @CookieValue("refresh_token") String refreshToken) {
    String accessToken = authService.reissueAccessToken(refreshToken);
    return ResponseEntity.ok(new AccessTokenResponse(accessToken));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      HttpServletRequest request,
      HttpServletResponse response,
      @AuthenticationPrincipal Long memberId) {
    authService.logout(memberId);
    cookieManager.removeRefreshTokenCookie(request, response);
    return ResponseEntity.noContent().build();
  }
}
