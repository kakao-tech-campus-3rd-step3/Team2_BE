package kr.it.pullit.platform.security.jwt;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProps(
    String secret,
    String issuer,
    String audience,
    Duration accessTokenExpirationMinutes,
    Duration refreshTokenExpirationDays,
    List<String> authorizedRedirectUris,
    List<String> authorizedCookieDomains) {}
