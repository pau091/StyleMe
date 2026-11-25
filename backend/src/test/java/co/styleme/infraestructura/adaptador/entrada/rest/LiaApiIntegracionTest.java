package co.styleme.infraestructura.adaptador.entrada.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/** El endpoint de Lía por HTTP real (estas pruebas no llaman a Gemini). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LiaApiIntegracionTest {

    private final HttpClient cliente = HttpClient.newHttpClient();

    @Value("${local.server.port}")
    private int puerto;

    private HttpResponse<String> post(String json) throws Exception {
        var peticion = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/api/lia/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("Un mensaje vacío responde 400 antes de llamar a la IA")
    void mensajeVacio() throws Exception {
        var r = post("{\"mensaje\":\"   \",\"historia\":[]}");
        assertThat(r.statusCode()).isEqualTo(400);
        assertThat(r.body()).contains("Escríbele algo a Lía");
    }

    @Test
    @DisplayName("Un mensaje de más de 500 caracteres responde 400")
    void mensajeLargo() throws Exception {
        assertThat(post("{\"mensaje\":\"" + "a".repeat(501) + "\"}").statusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("Un JSON mal formado responde 400 (error de quien llama), no 500")
    void jsonMalFormado() throws Exception {
        assertThat(post("{esto no es json").statusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("CORS: la página en Live Server (puerto 5500) puede llamar a Lía")
    void cors() throws Exception {
        var preflight = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/api/lia/chat"))
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .header("Origin", "http://127.0.0.1:5500")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type")
                .build();
        var r = cliente.send(preflight, HttpResponse.BodyHandlers.ofString());
        assertThat(r.headers().firstValue("Access-Control-Allow-Origin")).contains("http://127.0.0.1:5500");
    }

    @Test
    @DisplayName("CORS: un sitio desconocido NO puede llamar a la API")
    void corsDesconocido() throws Exception {
        var preflight = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/api/lia/chat"))
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .header("Origin", "https://sitio-malicioso.com")
                .header("Access-Control-Request-Method", "POST")
                .build();
        var r = cliente.send(preflight, HttpResponse.BodyHandlers.ofString());
        assertThat(r.headers().firstValue("Access-Control-Allow-Origin")).isEmpty();
    }
}
