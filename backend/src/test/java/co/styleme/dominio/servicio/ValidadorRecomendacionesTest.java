package co.styleme.dominio.servicio;

import static org.assertj.core.api.Assertions.assertThat;

import co.styleme.dominio.modelo.lia.AccionLia;
import co.styleme.dominio.modelo.lia.DatosModelo;
import co.styleme.dominio.modelo.lia.RespuestaLia;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** La regla de oro contra las alucinaciones, probada sin IA. */
class ValidadorRecomendacionesTest {

    private final ValidadorRecomendaciones validador = new ValidadorRecomendaciones();
    private final Set<String> candidatos = Set.of("p035", "p037", "p028");
    private final Set<String> tiendas = Set.of("mirada-de-angel", "so-beauty");

    private DatosModelo datos(String intencion, String emocion, List<String> productos, List<AccionLia> acciones) {
        return new DatosModelo("positivo", intencion, emocion, 0.8, productos, acciones, List.of("¿Y un protector?"));
    }

    @Test
    @DisplayName("Un producto INVENTADO (que no estaba en el contexto) se descarta")
    void descartaProductoInventado() {
        RespuestaLia r = validador.revisar(datos("catalogo", "felicidad", List.of("p035", "p999", "p001"), List.of()),
                candidatos, tiendas, "algo para piel grasa", "texto");
        assertThat(r.productos()).containsExactly("p035");
    }

    @Test
    @DisplayName("Máximo 3 productos y sin repetidos")
    void maximoTres() {
        RespuestaLia r = validador.revisar(datos("catalogo", null, List.of("p035", "p035", "p037", "p028", "p037"), List.of()),
                Set.of("p035", "p037", "p028"), tiendas, "x", "texto");
        assertThat(r.productos()).containsExactly("p035", "p037", "p028");
    }

    @Test
    @DisplayName("Una acción con una tienda que no existe se quita")
    void accionInvalida() {
        RespuestaLia r = validador.revisar(datos("catalogo", null, List.of(), List.of(
                        new AccionLia("ver_tienda", "tienda-inventada", null, null),
                        new AccionLia("ver_tienda", "so-beauty", null, null),
                        new AccionLia("volar", null, null, null))),
                candidatos, tiendas, "x", "texto");
        assertThat(r.acciones()).containsExactly(new AccionLia("ver_tienda", "so-beauty", null, null));
    }

    @Test
    @DisplayName("En una conversación general no se ofrecen productos ni acciones")
    void conversacionGeneral() {
        RespuestaLia r = validador.revisar(datos("general", "felicidad", List.of("p035"),
                List.of(new AccionLia("armar_salida", null, null, null))), candidatos, tiendas, "cuéntame un chiste", "texto");
        assertThat(r.productos()).isEmpty();
        assertThat(r.acciones()).isEmpty();
    }

    @Test
    @DisplayName("Ante un tema de salud, Lía muestra preocupación aunque la IA diga 'felicidad'")
    void temaDeSalud() {
        RespuestaLia r = validador.revisar(datos("catalogo", "felicidad", List.of(), List.of()),
                candidatos, tiendas, "me arde la cara con una crema", "texto");
        assertThat(r.emocion()).isEqualTo("preocupacion");
    }

    @Test
    @DisplayName("Valores desconocidos se cambian por valores seguros")
    void valoresDesconocidos() {
        RespuestaLia r = validador.revisar(new DatosModelo("rarísimo", "otra", "furia", 7.0, null, null, null),
                candidatos, tiendas, "x", "texto");
        assertThat(r.emocion()).isEqualTo("neutra");
        assertThat(r.sentimiento()).isEqualTo("neutral");
        assertThat(r.intensidad()).isEqualTo(1.0);
        assertThat(r.modo()).isEqualTo(RespuestaLia.Modo.IA);
    }

    @Test
    @DisplayName("Valores vacíos (null) dentro de las listas no hacen caer el validador")
    void valoresNulos() {
        RespuestaLia r = validador.revisar(new DatosModelo(null, null, null, null,
                java.util.Arrays.asList("p035", null), java.util.Arrays.asList(null, new AccionLia(null, null, null, null)),
                java.util.Arrays.asList(null, " ")), candidatos, tiendas, null, "texto");
        assertThat(r.productos()).containsExactly("p035");
        assertThat(r.acciones()).isEmpty();
        assertThat(r.sugerencias()).isEmpty();
    }

    @Test
    @DisplayName("'entusiasmo' se muestra con la expresión de felicidad")
    void expresionEquivalente() {
        assertThat(validador.revisar(datos("social", "entusiasmo", null, null), candidatos, tiendas, "x", "t").emocion())
                .isEqualTo("felicidad");
    }
}
