package co.styleme.infraestructura.adaptador.salida.imagen;

import co.styleme.aplicacion.puerto.salida.ia.GeneradorImagenPuerto;
import co.styleme.dominio.excepcion.ServicioIaNoDisponibleException;
import co.styleme.dominio.modelo.probador.Imagen;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

/**
 * Adaptador de salida: genera la imagen del probador con un modelo de imágenes de Gemini.
 * Se activa con STYLEME_PROBADOR_PROVEEDOR=gemini. Usa la misma GEMINI_API_KEY de Lía, pero el plan
 * gratuito de Gemini NO incluye imágenes (responde 429 "limit: 0"): hace falta activar facturación.
 */
@Component
@ConditionalOnProperty(name = "styleme.probador.proveedor", havingValue = "gemini")
public class GeminiImagenAdaptador implements GeneradorImagenPuerto {

    private static final Logger LOG = LoggerFactory.getLogger(GeminiImagenAdaptador.class);
    static final String URL_BASE = "https://generativelanguage.googleapis.com/v1beta";

    private final String clave;
    private final String modelo;
    private final RestClient cliente;

    @Autowired
    public GeminiImagenAdaptador(@Value("${styleme.gemini.api-key:}") String clave,
            @Value("${styleme.probador.gemini.modelo:gemini-2.5-flash-image}") String modelo,
            @Value("${styleme.probador.tiempo-maximo:120s}") Duration tiempoMaximo) {
        this(clave, modelo, RestClient.builder().baseUrl(URL_BASE).requestFactory(OpenAiImagenAdaptador.fabrica(tiempoMaximo)).build());
    }

    /** Para las pruebas: permite usar un servidor simulado. */
    GeminiImagenAdaptador(String clave, String modelo, RestClient cliente) {
        this.clave = clave == null ? "" : clave.trim();
        this.modelo = modelo;
        this.cliente = cliente;
    }

    @Override
    public boolean disponible() {
        return !clave.isEmpty();
    }

    @Override
    public String proveedor() {
        return "Google Gemini";
    }

    @Override
    public Imagen generar(String instrucciones, List<Imagen> referencias) {
        if (!disponible()) {
            throw new ServicioIaNoDisponibleException("Falta GEMINI_API_KEY");
        }
        List<Object> partes = new ArrayList<>();
        partes.add(Map.of("text", instrucciones));
        referencias.forEach(imagen -> partes.add(Map.of("inlineData", Map.of(
                "mimeType", imagen.tipoMime(), "data", Base64.getEncoder().encodeToString(imagen.datos())))));
        Map<String, Object> cuerpo = Map.of(
                "contents", List.of(Map.of("parts", partes)),
                "generationConfig", Map.of("responseModalities", List.of("IMAGE"),
                        "imageConfig", Map.of("aspectRatio", "3:4")));
        try {
            JsonNode respuesta = cliente.post().uri("/models/{modelo}:generateContent", modelo)
                    .header("x-goog-api-key", clave)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .body(JsonNode.class);
            if (respuesta != null) {
                for (JsonNode parte : respuesta.path("candidates").path(0).path("content").path("parts")) {
                    JsonNode datos = parte.path("inlineData");
                    if (datos.has("data")) {
                        return new Imagen(Base64.getDecoder().decode(datos.path("data").asString()),
                                datos.path("mimeType").asString("image/png"));
                    }
                }
            }
            // Sin imagen: por ejemplo, el filtro de seguridad de Gemini la bloqueó
            throw new ServicioIaNoDisponibleException("Gemini no devolvió una imagen");
        } catch (RestClientException e) {
            LOG.warn("Gemini no generó la imagen del probador: {}", e.getMessage());
            throw new ServicioIaNoDisponibleException("El probador con IA no está disponible en este momento", e);
        }
    }
}
