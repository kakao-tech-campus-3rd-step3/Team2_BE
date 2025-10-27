package kr.it.pullit.platform.docs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info =
        @Info(
            title = "Pullit API 명세서",
            version = "v1.0",
            description = "Pullit 프로젝트의 공식 API 문서입니다."))
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer", // Authorization:
    // Bearer
    // <token>
    bearerFormat = "JWT")
@Configuration
public class OpenApiConfig {}
