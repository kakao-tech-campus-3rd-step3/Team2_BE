package kr.it.pullit.support.config;

import java.time.Instant;
import java.time.ZoneId;
import kr.it.pullit.support.clock.MutableClock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class MutableClockConfig {

  @Bean
  public MutableClock clock() {
    return new MutableClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));
  }
}
