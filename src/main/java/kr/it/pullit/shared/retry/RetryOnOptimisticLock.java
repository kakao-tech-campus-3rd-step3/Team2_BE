package kr.it.pullit.shared.retry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Retryable(
    retryFor = ObjectOptimisticLockingFailureException.class,
    listeners = "optimisticLockingRetryListener")
public @interface RetryOnOptimisticLock {

  /** 호출 지점에서 backoff 전략을 바꿔 쓸 수 있도록 노출합니다. 기본은 1초 지연. */
  @AliasFor(annotation = Retryable.class, attribute = "backoff")
  Backoff backoff() default @Backoff(delay = 1000);

  /** 최대 시도 횟수를 노출합니다. (기본값 3 — Retryable 기본과 동일) */
  @AliasFor(annotation = Retryable.class, attribute = "maxAttempts")
  int maxAttempts() default 3;
}
