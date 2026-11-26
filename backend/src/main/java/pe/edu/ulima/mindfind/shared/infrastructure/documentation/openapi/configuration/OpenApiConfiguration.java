package pe.edu.ulima.mindfind.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger UI / OpenAPI document: the "Published Language" of the Open Host Service. */
@Configuration
public class OpenApiConfiguration {

    @Value("${documentation.application.title:MindFind API}")
    private String applicationTitle;

    @Value("${documentation.application.description}")
    private String applicationDescription;

    @Value("${documentation.application.version}")
    private String applicationVersion;

    @Bean
    public OpenAPI mindFindOpenApi() {
        return new OpenAPI().info(new Info()
                .title(applicationTitle)
                .description(applicationDescription)
                .version(applicationVersion));
    }
}
