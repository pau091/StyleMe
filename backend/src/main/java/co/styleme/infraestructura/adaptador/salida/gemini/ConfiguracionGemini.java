package co.styleme.infraestructura.adaptador.salida.gemini;

import java.net.http.HttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configuración del adaptador de Gemini: sus propiedades y el cliente HTTP.
 * Vive junto al adaptador porque solo él la usa (los adaptadores no dependen de otras piezas de infraestructura).
 */
@Configuration
@EnableConfigurationProperties(PropiedadesGemini.class)
public class ConfiguracionGemini {

    /** Cliente HTTP para Gemini, con tiempo máximo de espera (si Gemini no responde, no se queda colgado). */
    @Bean
    RestClient clienteGemini(PropiedadesGemini propiedades) {
        HttpClient http = HttpClient.newBuilder().connectTimeout(propiedades.tiempoMaximo()).build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(http);
        fabrica.setReadTimeout(propiedades.tiempoMaximo());
        return RestClient.builder()
                .baseUrl(propiedades.urlBase())
                .requestFactory(fabrica)
                .build();
    }
}
