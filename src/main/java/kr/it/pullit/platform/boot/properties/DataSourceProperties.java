package kr.it.pullit.platform.boot.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.datasource.hikari")
public class DataSourceProperties {

  private int maximumPoolSize;
}
