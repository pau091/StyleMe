package co.styleme.infraestructura.adaptador.salida.imagen;

import co.styleme.aplicacion.puerto.salida.ia.GeneradorImagenPuerto;
import co.styleme.dominio.excepcion.ServicioIaNoDisponibleException;
import co.styleme.dominio.modelo.probador.Imagen;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

/**
 * Adaptador de salida: genera la imagen del probador con la API de imágenes de OpenAI (POST /v1/images/edits).
 * Se activa con STYLEME_PROBADOR_PROVEEDOR=openai y OPENAI_API_KEY en el .env.
 * La API de OpenAI se paga aparte de ChatGPT (no tiene plan gratuito para imágenes).
 */
@Component
@ConditionalOnProperty(name = "styleme.probador.proveedor", havingValue = "openai")
public class OpenAiImagenAdaptador implements GeneradorImagenPuerto {

    private static final Logger LOG = LoggerFactory.getLogger(OpenAiImagenAdaptador.class);
    static final String URL_BASE = "https://api.openai.com/v1";

    private final String clave;
    private final String modelo;
    private final String calidad;
    private final RestClient cliente;

    @Autowired
    public OpenAiImagenAdaptador(@Value("${styleme.probador.openai.api-key:}") String clave,
            @Value("${styleme.probador.openai.modelo:gpt-image-1}") String modelo,
            @Value("${styleme.probador.openai.calidad:medium}") String calidad,
            @Value("${styleme.probador.tiempo-maximo:120s}") Duration tiempoMaximo) {
        this(clave, modelo, calidad, RestClient.builder().baseUrl(URL_BASE).requestFactory(fabrica(tiempoMaximo)).build());
    }

    /** Para las pruebas: permite usar un servidor simulado. */
    OpenAiImagenAdaptador(String clave, String modelo, String calidad, RestClient cliente) {
        this.clave = clave == null ? "" : clave.trim();
        this.modelo = modelo;
        this.calidad = calidad;
        this.cliente = cliente;
    }

    static JdkClientHttpRequestFactory fabrica(Duration tiempoMaximo) {
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
        fabrica.setReadTimeout(tiempoMaximo);
        return fabrica;
    }

    @Override
    public boolean disponible() {
        return !clave.isEmpty();
    }

    @Override
    public String proveedor() {
        return "OpenAI";
    }

    @Override
    public Imagen generar(String instrucciones, List<Imagen> referencias) {
        if (!disponible()) {
            throw new ServicioIaNoDisponibleException("Falta OPENAI_API_KEY");
        }
        MultiValueMap<String, Object> partes = new LinkedMultiValueMap<>();
        partes.add("model", modelo);
        partes.add("prompt", instrucciones);
        partes.add("size", "1024x1536"); // vertical: cuerpo completo
        partes.add("quality", calidad);
        partes.add("n", "1");
        for (int i = 0; i < referencias.size(); i++) {
            Imagen imagen = referencias.get(i);
            String nombre = "referencia-" + (i + 1) + "." + imagen.extension();
            partes.add("image[]", new ByteArrayResource(imagen.datos()) {
                @Override
                public String getFilename() {
                    return nombre;
                }
            });
        }
        try {
            JsonNode respuesta = cliente.post().uri("/images/edits")
                    .header("Authorization", "Bearer " + clave)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(partes)
                    .retrieve()
                    .body(JsonNode.class);
            String base64 = respuesta == null ? "" : respuesta.path("data").path(0).path("b64_json").asString("");
            if (base64.isEmpty()) {
                throw new ServicioIaNoDisponibleException("OpenAI no devolvió una imagen");
            }
            return new Imagen(Base64.getDecoder().decode(base64), "image/png");
        } catch (RestClientException e) {
            // 401 (clave mala), 429 (sin saldo o cupo), 5xx, tiempo agotado... la página usa el probador local
            LOG.warn("OpenAI no generó la imagen del probador: {}", e.getMessage());
            throw new ServicioIaNoDisponibleException("El probador con IA no está disponible en este momento", e);
        }
    }
}
