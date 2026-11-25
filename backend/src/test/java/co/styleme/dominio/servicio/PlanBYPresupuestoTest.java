package co.styleme.dominio.servicio;

import static org.assertj.core.api.Assertions.assertThat;

import co.styleme.dominio.modelo.catalogo.Categoria;
import co.styleme.dominio.modelo.catalogo.Producto;
import co.styleme.dominio.modelo.catalogo.TiendaReferencia;
import co.styleme.dominio.modelo.lia.ConversacionLia;
import co.styleme.dominio.modelo.lia.RespuestaLia;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PlanBYPresupuestoTest {

    private final LectorPresupuesto presupuesto = new LectorPresupuesto();
    private final PlanBLia planB = new PlanBLia();

    @ParameterizedTest(name = "\"{0}\" → {1}")
    @CsvSource({
            "'busco un sérum, máximo 50 mil', 50000",
            "'tengo hasta 150.000 para un vestido', 150000",
            "'algo de menos de 80k', 80000",
            "'presupuesto de 35000', 35000"
    })
    @DisplayName("Entiende el presupuesto escrito de varias formas")
    void entiendePresupuesto(String texto, int esperado) {
        assertThat(presupuesto.leer(texto)).contains(esperado);
    }

    @Test
    @DisplayName("Si no hay presupuesto, no inventa uno")
    void sinPresupuesto() {
        assertThat(presupuesto.leer("quiero un labial rojo")).isEmpty();
    }

    @Test
    @DisplayName("El plan B detecta la categoría por palabras clave, sin importar tildes")
    void detectaCategoria() {
        assertThat(planB.detectarCategoria("necesito un champú para mis rizos")).contains("capilar");
        assertThat(planB.detectarCategoria("un sérum para la piel")).contains("skincare");
        assertThat(planB.detectarCategoria("labial que dure")).contains("maquillaje");
        assertThat(planB.detectarCategoria("vestido para una boda")).contains("ropa");
        assertThat(planB.detectarCategoria("hola, ¿cómo estás?")).isEmpty();
    }

    @Test
    @DisplayName("El plan B recomienda productos reales y se marca como PLAN_B")
    void respondeConProductos() {
        var categoria = new Categoria((short) 3, "skincare", "Skincare");
        var tienda = new TiendaReferencia(9L, "so-beauty", "So Beauty");
        List<Producto> productos = List.of(
                new Producto(37L, "p037", tienda, categoria, "Sérum", null, 35_000, null, null, null, null, null, false, true, null, null),
                new Producto(40L, "p040", tienda, categoria, "Ejemplo", null, 10_000, null, null, null, null, null, true, true, null, null));
        RespuestaLia r = planB.responder(Optional.of("skincare"), productos);
        assertThat(r.modo()).isEqualTo(RespuestaLia.Modo.PLAN_B);
        assertThat(r.productos()).containsExactly("p037"); // el de ejemplo no se recomienda
        assertThat(r.texto()).contains("modo básico");
    }

    @Test
    @DisplayName("El resumen de la conversación acumula sin guardar el texto")
    void resumenConversacion() {
        var ahora = LocalDateTime.now();
        var uno = new ConversacionLia.Intercambio("social", null, "positivo", RespuestaLia.Modo.IA, 0.6, 1000, 500, 100);
        var dos = new ConversacionLia.Intercambio("catalogo", "skincare", "neutral", RespuestaLia.Modo.PLAN_B, 0.8, 2000, 0, 0);
        var tres = new ConversacionLia.Intercambio("general", null, "positivo", RespuestaLia.Modo.IA, 0.7, 1500, 300, 50);

        ConversacionLia c = ConversacionLia.nueva("sesion-1", uno, ahora).con(dos, ahora).con(tres, ahora);

        assertThat(c.cantidadMensajes()).isEqualTo(3);
        assertThat(c.tema()).isEqualTo("catalogo");      // se habló de compras en algún momento
        assertThat(c.categoria()).isEqualTo("skincare");
        assertThat(c.modo()).isEqualTo("MIXTO");          // hubo IA y plan B
        assertThat(c.similitudPromedio()).isCloseTo(0.7, org.assertj.core.api.Assertions.within(1e-9));
        assertThat(c.latenciaPromedioMs()).isEqualTo(1500);
        assertThat(c.tokensEntrada()).isEqualTo(800);
    }
}
