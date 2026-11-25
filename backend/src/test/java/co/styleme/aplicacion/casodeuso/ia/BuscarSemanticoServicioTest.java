package co.styleme.aplicacion.casodeuso.ia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.styleme.dominio.excepcion.DatoInvalidoException;
import co.styleme.dominio.modelo.ia.FiltroBusqueda;
import co.styleme.dominio.modelo.ia.FragmentoConocimiento;
import co.styleme.dominio.modelo.ia.ResultadoBusqueda;
import co.styleme.dominio.modelo.ia.TipoFuente;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BuscarSemanticoServicioTest {

    private Falsos.EmbeddingFalso embeddings;
    private Falsos.AlmacenFalso almacen;
    private BuscarSemanticoServicio servicio;

    private void indexar(FragmentoConocimiento f) {
        almacen.guardar(f, Falsos.EmbeddingFalso.vectorizar(f.texto()), "modelo-falso");
    }

    @BeforeEach
    void preparar() {
        embeddings = new Falsos.EmbeddingFalso();
        almacen = new Falsos.AlmacenFalso();
        servicio = new BuscarSemanticoServicio(embeddings, almacen);
        indexar(FragmentoConocimiento.deProducto(1, "Shampoo rizos", "shampoo sin sulfatos para cabello rizado y afro"));
        indexar(FragmentoConocimiento.deProducto(2, "Sérum", "serum de niacinamida para piel grasa con poros abiertos"));
        indexar(FragmentoConocimiento.deGuia("Protector", "protector solar todos los dias para prevenir manchas"));
    }

    @Test
    @DisplayName("Trae primero lo más parecido a la consulta")
    void masParecidoPrimero() {
        List<ResultadoBusqueda> r = servicio.buscar("shampoo para cabello rizado", new FiltroBusqueda(null, null, null, 3, 0));
        assertThat(r.get(0).titulo()).isEqualTo("Shampoo rizos");
    }

    @Test
    @DisplayName("Respeta el filtro de tipo de la búsqueda híbrida")
    void filtroTipo() {
        List<ResultadoBusqueda> r = servicio.buscar("manchas", new FiltroBusqueda(null, null, TipoFuente.GUIA, 3, 0));
        assertThat(r).extracting(ResultadoBusqueda::tipo).containsOnly(TipoFuente.GUIA);
    }

    @Test
    @DisplayName("Una búsqueda vacía o demasiado larga es un error del usuario (400), no se llama a la IA")
    void consultaInvalida() {
        assertThatThrownBy(() -> servicio.buscar("   ", null)).isInstanceOf(DatoInvalidoException.class);
        assertThatThrownBy(() -> servicio.buscar("a".repeat(301), null)).isInstanceOf(DatoInvalidoException.class);
    }

    @Test
    @DisplayName("Sin filtro usa el filtro por defecto (top 5, umbral 0.5)")
    void filtroPorDefecto() {
        assertThat(servicio.buscar("serum de niacinamida para piel grasa", null)).hasSizeLessThanOrEqualTo(5);
    }
}
