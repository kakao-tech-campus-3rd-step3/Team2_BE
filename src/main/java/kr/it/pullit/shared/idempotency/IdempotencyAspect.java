package kr.it.pullit.shared.idempotency;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotencyAspect {

  private final StringRedisTemplate stringRedisTemplate;
  private static final String REDIS_KEY_PREFIX = "idempotency:";

  @Around("@annotation(idempotent)")
  public Object check(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
    String idemKey = extractIdempotencyKey(idempotent, joinPoint);
    if (idemKey == null || idemKey.isBlank()) {
      return joinPoint.proceed();
    }

    String redisKey = buildRedisKey(idemKey);
    acquireLockOrThrow(redisKey, idempotent);

    try {
      Object result = joinPoint.proceed();
      Duration ttl = toDuration(idempotent.ttl(), idempotent.timeUnit());
      stringRedisTemplate.opsForValue().set(redisKey, "DONE", ttl);
      return result;
    } catch (Throwable t) {
      stringRedisTemplate.delete(redisKey);
      throw t;
    }
  }

  private String extractIdempotencyKey(Idempotent idempotent, ProceedingJoinPoint joinPoint) {
    try {
      RequestAttributes attrs = RequestContextHolder.currentRequestAttributes();
      if (!(attrs instanceof ServletRequestAttributes servletAttrs)) {
        log.debug("No servlet request attributes; skipping idempotency.");
        return null;
      }
      HttpServletRequest req = servletAttrs.getRequest();

      String headerKey = Optional.ofNullable(req.getHeader(idempotent.keyHeader())).orElse("");
      if (headerKey.isBlank()) {
        log.warn("메서드 '{}'에 멱등성 키가 없습니다.", joinPoint.getSignature().getName());
        return null;
      }

      String user =
          Optional.ofNullable(req.getUserPrincipal()).map(p -> p.getName()).orElse("anon");
      String scope = req.getMethod() + "|" + req.getRequestURI() + "|" + user + "|" + headerKey;
      return scope;

    } catch (IllegalStateException e) {
      log.debug("RequestContextHolder not available; skipping idempotency.");
      return null;
    }
  }

  private String buildRedisKey(String scopeString) {
    String hash = DigestUtils.md5DigestAsHex(scopeString.getBytes(StandardCharsets.UTF_8));
    return REDIS_KEY_PREFIX + hash;
  }

  private void acquireLockOrThrow(String redisKey, Idempotent idempotent) {
    Duration ttl = toDuration(idempotent.ttl(), idempotent.timeUnit());

    String existing = stringRedisTemplate.opsForValue().get(redisKey);
    if (existing != null) {
      log.info("중복 요청 감지: {}", redisKey);
      throw new DuplicateRequestException();
    }

    Boolean first = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, "LOCK", ttl);
    if (Boolean.FALSE.equals(first)) {
      log.info("경합 중 중복 요청 감지: {}", redisKey);
      throw new DuplicateRequestException();
    }
  }

  private Duration toDuration(long ttl, TimeUnit unit) {
    Objects.requireNonNull(unit, "timeUnit");
    return Duration.ofMillis(unit.toMillis(ttl));
  }
}
