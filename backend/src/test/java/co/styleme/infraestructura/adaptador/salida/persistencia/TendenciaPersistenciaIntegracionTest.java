package co.styleme.infraestructura.adaptador.salida.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import co.styleme.dominio.modelo.analitica.Tendencias.Conteo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** Tendencias contra la base REAL; @Transactional deshace los eventos de prueba al terminar. */
@SpringBootTest
@Transactional
class TendenciaPersistenciaIntegracionTest {

    private static final int SIN_LIMITE = 100_000;

    @Autowired
    private TendenciaPersistenciaAdaptador tendencias;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void eventosDePrueba() {
        // 3 personas buscan lo mismo (con mayúsculas y espacios distintos) y encuentran resultados
        busqueda("tendencia-a", "Zz Prueba Tendencia", 4);
        busqueda("tendencia-b", "zz prueba tendencia ", 2);
        busqueda("tendencia-c", "zz prueba tendencia", 1);
        // Una sola persona, aunque busque 3 veces: no debe aparecer
        busqueda("tendencia-d", "zz solo una persona", 3);
        busqueda("tendencia-d", "zz solo una persona", 3);
        busqueda("tendencia-d", "zz solo una persona", 3);
        // 3 personas, pero sin resultados: tampoco aparece
        busqueda("tendencia-a", "zz sin resultados", 0);
        busqueda("tendencia-b", "zz sin resultados", 0);
        busqueda("tendencia-c", "zz sin resultados", 0);
    }

    @Test
    @DisplayName("Búsquedas: se agrupan sin mayúsculas ni espacios y solo salen las de 3+ personas con resultados")
    void busquedas() {
        var lista = tendencias.busquedas(1, 3, SIN_LIMITE);
        assertThat(lista).contains(new Conteo("zz prueba tendencia", 3));
        assertThat(lista).extracting(Conteo::referencia).doesNotContain("zz solo una persona", "zz sin resultados");
    }

    @Test
    @DisplayName("Tiendas y productos: cuentan personas distintas, no clics")
    void tiendasYProductos() {
        int antes = personasQueVieron("bohor");
        for (int i = 0; i < 3; i++) {
            jdbc.update("INSERT INTO evento (tipo, sesion_id, tienda_id) VALUES ('VER_TIENDA', 'tendencia-unica', (SELECT id FROM tienda WHERE slug = 'bohor'))");
        }
        assertThat(personasQueVieron("bohor")).isEqualTo(antes + 1);
        assertThat(tendencias.productosMasVistos(90, SIN_LIMITE)).allMatch(c -> c.personas() > 0);
    }

    private int personasQueVieron(String slug) {
        return tendencias.tiendasMasVistas(1, SIN_LIMITE).stream()
                .filter(c -> c.referencia().equals(slug)).mapToInt(c -> (int) c.personas()).findFirst().orElse(0);
    }

    private void busqueda(String sesion, String texto, int resultados) {
        jdbc.update("INSERT INTO evento (tipo, sesion_id, texto_busqueda, cantidad_resultados) VALUES ('BUSQUEDA', ?, ?, ?)",
                sesion, texto, resultados);
    }
}
