package kr.it.pullit.support.config;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import kr.it.pullit.support.clock.MutableClock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@TestConfiguration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class TestJpaConfig {

  @Bean
  public DateTimeProvider auditingDateTimeProvider(MutableClock mutableClock) {
    return () ->
        Optional.of(LocalDateTime.ofInstant(mutableClock.instant(), ZoneId.systemDefault()));
  }
}
