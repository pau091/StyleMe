package co.styleme.infraestructura.adaptador.salida;

import static org.assertj.core.api.Assertions.assertThat;

import co.styleme.dominio.modelo.catalogo.FiltroProductos;
import co.styleme.dominio.modelo.catalogo.Producto;
import co.styleme.dominio.modelo.evaluacion.CasoEvaluacion;
import co.styleme.dominio.modelo.evaluacion.ResultadoCaso;
import co.styleme.dominio.modelo.evaluacion.TipoPregunta;
import co.styleme.dominio.modelo.lia.RespuestaLia;
import co.styleme.infraestructura.adaptador.salida.conocimiento.ConjuntoEvaluacionClasspathAdaptador;
import co.styleme.infraestructura.adaptador.salida.persistencia.EvaluacionPersistenciaAdaptador;
import co.styleme.infraestructura.adaptador.salida.persistencia.ProductoPersistenciaAdaptador;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** El conjunto de evaluación y su almacenamiento, contra la base REAL (rollback al terminar). */
@SpringBootTest
@Transactional
class EvaluacionInfraestructuraIntegracionTest {

    @Autowired
    private ConjuntoEvaluacionClasspathAdaptador conjunto;

    @Autowired
    private EvaluacionPersistenciaAdaptador evaluaciones;

    @Autowired
    private ProductoPersistenciaAdaptador productos;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @DisplayName("El conjunto tiene 30 preguntas de los 5 tipos")
    void treintaPreguntas() {
        List<CasoEvaluacion> casos = conjunto.casos();
        assertThat(casos).hasSize(30);
        assertThat(casos).extracting(CasoEvaluacion::tipo).contains(TipoPregunta.values());
        assertThat(casos).extracting(CasoEvaluacion::id).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("La verdad de referencia es válida: cada producto 'relevante' existe, es real y cumple el presupuesto")
    void verdadDeReferenciaValida() {
        Map<String, Producto> catalogo = productos.buscar(FiltroProductos.todos()).stream()
                .collect(Collectors.toMap(Producto::codigo, Function.identity()));
        for (CasoEvaluacion caso : conjunto.casos()) {
            for (String codigo : caso.relevantes()) {
                Producto p = catalogo.get(codigo);
                assertThat(p).as("%s: el producto %s no existe", caso.id(), codigo).isNotNull();
                assertThat(p.esEjemplo()).as("%s: %s es un producto de ejemplo", caso.id(), codigo).isFalse();
                if (caso.presupuesto() != null) {
                    assertThat(p.cuestaMaximo(caso.presupuesto())).as("%s: %s se pasa del presupuesto", caso.id(), codigo).isTrue();
                }
            }
        }
    }

    @Test
    @DisplayName("Guarda un resultado en evaluacion_ia con esperado y obtenido en JSONB")
    void guardaResultado() {
        var caso = new CasoEvaluacion("C99", "sérum para piel grasa", TipoPregunta.CATALOGO, Set.of("p035"), 40_000);
        var r = new ResultadoCaso(caso, List.of("p035", "p037"), List.of(), List.of("p035"), "neutra", RespuestaLia.Modo.IA,
                "Te recomiendo el Suero Detox.", 1.0, 1.0, true, false, true, true, true, 2100);

        evaluaciones.guardar("ejecucion-prueba", r);

        Map<String, Object> fila = jdbc.queryForMap("""
                SELECT tipo_pregunta, aprobada, precision_k, esperado->>'presupuesto' AS presupuesto,
                       obtenido->'recomendados'->>0 AS primero
                FROM evaluacion_ia WHERE ejecucion = 'ejecucion-prueba'""");
        assertThat(fila).containsEntry("tipo_pregunta", "catalogo").containsEntry("aprobada", true)
                .containsEntry("presupuesto", "40000").containsEntry("primero", "p035");
    }

}
