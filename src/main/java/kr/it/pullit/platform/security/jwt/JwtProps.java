package kr.it.pullit.platform.security.jwt;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProps(
    String secret,
    String issuer,
    String audience,
    Duration accessTokenExpirationMinutes,
    Duration refreshTokenExpirationDays,
    String redirectUrl,
    String cookieDomain) {}
