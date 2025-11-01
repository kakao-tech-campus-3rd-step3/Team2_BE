package kr.it.pullit.modules.notification.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.notification")
public class NotificationProperties {

  @Min(1)
  private int sseCacheSize = 10000;
}
