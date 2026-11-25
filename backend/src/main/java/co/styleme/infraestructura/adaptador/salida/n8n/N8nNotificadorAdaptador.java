package co.styleme.infraestructura.adaptador.salida.n8n;

import co.styleme.aplicacion.puerto.salida.notificacion.NotificadorPuerto;
import co.styleme.dominio.modelo.comunidad.Resena;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Adaptador de salida: le avisa a n8n (por su webhook) que hay una reseña nueva.
 * n8n la lee, le pregunta a Gemini si es apropiada y la publica o la rechaza
 * (workflow n8n/moderacion-resenas.json).
 *
 * Solo se manda el id: n8n pide los datos al backend con la clave de administración.
 * Si N8N_WEBHOOK_RESENAS está vacío (por ejemplo, sin Docker), no hace nada.
 */
@Component
public class N8nNotificadorAdaptador implements NotificadorPuerto {

    private static final Logger LOG = LoggerFactory.getLogger(N8nNotificadorAdaptador.class);
    private static final Duration TIEMPO_MAXIMO = Duration.ofSeconds(3); // la persona no debe esperar a n8n

    private final String urlWebhook;
    private final RestClient cliente;

    @Autowired
    public N8nNotificadorAdaptador(@Value("${styleme.n8n.webhook-resenas:}") String urlWebhook) {
        this(urlWebhook, RestClient.builder().requestFactory(fabricaConTiempoMaximo()).build());
    }

    /** Para las pruebas: permite usar un servidor simulado. */
    N8nNotificadorAdaptador(String urlWebhook, RestClient cliente) {
        this.urlWebhook = urlWebhook == null ? "" : urlWebhook.trim();
        this.cliente = cliente;
    }

    private static JdkClientHttpRequestFactory fabricaConTiempoMaximo() {
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(TIEMPO_MAXIMO).build());
        fabrica.setReadTimeout(TIEMPO_MAXIMO);
        return fabrica;
    }

    @Override
    public void resenaCreada(Resena resena) {
        if (urlWebhook.isEmpty() || resena.id() == null) {
            return;
        }
        try {
            cliente.post().uri(urlWebhook)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("evento", "resena_creada", "id", resena.id()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException e) {
            // n8n apagado o lento: la moderación programada (cada 30 min) recogerá la reseña después
            LOG.warn("No se pudo avisar a n8n de la reseña {}: {}", resena.id(), e.getMessage());
        }
    }
}
