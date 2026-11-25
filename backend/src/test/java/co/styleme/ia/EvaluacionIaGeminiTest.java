package co.styleme.ia;

import static org.assertj.core.api.Assertions.assertThat;

import co.styleme.aplicacion.puerto.entrada.ia.EvaluarIaCasoUso;
import co.styleme.aplicacion.puerto.entrada.ia.IndexarConocimientoCasoUso;
import co.styleme.dominio.modelo.evaluacion.InformeEvaluacion;
import co.styleme.dominio.modelo.evaluacion.ResultadoCaso;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * EVALUACIÓN COMPLETA de la IA con Gemini real: las 30 preguntas.
 * Solo corre si hay clave de Gemini. Tarda 1–3 minutos y deja los resultados en evaluacion_ia
 * (es la evidencia que usa el informe de calidad de IA).
 *
 * Metas (docs/planeacion/12-lia-asesora-virtual.md, sección 8):
 *   - 0 % de productos inventados          → sin alucinación = 100 %
 *   - respeta el presupuesto                → 100 %
 *   - producto correcto entre los 3 primeros → ≥ 80 %
 */
@SpringBootTest
@Tag("ia")
@EnabledIf("co.styleme.ia.BusquedaSemanticaGeminiIntegracionTest#hayClaveGemini")
class EvaluacionIaGeminiTest {

    @Autowired
    private IndexarConocimientoCasoUso indexar;

    @Autowired
    private EvaluarIaCasoUso evaluar;

    @Test
    @DisplayName("Lía y la búsqueda semántica cumplen las metas de calidad")
    void cumpleLasMetas() {
        indexar.indexar(); // asegura el índice al día (sin productos de ejemplo)
        InformeEvaluacion i = evaluar.evaluar();

        System.out.printf("%n===== EVALUACIÓN DE IA (%s) =====%n", i.ejecucion());
        System.out.printf("Aprobadas: %.1f %% de %d%n", i.porcentajeAprobadas() * 100, i.total());
        System.out.printf("Acierto@3 semántico: %.1f %% | por palabras: %.1f %%%n", i.aciertoTop3Semantico() * 100, i.aciertoTop3Palabras() * 100);
        System.out.printf("Precisión@k media: %.1f %% | Recall@5 medio: %.1f %%%n", i.precisionMedia() * 100, i.recallMedio() * 100);
        System.out.printf("Sin alucinación: %.1f %% | Presupuesto: %.1f %% | Salud: %.1f %% | Fuera de tema: %.1f %%%n",
                i.porcentajeSinAlucinacion() * 100, i.porcentajeRespetaPresupuesto() * 100, i.porcentajeSalud() * 100,
                i.porcentajeSinComprasFueraDeTema() * 100);
        System.out.printf("Latencia media: %.0f ms | Respuestas con plan B: %d%n", i.latenciaMediaMs(), i.respuestasConPlanB());
        for (ResultadoCaso r : i.resultados()) {
            System.out.printf("  %s %-4s %-10s %s → recuperados=%s recomendados=%s%n", r.aprobada() ? "✔" : "✘", r.caso().id(),
                    r.caso().tipo().codigo(), r.caso().pregunta(), r.recuperados(), r.recomendados());
        }

        assertThat(i.total()).isEqualTo(30);
        assertThat(i.porcentajeSinAlucinacion()).as("productos inventados").isEqualTo(1.0);
        assertThat(i.porcentajeRespetaPresupuesto()).as("presupuesto").isEqualTo(1.0);
        assertThat(i.aciertoTop3Semantico()).as("acierto@3 semántico").isGreaterThanOrEqualTo(0.8);
        assertThat(i.aciertoTop3Semantico()).as("los embeddings superan a las palabras").isGreaterThan(i.aciertoTop3Palabras());
    }
}
