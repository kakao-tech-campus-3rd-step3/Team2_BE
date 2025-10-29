package kr.it.pullit.modules.auth.web.dto;

public record AccessTokenResponse(String accessToken) {
  public static AccessTokenResponse of(String accessToken) {
    return new AccessTokenResponse(accessToken);
  }
}
