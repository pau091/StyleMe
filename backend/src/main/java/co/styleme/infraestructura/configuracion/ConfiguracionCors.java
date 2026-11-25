package co.styleme.infraestructura.configuracion;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS: el navegador solo deja que la página (Live Server, puerto 5500) llame al backend
 * (puerto 8090) si el backend lo autoriza explícitamente. Aquí se autorizan esos orígenes.
 * Se cambian sin tocar código con STYLEME_CORS_ORIGENES en el .env.
 *
 * Es un FILTRO que corre antes que todos los demás (incluido FiltroClaveAdmin). Así, cuando la clave
 * de administración es incorrecta, el 401 también lleva las cabeceras CORS y la página puede leer
 * el mensaje "la clave no es válida" (sin ellas, el navegador lo esconde como "error de conexión").
 */
@Configuration
public class ConfiguracionCors {

    @Bean
    FilterRegistrationBean<CorsFilter> filtroCors(@Value("${styleme.cors.origenes}") List<String> origenes) {
        CorsConfiguration reglas = new CorsConfiguration();
        reglas.setAllowedOrigins(origenes);
        reglas.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE"));
        reglas.setAllowedHeaders(List.of("Content-Type", "X-Admin-Key"));
        reglas.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource rutas = new UrlBasedCorsConfigurationSource();
        rutas.registerCorsConfiguration("/api/**", reglas);

        FilterRegistrationBean<CorsFilter> registro = new FilterRegistrationBean<>(new CorsFilter(rutas));
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE); // primero que cualquier otro filtro
        return registro;
    }
}
