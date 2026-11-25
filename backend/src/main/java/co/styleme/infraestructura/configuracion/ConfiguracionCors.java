package co.styleme.infraestructura.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS: el navegador solo deja que la página (Live Server, puerto 5500) llame al backend
 * (puerto 8090) si el backend lo autoriza explícitamente. Aquí se autorizan esos orígenes.
 * Se cambian sin tocar código con STYLEME_CORS_ORIGENES en el .env.
 */
@Configuration
public class ConfiguracionCors implements WebMvcConfigurer {

    private final String[] origenes;

    public ConfiguracionCors(@Value("${styleme.cors.origenes}") String[] origenes) {
        this.origenes = origenes;
    }

    @Override
    public void addCorsMappings(CorsRegistry registro) {
        registro.addMapping("/api/**")
                .allowedOrigins(origenes)
                .allowedMethods("GET", "POST", "PATCH", "DELETE")
                .allowedHeaders("Content-Type", "X-Admin-Key")
                .maxAge(3600);
    }
}
