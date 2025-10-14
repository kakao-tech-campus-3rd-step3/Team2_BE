package kr.it.pullit.support;

import java.util.TimeZone;
import kr.it.pullit.modules.notification.repository.SseEventCache;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class IntegrationTestExtension implements BeforeAllCallback, AfterEachCallback {

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    SseEventCache sseEventCache =
        SpringExtension.getApplicationContext(context).getBean(SseEventCache.class);
    sseEventCache.clear();

    SecurityContextHolder.clearContext();
  }
}
