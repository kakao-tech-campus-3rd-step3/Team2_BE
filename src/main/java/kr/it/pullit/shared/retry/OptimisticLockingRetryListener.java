package kr.it.pullit.shared.retry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component("optimisticLockingRetryListener")
public class OptimisticLockingRetryListener implements RetryListener {

  @Override
  public <T, E extends Throwable> void onError(
      RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
    if (throwable instanceof ObjectOptimisticLockingFailureException) {
      log.warn("[재시도 경고] 낙관적 락 충돌이 감지되어 재시도합니다. (시도 횟수: {})", context.getRetryCount(), throwable);
    }
  }
}
