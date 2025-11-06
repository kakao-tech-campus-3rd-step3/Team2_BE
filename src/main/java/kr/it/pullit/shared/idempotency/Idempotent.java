package kr.it.pullit.shared.idempotency;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Retention(RUNTIME)
@Target(METHOD)
public @interface Idempotent {
  String keyHeader() default "Idempotency-Key";

  long ttl() default 10; // 기본 10분

  TimeUnit timeUnit() default TimeUnit.MINUTES;
}
