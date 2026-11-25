package co.styleme.infraestructura.configuracion;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Información general que muestra Swagger (springdoc-openapi).
 *
 *   Documentación interactiva: http://localhost:8090/swagger-ui.html
 *   Especificación en JSON:    http://localhost:8090/v3/api-docs
 */
@Configuration
public class ConfiguracionOpenApi {

    @Bean
    OpenAPI documentacionStyleMe() {
        return new OpenAPI().info(new Info()
                .title("API de StyleMe")
                .version("0.3.0")
                .description("""
                        Catálogo de tiendas físicas de moda y belleza de Bucaramanga.
                        Backend en Spring Boot con arquitectura hexagonal.
                        Próximamente: búsqueda semántica con embeddings y Lía (RAG).""")
                .license(new License().name("Proyecto académico")));
    }
}
