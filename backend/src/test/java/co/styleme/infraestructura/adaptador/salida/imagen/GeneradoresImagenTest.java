package co.styleme.infraestructura.adaptador.salida.imagen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import co.styleme.dominio.excepcion.DatoInvalidoException;
import co.styleme.dominio.excepcion.ServicioIaNoDisponibleException;
import co.styleme.dominio.modelo.probador.Imagen;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** Adaptadores de imágenes del probador SIN llamar a los proveedores reales (servidor simulado). */
class GeneradoresImagenTest {

    private static final List<Imagen> REFERENCIAS = List.of(new Imagen(new byte[] {1}, "image/jpeg"), new Imagen(new byte[] {2}, "image/png"));
    private static final String PNG_BASE64 = Base64.getEncoder().encodeToString(new byte[] {(byte) 0x89, 'P', 'N', 'G'});

    @Test
    @DisplayName("OpenAI: manda las imágenes por multipart con la clave y convierte la respuesta en PNG")
    void openAi() {
        RestClient.Builder builder = RestClient.builder().baseUrl(OpenAiImagenAdaptador.URL_BASE);
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        servidor.expect(requestTo(OpenAiImagenAdaptador.URL_BASE + "/images/edits"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer clave-prueba"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
                .andExpect(content().string(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("gpt-image-1"),
                        org.hamcrest.Matchers.containsString("referencia-1.jpg"),
                        org.hamcrest.Matchers.containsString("referencia-2.png"),
                        org.hamcrest.Matchers.containsString("1024x1536"))))
                .andRespond(withSuccess("{\"data\":[{\"b64_json\":\"" + PNG_BASE64 + "\"}]}", MediaType.APPLICATION_JSON));

        var adaptador = new OpenAiImagenAdaptador("clave-prueba", "gpt-image-1", "medium", builder.build());
        Imagen imagen = adaptador.generar("instrucciones", REFERENCIAS);

        assertThat(imagen.tipoMime()).isEqualTo("image/png");
        assertThat(imagen.datos()).hasSize(4);
        servidor.verify();
    }

    @Test
    @DisplayName("OpenAI: sin saldo (429) o sin clave → 'no disponible' (la página usa el probador local)")
    void openAiSinSaldo() {
        RestClient.Builder builder = RestClient.builder().baseUrl(OpenAiImagenAdaptador.URL_BASE);
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        servidor.expect(requestTo(OpenAiImagenAdaptador.URL_BASE + "/images/edits")).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> new OpenAiImagenAdaptador("clave", "gpt-image-1", "medium", builder.build()).generar("x", REFERENCIAS))
                .isInstanceOf(ServicioIaNoDisponibleException.class);
        assertThat(new OpenAiImagenAdaptador("", "gpt-image-1", "medium", builder.build()).disponible()).isFalse();
    }

    @Test
    @DisplayName("Gemini: manda las imágenes como inlineData y lee la imagen de la respuesta")
    void gemini() {
        RestClient.Builder builder = RestClient.builder().baseUrl(GeminiImagenAdaptador.URL_BASE);
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        servidor.expect(requestTo(GeminiImagenAdaptador.URL_BASE + "/models/gemini-2.5-flash-image:generateContent"))
                .andExpect(header("x-goog-api-key", "clave-prueba"))
                .andExpect(jsonPath("$.contents[0].parts[0].text").value("instrucciones"))
                .andExpect(jsonPath("$.contents[0].parts[1].inlineData.mimeType").value("image/jpeg"))
                .andExpect(jsonPath("$.contents[0].parts[2].inlineData.mimeType").value("image/png"))
                .andExpect(jsonPath("$.generationConfig.responseModalities[0]").value("IMAGE"))
                .andRespond(withSuccess("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Listo\"},"
                        + "{\"inlineData\":{\"mimeType\":\"image/png\",\"data\":\"" + PNG_BASE64 + "\"}}]}}]}", MediaType.APPLICATION_JSON));

        Imagen imagen = new GeminiImagenAdaptador("clave-prueba", "gemini-2.5-flash-image", builder.build()).generar("instrucciones", REFERENCIAS);
        assertThat(imagen.datos()).hasSize(4);
        servidor.verify();
    }

    @Test
    @DisplayName("Gemini: plan gratuito sin imágenes (429) o respuesta sin imagen → 'no disponible'")
    void geminiSinImagen() {
        RestClient.Builder builder = RestClient.builder().baseUrl(GeminiImagenAdaptador.URL_BASE);
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        servidor.expect(requestTo(GeminiImagenAdaptador.URL_BASE + "/models/m:generateContent")).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        servidor.expect(requestTo(GeminiImagenAdaptador.URL_BASE + "/models/m:generateContent"))
                .andRespond(withSuccess("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"No puedo\"}]}}]}", MediaType.APPLICATION_JSON));

        var adaptador = new GeminiImagenAdaptador("clave", "m", builder.build());
        assertThatThrownBy(() -> adaptador.generar("x", REFERENCIAS)).isInstanceOf(ServicioIaNoDisponibleException.class);
        assertThatThrownBy(() -> adaptador.generar("x", REFERENCIAS)).isInstanceOf(ServicioIaNoDisponibleException.class);
    }

    @Test
    @DisplayName("Descargar la foto de la prenda: solo https y solo imágenes")
    void descargador() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        servidor.expect(requestTo("https://cdn.tienda/blusa.jpg")).andRespond(withSuccess(new byte[] {1, 2, 3}, MediaType.IMAGE_JPEG));
        servidor.expect(requestTo("https://cdn.tienda/pagina")).andRespond(withSuccess("<html>", MediaType.TEXT_HTML));

        var descargador = new HttpDescargadorImagenAdaptador(builder.build());
        assertThat(descargador.descargar("https://cdn.tienda/blusa.jpg").datos()).hasSize(3);
        assertThatThrownBy(() -> descargador.descargar("https://cdn.tienda/pagina")).isInstanceOf(DatoInvalidoException.class);
        assertThatThrownBy(() -> descargador.descargar("http://inseguro/blusa.jpg")).isInstanceOf(DatoInvalidoException.class);
        assertThatThrownBy(() -> descargador.descargar("file:///etc/passwd")).isInstanceOf(DatoInvalidoException.class);
    }
}
