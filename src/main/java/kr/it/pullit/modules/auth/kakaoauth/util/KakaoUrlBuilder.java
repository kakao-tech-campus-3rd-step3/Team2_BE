package kr.it.pullit.modules.auth.kakaoauth.util;

import kr.it.pullit.boot.properties.AppProps;
import kr.it.pullit.modules.auth.kakaoauth.config.KakaoProps;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class KakaoUrlBuilder {

  private final KakaoProps kakaoProps;
  private final AppProps appProps;

  public String buildAuthorizeUrl(String state) {
    String clientId = kakaoProps.clientId();
    String redirectUri = kakaoProps.redirectUri();
    String scheme = appProps.scheme();
    String baseUri = appProps.baseUri();

    if (clientId == null || clientId.isBlank()) {
      throw new IllegalStateException("kakao.clientId 설정이 필요합니다.");
    }
    if (redirectUri == null || redirectUri.isBlank()) {
      throw new IllegalStateException("kakao.redirectUri 설정이 필요합니다.");
    }

    UriComponentsBuilder builder =
        UriComponentsBuilder.fromUriString(kakaoProps.kauthUrl() + kakaoProps.authorizePath())
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", scheme + "://" + baseUri + redirectUri)
            .queryParam("response_type", "code");

    if (state != null && !state.isBlank()) {
      builder.queryParam("state", state);
    }

    return builder.toUriString();
  }
}
