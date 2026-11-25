package co.styleme.infraestructura.adaptador.salida.gemini;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de Gemini (sección "styleme.gemini" de application.yml).
 * La clave sale de la variable de entorno GEMINI_API_KEY: nunca se escribe en el código.
 *
 * @param apiKey             clave de Google AI Studio
 * @param urlBase            dirección de la API de Gemini
 * @param modeloEmbeddings   modelo que convierte textos en vectores
 * @param modeloChat         modelo de lenguaje que genera las respuestas de Lía
 * @param tiempoMaximo       cuánto esperar una respuesta antes de rendirse
 */
@ConfigurationProperties(prefix = "styleme.gemini")
public record PropiedadesGemini(String apiKey, String urlBase, String modeloEmbeddings, String modeloChat, Duration tiempoMaximo) {

    public boolean tieneClave() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.startsWith("pega-aqui");
    }
}
