package kr.it.pullit.configuration;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.examples.Example;

@Configuration
public class OpenApiComponentsConfig {

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            if (openApi.getComponents() != null) {
                openApi
                    .getComponents()
                    .addExamples(
                        "argumentTypeMismatchExample",
                        new Example()
                            .summary("인수 타입 불일치")
                            .value(
                                """
                                      {
                                        "type": "about:blank",
                                        "title": "Bad Request",
                                        "status": 400,
                                        "detail": "argument type mismatch",
                                        "instance": "/api/some-endpoint/abc",
                                        "code": "C_001"
                                      }
                                      """));
            }
        };
    }
}
