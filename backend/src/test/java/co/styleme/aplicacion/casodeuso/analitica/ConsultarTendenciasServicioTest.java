package co.styleme.aplicacion.casodeuso.analitica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.styleme.aplicacion.puerto.salida.persistencia.TendenciaRepositorioPuerto;
import co.styleme.dominio.excepcion.DatoInvalidoException;
import co.styleme.dominio.modelo.analitica.Tendencias;
import co.styleme.dominio.modelo.analitica.Tendencias.Conteo;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tendencias de la portada con un puerto falso (Mockito). */
class ConsultarTendenciasServicioTest {

    private TendenciaRepositorioPuerto repositorio;
    private ConsultarTendenciasServicio servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(TendenciaRepositorioPuerto.class);
        servicio = new ConsultarTendenciasServicio(repositorio);
    }

    @Test
    @DisplayName("Pide las búsquedas con el mínimo de 3 personas y el límite de 5, y junta las tres listas")
    void juntaLasListas() {
        when(repositorio.busquedas(7, Tendencias.MINIMO_SESIONES, Tendencias.LIMITE)).thenReturn(List.of(new Conteo("jean", 64)));
        when(repositorio.tiendasMasVistas(7, Tendencias.LIMITE)).thenReturn(List.of(new Conteo("bohor", 120)));
        when(repositorio.productosMasVistos(anyInt(), anyInt())).thenReturn(List.of());

        Tendencias tendencias = servicio.ultimosDias(7);

        assertThat(tendencias.dias()).isEqualTo(7);
        assertThat(tendencias.busquedas()).containsExactly(new Conteo("jean", 64));
        assertThat(tendencias.tiendas()).extracting(Conteo::referencia).containsExactly("bohor");
        assertThat(tendencias.productos()).isEmpty();
        verify(repositorio).busquedas(7, 3, 5);
    }

    @Test
    @DisplayName("Un periodo fuera de 1 a 90 días es un error y no se consulta la base")
    void periodoInvalido() {
        assertThatThrownBy(() -> servicio.ultimosDias(0)).isInstanceOf(DatoInvalidoException.class);
        assertThatThrownBy(() -> servicio.ultimosDias(91)).isInstanceOf(DatoInvalidoException.class);
        verifyNoInteractions(repositorio);
    }
}
