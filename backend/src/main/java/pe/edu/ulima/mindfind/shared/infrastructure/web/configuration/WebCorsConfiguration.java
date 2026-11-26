package pe.edu.ulima.mindfind.shared.infrastructure.web.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Global CORS configuration allowing local frontend development (Vite)
 * and production origins configured via {@code CORS_ALLOWED_ORIGINS}.
 */
@Configuration
public class WebCorsConfiguration implements WebMvcConfigurer {

    private static final String DEFAULT_LOCAL_ORIGIN = "http://localhost:5173";

    private final String[] allowedOriginPatterns;

    public WebCorsConfiguration(@Value("${cors.allowed-origins:}") String rawAllowedOrigins) {
        Set<String> origins = new LinkedHashSet<>();
        origins.add(DEFAULT_LOCAL_ORIGIN);
        if (rawAllowedOrigins != null && !rawAllowedOrigins.isBlank()) {
            Arrays.stream(rawAllowedOrigins.split(","))
                    .map(String::trim)
                    .filter(origin -> !origin.isEmpty())
                    .forEach(origins::add);
        }
        this.allowedOriginPatterns = origins.toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOriginPatterns)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
