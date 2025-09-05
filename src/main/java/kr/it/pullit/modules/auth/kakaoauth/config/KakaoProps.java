package kr.it.pullit.modules.auth.kakaoauth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.kakao")
public record KakaoProps(
    String clientId,
    String clientSecret,
    String redirectUri,
    String kauthUrl,
    String authorizePath,
    String tokenPath,
    String userInfoPath) {}
