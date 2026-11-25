package co.styleme.dominio.modelo.comunidad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.styleme.dominio.excepcion.DatoInvalidoException;
import co.styleme.dominio.modelo.analitica.Evento;
import co.styleme.dominio.modelo.analitica.Indicador;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Reglas de reseñas, favoritos y eventos, sin Spring ni base de datos. */
class ComunidadYAnaliticaTest {

    @Test
    @DisplayName("Una reseña nueva queda PENDIENTE y sin nombre se muestra como 'Anónimo'")
    void resenaNueva() {
        Resena r = Resena.nueva("sesion-1", "  ", Objetivo.tienda("bohor"), 5, "Excelente");
        assertThat(r.estado()).isEqualTo(Resena.Estado.PENDIENTE);
        assertThat(r.autor()).isEqualTo("Anónimo");
    }

    @Test
    @DisplayName("Los corazones van de 1 a 5")
    void corazones() {
        assertThatThrownBy(() -> Resena.nueva("s", null, Objetivo.tienda("bohor"), 0, null)).isInstanceOf(DatoInvalidoException.class);
        assertThatThrownBy(() -> Resena.nueva("s", null, Objetivo.tienda("bohor"), 6, null)).isInstanceOf(DatoInvalidoException.class);
    }

    @Test
    @DisplayName("Solo se modera lo pendiente, y moderar es publicar o rechazar")
    void moderacion() {
        Resena r = Resena.nueva("s", null, Objetivo.producto("p035"), 4, null);
        Resena publicada = r.moderada(Resena.Estado.PUBLICADA);
        assertThat(publicada.estado()).isEqualTo(Resena.Estado.PUBLICADA);
        assertThatThrownBy(() -> publicada.moderada(Resena.Estado.RECHAZADA)).isInstanceOf(DatoInvalidoException.class);
        assertThatThrownBy(() -> r.moderada(Resena.Estado.PENDIENTE)).isInstanceOf(DatoInvalidoException.class);
    }

    @Test
    @DisplayName("El objetivo es una tienda o un producto, nunca los dos ni ninguno")
    void objetivoUnoDeDos() {
        assertThat(Objetivo.deUno("bohor", null).esTienda()).isTrue();
        assertThat(Objetivo.deUno(null, "p035").esTienda()).isFalse();
        assertThatThrownBy(() -> Objetivo.deUno("bohor", "p035")).isInstanceOf(DatoInvalidoException.class);
        assertThatThrownBy(() -> Objetivo.deUno(null, " ")).isInstanceOf(DatoInvalidoException.class);
    }

    @Test
    @DisplayName("Cada tipo de evento exige sus datos (una visita a tienda necesita la tienda)")
    void eventoExigeDatos() {
        assertThatThrownBy(() -> new Evento(Evento.Tipo.VER_TIENDA, "s", null, null, null, null, null, null, null))
                .isInstanceOf(DatoInvalidoException.class);
        assertThatThrownBy(() -> new Evento(Evento.Tipo.BUSQUEDA, "s", null, null, null, "zapatos", null, null, null))
                .isInstanceOf(DatoInvalidoException.class);
        assertThat(new Evento(Evento.Tipo.ABRIR_LIA, "s", null, null, null, null, null, "celular", null).dispositivo())
                .isEqualTo("celular");
    }

    @Test
    @DisplayName("Una búsqueda con 0 resultados es demanda insatisfecha; un dispositivo raro se ignora")
    void busquedaSinResultados() {
        Evento e = new Evento(Evento.Tipo.BUSQUEDA, "s", null, null, null, "zapatos", 0, "nevera", Map.of("origen", "catalogo"));
        assertThat(e.esBusquedaSinResultados()).isTrue();
        assertThat(e.dispositivo()).isNull();
    }

    @Test
    @DisplayName("Los indicadores se buscan por su código")
    void indicadores() {
        assertThat(Indicador.Codigo.desdeCodigo("conversion_tiendas")).isEqualTo(Indicador.Codigo.CONVERSION_TIENDAS);
        assertThatThrownBy(() -> Indicador.Codigo.desdeCodigo("inventado")).isInstanceOf(DatoInvalidoException.class);
    }
}
