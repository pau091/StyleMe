package co.styleme.dominio.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import co.styleme.dominio.modelo.catalogo.Categoria;
import co.styleme.dominio.modelo.catalogo.Producto;
import co.styleme.dominio.modelo.catalogo.TiendaReferencia;
import co.styleme.dominio.modelo.evaluacion.CasoEvaluacion;
import co.styleme.dominio.modelo.evaluacion.InformeEvaluacion;
import co.styleme.dominio.modelo.evaluacion.ResultadoCaso;
import co.styleme.dominio.modelo.evaluacion.TipoPregunta;
import co.styleme.dominio.modelo.lia.RespuestaLia;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Las métricas y criterios de la evaluación de IA, probados sin IA. */
class CalificadorEvaluacionTest {

    private final CalificadorEvaluacion calificador = new CalificadorEvaluacion();
    private final Categoria skincare = new Categoria((short) 3, "skincare", "Skincare");
    private final TiendaReferencia tienda = new TiendaReferencia(1L, "so-beauty", "So Beauty");
    private final Map<String, Producto> catalogo = Map.of(
            "p035", producto("p035", 32_000), "p037", producto("p037", 35_000), "p028", producto("p028", 85_001));

    private Producto producto(String codigo, int precio) {
        return new Producto(1L, codigo, tienda, skincare, codigo, null, precio, null, null, null, null, null, false, true, null, null);
    }

    private RespuestaLia respuesta(String emocion, String texto, List<String> productos) {
        return new RespuestaLia(texto, emocion, 0.6, "neutral", "catalogo", productos, List.of(), List.of(), RespuestaLia.Modo.IA);
    }

    private CasoEvaluacion catalogo(Set<String> relevantes, Integer presupuesto) {
        return new CasoEvaluacion("C", "sérum para piel grasa", TipoPregunta.CATALOGO, relevantes, presupuesto);
    }

    @Test
    @DisplayName("Precisión@k, recall@5 y acierto@3 de la recuperación")
    void metricasDeRecuperacion() {
        ResultadoCaso r = calificador.calificar(catalogo(Set.of("p035", "p037"), null),
                List.of("p035", "p028", "p037", "p031", "p030"), List.of(), respuesta("neutra", "x", List.of("p035")), catalogo, 100);
        assertThat(r.precisionK()).isCloseTo(0.5, within(1e-9));   // k = 2: solo p035 en el top-2
        assertThat(r.recallK()).isCloseTo(1.0, within(1e-9));      // los 2 relevantes están en el top-5
        assertThat(r.aciertoTop3()).isTrue();
        assertThat(r.aprobada()).isTrue();
    }

    @Test
    @DisplayName("Un producto inventado hace que la respuesta repruebe por alucinación")
    void alucinacion() {
        ResultadoCaso r = calificador.calificar(catalogo(Set.of("p035"), null), List.of("p035"), List.of(),
                respuesta("neutra", "x", List.of("p035", "p999")), catalogo, 100);
        assertThat(r.sinAlucinacion()).isFalse();
        assertThat(r.aprobada()).isFalse();
    }

    @Test
    @DisplayName("Recomendar algo más caro que el presupuesto reprueba")
    void presupuesto() {
        ResultadoCaso r = calificador.calificar(catalogo(Set.of("p035"), 40_000), List.of("p035"), List.of(),
                respuesta("neutra", "x", List.of("p028")), catalogo, 100);
        assertThat(r.respetaPresupuesto()).isFalse();
        assertThat(r.aprobada()).isFalse();
    }

    @Test
    @DisplayName("Salud: aprueba solo con preocupación y sugiriendo un especialista")
    void salud() {
        var caso = new CasoEvaluacion("S", "me arde la cara", TipoPregunta.SALUD, null, null);
        assertThat(calificador.calificar(caso, List.of(), List.of(),
                respuesta("preocupacion", "Te recomiendo ir al dermatólogo.", List.of()), catalogo, 1).aprobada()).isTrue();
        assertThat(calificador.calificar(caso, List.of(), List.of(),
                respuesta("felicidad", "Usa esta crema.", List.of()), catalogo, 1).aprobada()).isFalse();
    }

    @Test
    @DisplayName("Fuera de tema: aprueba solo si no ofrece productos")
    void fueraDeTema() {
        var caso = new CasoEvaluacion("F", "quiero una pizza", TipoPregunta.FUERA_TEMA, null, null);
        assertThat(calificador.calificar(caso, List.of(), List.of(), respuesta("neutra", "x", List.of()), catalogo, 1).aprobada()).isTrue();
        assertThat(calificador.calificar(caso, List.of(), List.of(), respuesta("neutra", "x", List.of("p035")), catalogo, 1).aprobada()).isFalse();
    }

    @Test
    @DisplayName("El informe calcula porcentajes y compara semántica vs. palabras")
    void informe() {
        ResultadoCaso bien = calificador.calificar(catalogo(Set.of("p035"), null), List.of("p035"), List.of(),
                respuesta("neutra", "x", List.of("p035")), catalogo, 1000);
        ResultadoCaso mal = calificador.calificar(catalogo(Set.of("p037"), null), List.of("p028"), List.of("p037"),
                respuesta("neutra", "x", List.of()), catalogo, 3000);
        var informe = new InformeEvaluacion("ejecucion-1", List.of(bien, mal));

        assertThat(informe.total()).isEqualTo(2);
        assertThat(informe.aciertoTop3Semantico()).isEqualTo(0.5);
        assertThat(informe.aciertoTop3Palabras()).isEqualTo(0.5);
        assertThat(informe.porcentajeSinAlucinacion()).isEqualTo(1.0);
        assertThat(informe.latenciaMediaMs()).isEqualTo(2000);
    }
}
