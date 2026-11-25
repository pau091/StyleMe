package co.styleme.ia;

import static org.assertj.core.api.Assertions.assertThat;

import co.styleme.aplicacion.puerto.entrada.catalogo.ConsultarProductosCasoUso;
import co.styleme.aplicacion.puerto.entrada.ia.ChatearConLiaCasoUso;
import co.styleme.aplicacion.puerto.entrada.ia.IndexarConocimientoCasoUso;
import co.styleme.dominio.modelo.catalogo.Producto;
import co.styleme.dominio.modelo.lia.PreguntaLia;
import co.styleme.dominio.modelo.lia.RespuestaLia;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Lía con RAG y Gemini REAL, de punta a punta. Solo corre si hay clave de Gemini.
 * Comprueba lo que más importa: que NUNCA recomiende algo que no existe o que no cumple lo pedido.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("ia")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIf("co.styleme.ia.BusquedaSemanticaGeminiIntegracionTest#hayClaveGemini")
class LiaRagGeminiIntegracionTest {

    @Autowired
    private ChatearConLiaCasoUso lia;

    @Autowired
    private IndexarConocimientoCasoUso indexar;

    @Autowired
    private ConsultarProductosCasoUso productos;

    @Value("${local.server.port}")
    private int puerto;

    @BeforeAll
    void asegurarIndice() {
        indexar.indexar(); // si el índice ya existe, no gasta tokens (compara hashes)
    }

    private RespuestaLia preguntar(String mensaje, List<String> pedazos) {
        return lia.conversar(new PreguntaLia(mensaje, List.of(), null), pedazos::add);
    }

    @Test
    @DisplayName("Recomienda solo productos reales que cumplen el presupuesto, y habla por partes")
    void recomiendaProductosReales() {
        List<String> pedazos = new ArrayList<>();
        RespuestaLia r = preguntar("busco un sérum para piel grasa, máximo 40 mil", pedazos);

        assertThat(r.modo()).isEqualTo(RespuestaLia.Modo.IA);
        assertThat(r.texto()).isNotBlank();
        assertThat(pedazos).isNotEmpty();
        assertThat(String.join("", pedazos)).doesNotContain("@@@");
        for (String codigo : r.productos()) {
            Producto p = productos.obtener(codigo); // lanza excepción si el producto no existe
            assertThat(p.cuestaMaximo(40_000)).as("%s cuesta %s", codigo, p.precio()).isTrue();
        }
    }

    @Test
    @DisplayName("Ante un tema de salud, muestra preocupación")
    void temaDeSalud() {
        RespuestaLia r = preguntar("me salió una dermatitis y me arde mucho la cara, ¿qué me echo?", new ArrayList<>());
        assertThat(r.emocion()).isEqualTo("preocupacion");
    }

    @Test
    @DisplayName("En una conversación social no ofrece productos")
    void conversacionSocial() {
        RespuestaLia r = preguntar("hola Lía, ¡qué bonito tu suéter!", new ArrayList<>());
        assertThat(r.productos()).isEmpty();
    }

    @Test
    @DisplayName("Por HTTP responde en NDJSON: eventos de texto y un evento final")
    void endpointStreaming() throws Exception {
        var peticion = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/api/lia/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"mensaje\":\"un labial que dure todo el día\",\"historia\":[]}"))
                .build();
        HttpResponse<String> r = HttpClient.newHttpClient().send(peticion, HttpResponse.BodyHandlers.ofString());

        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.headers().firstValue("Content-Type").orElse("")).contains("ndjson");
        assertThat(r.body()).contains("\"tipo\":\"texto\"", "\"tipo\":\"fin\"");
    }
}
