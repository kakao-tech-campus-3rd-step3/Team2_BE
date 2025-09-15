package kr.it.pullit.platform.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProps(
    String secret,
    String issuer,
    String audience,
    long accessTokenExpirationMinutes,
    long refreshTokenExpirationDays,
    String redirectUrl) {}
