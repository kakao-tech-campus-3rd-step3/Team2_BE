package kr.it.pullit.platform.docs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info = @Info(title = "Pullit API", version = "v1", description = "팀 내부 개발용 OpenAPI 문서"),
    servers = {@Server(url = "https://api-qa.pull.it.kr", description = "QA Server")})
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer", // Authorization: Bearer <token>
    bearerFormat = "JWT")
@Configuration
public class OpenApiConfig {}
