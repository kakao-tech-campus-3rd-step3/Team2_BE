package kr.it.pullit.modules.auth.kakaoauth.service;

import kr.it.pullit.modules.auth.kakaoauth.util.KakaoUrlBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

  private final KakaoUrlBuilder kakaoUrlBuilder;

  public String buildAuthorizeUrl(String state) {
    return kakaoUrlBuilder.buildAuthorizeUrl(state);
  }
}
