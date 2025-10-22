package likelion13th.voyageaider.config.swagger;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "voyageaider API",
                version = "v1",
                description = "나만의 여행챗봇 API 문서"
        )
)
public class SwaggerConfig {

    @Bean
    public OpenAPI api() {
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth", bearerAuth));
    }
}
