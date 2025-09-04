package kr.it.pullit.platform.web;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Aspect
@Component
public class RateLimiterConfig {

  private final RateLimiter rateLimiter;

  public RateLimiterConfig(RateLimiterRegistry rateLimiterRegistry) {
    this.rateLimiter = rateLimiterRegistry.rateLimiter("default");
  }

  @Before("within(@org.springframework.web.bind.annotation.RestController *)")
  public void beforeRestController() {
    if (!rateLimiter.acquirePermission()) {
      throw new ResponseStatusException(
          HttpStatus.TOO_MANY_REQUESTS, "You have exhausted your API request quota.");
    }
  }
}
