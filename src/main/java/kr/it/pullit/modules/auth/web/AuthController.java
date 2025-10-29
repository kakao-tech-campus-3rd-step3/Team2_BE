package kr.it.pullit.modules.auth.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.it.pullit.modules.auth.service.AuthService;
import kr.it.pullit.modules.auth.web.apidocs.AuthApiDocs;
import kr.it.pullit.modules.auth.web.apidocs.LogoutApiDocs;
import kr.it.pullit.modules.auth.web.apidocs.ReissueTokenApiDocs;
import kr.it.pullit.modules.auth.web.dto.AccessTokenResponse;
import kr.it.pullit.platform.aop.annotation.ClearCookie;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth API", description = "인증 및 토큰 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@AuthApiDocs
public class AuthController {
  private final AuthService authService;

  @ReissueTokenApiDocs
  @PostMapping("/refresh")
  public ResponseEntity<AccessTokenResponse> refresh(
      @CookieValue("refresh_token") String refreshToken) {
    String accessToken = authService.reissueAccessToken(refreshToken);
    return ResponseEntity.ok(AccessTokenResponse.of(accessToken));
  }

  @LogoutApiDocs
  @PostMapping("/logout")
  @ClearCookie(name = "refresh_token")
  public ResponseEntity<Void> logout(@AuthenticationPrincipal Long memberId) {
    authService.logout(memberId);
    return ResponseEntity.noContent().build();
  }
}
