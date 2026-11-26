package co.styleme.infraestructura.adaptador.entrada.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * El probador con IA por HTTP real, SIN proveedor (styleme.probador.proveedor=ninguno en las pruebas):
 * así se comprueba lo que ve la página cuando no hay clave de imágenes → usa el probador local.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProbadorApiIntegracionTest {

    private static final String LIMITE = "limite-de-prueba-styleme";
    private final HttpClient cliente = HttpClient.newHttpClient();

    @Value("${local.server.port}")
    private int puerto;

    @Test
    @DisplayName("GET /api/probador/estado: apagado, con las 4 poses")
    void estado() throws Exception {
        var respuesta = cliente.send(HttpRequest.newBuilder(URI.create(url("/api/probador/estado"))).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(respuesta.statusCode()).isEqualTo(200);
        assertThat(respuesta.body()).contains("\"disponible\":false", "\"codigo\":\"FRENTE\"", "\"codigo\":\"CAMINANDO\"");
    }

    @Test
    @DisplayName("POST con una foto válida pero sin proveedor: 503 (la página cambia al probador local)")
    void sinProveedor() throws Exception {
        assertThat(enviar(new byte[] {(byte) 0xFF, (byte) 0xD8, 1}, "image/jpeg", "p001", "FRENTE").statusCode()).isEqualTo(503);
    }

    @Test
    @DisplayName("Archivo que no es imagen, pose desconocida o sin foto: 400")
    void invalidos() throws Exception {
        assertThat(enviar("hola".getBytes(StandardCharsets.UTF_8), "text/plain", "p001", "FRENTE").statusCode()).isEqualTo(400);
        assertThat(enviar(new byte[] {1}, "image/png", "p001", "ACOSTADA").statusCode()).isEqualTo(400);
        assertThat(enviar(null, null, "p001", "FRENTE").statusCode()).isEqualTo(400);
    }

    private HttpResponse<String> enviar(byte[] foto, String tipo, String producto, String pose) throws IOException, InterruptedException {
        ByteArrayOutputStream cuerpo = new ByteArrayOutputStream();
        campo(cuerpo, "producto", producto);
        campo(cuerpo, "pose", pose);
        if (foto != null) {
            cuerpo.write(("--" + LIMITE + "\r\nContent-Disposition: form-data; name=\"foto\"; filename=\"foto\"\r\n"
                    + "Content-Type: " + tipo + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            cuerpo.write(foto);
            cuerpo.write("\r\n".getBytes(StandardCharsets.UTF_8));
        }
        cuerpo.write(("--" + LIMITE + "--\r\n").getBytes(StandardCharsets.UTF_8));
        var peticion = HttpRequest.newBuilder(URI.create(url("/api/probador")))
                .header("Content-Type", "multipart/form-data; boundary=" + LIMITE)
                .POST(HttpRequest.BodyPublishers.ofByteArray(cuerpo.toByteArray())).build();
        return cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
    }

    private static void campo(ByteArrayOutputStream cuerpo, String nombre, String valor) throws IOException {
        cuerpo.write(("--" + LIMITE + "\r\nContent-Disposition: form-data; name=\"" + nombre + "\"\r\n\r\n" + valor + "\r\n")
                .getBytes(StandardCharsets.UTF_8));
    }

    private String url(String ruta) {
        return "http://localhost:" + puerto + ruta;
    }
}
