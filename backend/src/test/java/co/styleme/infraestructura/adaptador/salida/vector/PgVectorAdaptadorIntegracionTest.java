package co.styleme.infraestructura.adaptador.salida.vector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import co.styleme.dominio.modelo.ia.Embedding;
import co.styleme.dominio.modelo.ia.FiltroBusqueda;
import co.styleme.dominio.modelo.ia.FragmentoConocimiento;
import co.styleme.dominio.modelo.ia.ResultadoBusqueda;
import co.styleme.dominio.modelo.ia.TipoFuente;
import co.styleme.infraestructura.adaptador.salida.conocimiento.GuiasClasspathAdaptador;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * El adaptador pgvector contra la base REAL. @Transactional: todo lo que la prueba guarda
 * se deshace al terminar (rollback), así la base queda como estaba.
 * Usa vectores "de juguete" (no Gemini) para saber de antemano cuál debe ser el más parecido.
 */
@SpringBootTest
@Transactional
class PgVectorAdaptadorIntegracionTest {

    private static final String MODELO = "modelo-de-prueba";

    @Autowired
    private PgVectorAdaptador almacen;

    @Autowired
    private GuiasClasspathAdaptador guias;

    @Autowired
    private JdbcTemplate jdbc;

    private long idSerum;
    private long idBohor;

    /** Vector que apunta sobre todo hacia la dimensión "eje", con algo de la dimensión "otra". */
    private static Embedding vector(int eje, float valor, int otra, float valorOtra) {
        float[] v = new float[Embedding.DIMENSIONES];
        v[eje] = valor;
        v[otra] += valorOtra;
        return Embedding.de(v);
    }

    @BeforeEach
    void preparar() {
        jdbc.update("DELETE FROM fragmento_conocimiento");
        idSerum = jdbc.queryForObject("SELECT id FROM producto WHERE codigo = 'p028'", Long.class);
        idBohor = jdbc.queryForObject("SELECT id FROM tienda WHERE slug = 'bohor'", Long.class);
        almacen.guardar(FragmentoConocimiento.deProducto(idSerum, "Sérum Centella", "Sérum para piel sensible"),
                vector(0, 1f, 1, 0.1f), MODELO);
        almacen.guardar(FragmentoConocimiento.deTienda(idBohor, "BOHOR", "Tienda de ropa en Cabecera"),
                vector(1, 1f, 0, 0.1f), MODELO);
        almacen.guardar(FragmentoConocimiento.deGuia("Rutina · Protector solar", "Usa protector solar todos los días"),
                vector(2, 1f, 0, 0.3f), MODELO);
    }

    @Test
    @DisplayName("Devuelve primero el fragmento más parecido, con similitud cercana a 1")
    void masParecidoPrimero() {
        List<ResultadoBusqueda> r = almacen.buscarParecidos(vector(0, 1f, 1, 0.1f),
                new FiltroBusqueda(null, null, null, 3, 0));
        assertThat(r).hasSize(3);
        assertThat(r.get(0).codigoProducto()).isEqualTo("p028");
        assertThat(r.get(0).slugTienda()).isEqualTo("pandora-makeup");
        assertThat(r.get(0).similitud()).isCloseTo(1.0, within(1e-3));
        assertThat(r).isSortedAccordingTo((a, b) -> Double.compare(b.similitud(), a.similitud()));
    }

    @Test
    @DisplayName("Búsqueda híbrida: el filtro de categoría deja solo lo de esa categoría")
    void filtroCategoria() {
        List<ResultadoBusqueda> r = almacen.buscarParecidos(vector(0, 1f, 1, 0.1f),
                new FiltroBusqueda("ropa", null, null, 5, 0));
        assertThat(r).extracting(ResultadoBusqueda::slugTienda).containsOnly("bohor");
    }

    @Test
    @DisplayName("El filtro de tipo deja solo guías; el umbral descarta lo poco parecido")
    void filtroTipoYUmbral() {
        assertThat(almacen.buscarParecidos(vector(0, 1f, 1, 0.1f), new FiltroBusqueda(null, null, TipoFuente.GUIA, 5, 0)))
                .extracting(ResultadoBusqueda::tipo).containsOnly(TipoFuente.GUIA);
        assertThat(almacen.buscarParecidos(vector(0, 1f, 1, 0.1f), new FiltroBusqueda(null, null, null, 5, 0.9)))
                .hasSize(1);
    }

    @Test
    @DisplayName("El presupuesto excluye productos más caros (el sérum cuesta $85.001)")
    void presupuesto() {
        assertThat(almacen.buscarParecidos(vector(0, 1f, 1, 0.1f), new FiltroBusqueda(null, 50_000, TipoFuente.PRODUCTO, 5, 0)))
                .isEmpty();
    }

    @Test
    @DisplayName("Guardar dos veces la misma clave actualiza (no duplica); eliminarExcepto borra lo sobrante")
    void actualizarYEliminar() {
        var serum = FragmentoConocimiento.deProducto(idSerum, "Sérum Centella", "Texto nuevo del sérum");
        almacen.guardar(serum, vector(0, 1f, 1, 0.2f), MODELO);
        assertThat(almacen.contar()).isEqualTo(3);
        assertThat(almacen.hashesPorClave(MODELO)).containsEntry(serum.clave(), serum.hashTexto());

        int borrados = almacen.eliminarExcepto(Set.of(serum.clave()));
        assertThat(borrados).isEqualTo(2);
        assertThat(almacen.contar()).isEqualTo(1);
    }

    @Test
    @DisplayName("Los fragmentos guardados con otro modelo no cuentan: hay que recalcularlos")
    void otroModelo() {
        assertThat(almacen.hashesPorClave("otro-modelo")).isEmpty();
    }

    @Test
    @DisplayName("Lee las 3 guías de conocimiento del proyecto")
    void leeGuias() {
        assertThat(guias.listar()).extracting("archivo")
                .containsExactly("cuidado-capilar.md", "estilo-y-ocasiones.md", "rutina-skincare.md");
    }
}
