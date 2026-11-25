package co.styleme.infraestructura.adaptador.salida.gemini;

import co.styleme.aplicacion.puerto.salida.ia.EmbeddingPuerto;
import co.styleme.dominio.excepcion.ServicioIaNoDisponibleException;
import co.styleme.dominio.modelo.ia.Embedding;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Adaptador de salida: implementa EmbeddingPuerto con la API de Gemini (gemini-embedding-001).
 *
 * Detalles importantes:
 *  - taskType RETRIEVAL_DOCUMENT para lo que se guarda y RETRIEVAL_QUERY para lo que se busca:
 *    Gemini optimiza cada vector para su papel, y la búsqueda mejora.
 *  - outputDimensionality = 768 (debe coincidir con la columna VECTOR(768)).
 *  - Con menos de 3072 dimensiones Gemini recomienda normalizar: lo hace Embedding.de(...).
 *  - La clave va en la cabecera x-goog-api-key, nunca en la dirección.
 */
@Component
public class GeminiEmbeddingAdaptador implements EmbeddingPuerto {

    private static final String DOCUMENTO = "RETRIEVAL_DOCUMENT";
    private static final String CONSULTA = "RETRIEVAL_QUERY";

    private final PropiedadesGemini propiedades;
    private final RestClient cliente;

    public GeminiEmbeddingAdaptador(PropiedadesGemini propiedades, @Qualifier("clienteGemini") RestClient cliente) {
        this.propiedades = propiedades;
        this.cliente = cliente;
    }

    @Override
    public List<Embedding> deDocumentos(List<String> textos) {
        if (textos.isEmpty()) {
            return List.of();
        }
        List<Map<String, Object>> peticiones = textos.stream().map(t -> peticion(t, DOCUMENTO)).toList();
        Map<?, ?> respuesta = llamar(":batchEmbedContents", Map.of("requests", peticiones));
        List<?> lista = (List<?>) respuesta.get("embeddings");
        if (lista == null || lista.size() != textos.size()) {
            throw new ServicioIaNoDisponibleException("Gemini devolvió una cantidad inesperada de embeddings");
        }
        return lista.stream().map(e -> aEmbedding((Map<?, ?>) e)).toList();
    }

    @Override
    public Embedding deConsulta(String texto) {
        Map<?, ?> respuesta = llamar(":embedContent", peticion(texto, CONSULTA));
        return aEmbedding((Map<?, ?>) respuesta.get("embedding"));
    }

    @Override
    public String modelo() {
        return propiedades.modeloEmbeddings();
    }

    private Map<String, Object> peticion(String texto, String tipoTarea) {
        return Map.of(
                "model", "models/" + modelo(),
                "content", Map.of("parts", List.of(Map.of("text", texto))),
                "taskType", tipoTarea,
                "outputDimensionality", Embedding.DIMENSIONES);
    }

    private Map<?, ?> llamar(String accion, Object cuerpo) {
        if (!propiedades.tieneClave()) {
            throw new ServicioIaNoDisponibleException("Falta configurar GEMINI_API_KEY en el archivo .env");
        }
        try {
            Map<?, ?> respuesta = cliente.post()
                    .uri("/models/{modelo}" + accion, modelo())
                    .header("x-goog-api-key", propiedades.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .body(Map.class);
            if (respuesta == null) {
                throw new ServicioIaNoDisponibleException("Gemini respondió vacío");
            }
            return respuesta;
        } catch (RestClientException e) {
            // 400 (petición mala), 403 (clave inválida), 429 (sin cupo), 5xx, sin internet...
            throw new ServicioIaNoDisponibleException("No se pudo obtener el embedding de Gemini: " + e.getMessage(), e);
        }
    }

    private static Embedding aEmbedding(Map<?, ?> embedding) {
        if (embedding == null || !(embedding.get("values") instanceof List<?> valores)) {
            throw new ServicioIaNoDisponibleException("La respuesta de Gemini no trae el vector");
        }
        float[] vector = new float[valores.size()];
        for (int i = 0; i < vector.length; i++) {
            vector[i] = ((Number) valores.get(i)).floatValue();
        }
        return Embedding.de(vector);
    }
}
