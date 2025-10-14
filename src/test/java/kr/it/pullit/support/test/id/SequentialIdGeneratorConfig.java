package kr.it.pullit.support.test.id;

import java.util.concurrent.atomic.AtomicLong;
import kr.it.pullit.shared.id.IdGenerator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class SequentialIdGeneratorConfig {
  private final AtomicLong sequence = new AtomicLong();

  @Bean
  public IdGenerator idGenerator() {
    return () -> "ID-%03d".formatted(sequence.incrementAndGet());
  }
}
