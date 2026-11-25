package co.styleme.infraestructura.configuracion;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Información general que muestra Swagger (springdoc-openapi).
 *
 *   Documentación interactiva: http://localhost:8090/swagger-ui.html
 *   Especificación en JSON:    http://localhost:8090/v3/api-docs
 *
 * Las rutas de administración piden la clave: en Swagger se escribe una vez con el botón "Authorize".
 */
@Configuration
public class ConfiguracionOpenApi {

    /** Nombre del esquema de seguridad; los controladores de administración lo citan con @SecurityRequirement(name = "claveAdmin"). */
    public static final String CLAVE_ADMIN = "claveAdmin";

    @Bean
    OpenAPI documentacionStyleMe() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de StyleMe")
                        .version("0.5.0")
                        .description("""
                                Catálogo de tiendas físicas de moda y belleza de Bucaramanga.
                                Backend en Spring Boot con arquitectura hexagonal: búsqueda semántica con embeddings,
                                Lía (RAG con Gemini), reseñas, favoritos y analítica.
                                Las rutas /api/admin/* necesitan la clave de administración (botón Authorize).""")
                        .license(new License().name("Proyecto académico")))
                .components(new Components().addSecuritySchemes(CLAVE_ADMIN, new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-Admin-Key")
                        .description("Valor de STYLEME_ADMIN_CLAVE en el .env")));
    }
}
