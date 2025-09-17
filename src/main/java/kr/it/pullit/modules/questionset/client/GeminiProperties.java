package kr.it.pullit.modules.questionset.client;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "app.gemini")
@Validated
@Getter
@Setter
public class GeminiProperties {

  @NotBlank private String apiKey;
}
