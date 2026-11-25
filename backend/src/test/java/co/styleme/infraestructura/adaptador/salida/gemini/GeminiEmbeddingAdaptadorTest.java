package co.styleme.infraestructura.adaptador.salida.gemini;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import co.styleme.dominio.excepcion.ServicioIaNoDisponibleException;
import co.styleme.dominio.modelo.ia.Embedding;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/**
 * Prueba del adaptador de Gemini SIN llamar a Gemini: MockRestServiceServer finge ser la API.
 * Comprueba que la petición tenga la forma correcta y que la respuesta se convierta bien.
 */
class GeminiEmbeddingAdaptadorTest {

    private static final String BASE = "https://gemini.prueba/v1beta";

    private MockRestServiceServer servidor;
    private GeminiEmbeddingAdaptador adaptador;

    /** JSON con un vector de 768 números: el primero vale "valor" y el resto 0. */
    private static String vector(double valor) {
        List<String> numeros = new java.util.ArrayList<>(Collections.nCopies(Embedding.DIMENSIONES, "0"));
        numeros.set(0, String.valueOf(valor));
        return "{\"values\":[" + String.join(",", numeros) + "]}";
    }

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        servidor = MockRestServiceServer.bindTo(builder).build();
        var propiedades = new PropiedadesGemini("clave-de-prueba", BASE, "gemini-embedding-001", Duration.ofSeconds(5));
        adaptador = new GeminiEmbeddingAdaptador(propiedades, builder.build());
    }

    @Test
    @DisplayName("Pide los documentos en lote con RETRIEVAL_DOCUMENT, 768 dimensiones y la clave en la cabecera")
    void documentosEnLote() {
        servidor.expect(requestTo(BASE + "/models/gemini-embedding-001:batchEmbedContents"))
                .andExpect(header("x-goog-api-key", "clave-de-prueba"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"taskType\":\"RETRIEVAL_DOCUMENT\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"outputDimensionality\":768")))
                .andRespond(withSuccess("{\"embeddings\":[" + vector(3) + "," + vector(4) + "]}", MediaType.APPLICATION_JSON));

        List<Embedding> resultado = adaptador.deDocumentos(List.of("texto uno", "texto dos"));

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).valores()[0]).isCloseTo(1f, within(1e-6f)); // normalizado
        servidor.verify();
    }

    @Test
    @DisplayName("Una búsqueda usa RETRIEVAL_QUERY")
    void consulta() {
        servidor.expect(requestTo(BASE + "/models/gemini-embedding-001:embedContent"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"taskType\":\"RETRIEVAL_QUERY\"")))
                .andRespond(withSuccess("{\"embedding\":" + vector(2) + "}", MediaType.APPLICATION_JSON));

        assertThat(adaptador.deConsulta("crema para piel grasa").valores()).hasSize(Embedding.DIMENSIONES);
        servidor.verify();
    }

    @Test
    @DisplayName("Si Gemini responde 429 (sin cupo), se lanza ServicioIaNoDisponibleException")
    void sinCupo() {
        servidor.expect(requestTo(BASE + "/models/gemini-embedding-001:embedContent"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> adaptador.deConsulta("hola")).isInstanceOf(ServicioIaNoDisponibleException.class);
    }

    @Test
    @DisplayName("Sin clave configurada no llama a Gemini y avisa con un mensaje claro")
    void sinClave() {
        var sinClave = new GeminiEmbeddingAdaptador(
                new PropiedadesGemini("", BASE, "gemini-embedding-001", Duration.ofSeconds(5)), RestClient.create());
        assertThatThrownBy(() -> sinClave.deConsulta("hola"))
                .isInstanceOf(ServicioIaNoDisponibleException.class)
                .hasMessageContaining("GEMINI_API_KEY");
    }
}
