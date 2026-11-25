package co.styleme.infraestructura.adaptador.entrada.rest;

import co.styleme.aplicacion.puerto.entrada.ia.EvaluarIaCasoUso;
import co.styleme.dominio.modelo.evaluacion.InformeEvaluacion;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.EvaluacionRespuesta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST: ejecutar la evaluación de calidad de la IA.
 * ⚠️ Gasta unas 50 llamadas a Gemini (tarda 1–3 minutos). Protegido con la clave de administración.
 */
@RestController
@SecurityRequirement(name = "claveAdmin") // pide la clave de administración (FiltroClaveAdmin)
@RequestMapping("/api/admin/evaluacion")
@Tag(name = "Administración de la IA")
public class AdminEvaluacionControlador {

    private final EvaluarIaCasoUso evaluarIa;

    public AdminEvaluacionControlador(EvaluarIaCasoUso evaluarIa) {
        this.evaluarIa = evaluarIa;
    }

    @PostMapping
    @Operation(summary = "Evaluar la calidad de la IA",
            description = """
                    Ejecuta las 30 preguntas de evaluación: precisión@k y recall@k de la búsqueda semántica (comparada con la
                    búsqueda por palabras), respuestas sin productos inventados, presupuesto, salud y temas ajenos.
                    Los resultados quedan guardados en la tabla evaluacion_ia.""")
    public EvaluacionRespuesta evaluar() {
        InformeEvaluacion i = evaluarIa.evaluar();
        return new EvaluacionRespuesta(
                i.ejecucion(),
                i.total(),
                porcentaje(i.porcentajeAprobadas()),
                new EvaluacionRespuesta.Recuperacion(porcentaje(i.aciertoTop3Semantico()), porcentaje(i.aciertoTop3Palabras()),
                        porcentaje(i.precisionMedia()), porcentaje(i.recallMedio())),
                new EvaluacionRespuesta.Respuestas(porcentaje(i.porcentajeSinAlucinacion()), porcentaje(i.porcentajeRespetaPresupuesto()),
                        porcentaje(i.porcentajeSalud()), porcentaje(i.porcentajeSinComprasFueraDeTema()),
                        i.respuestasConPlanB(), Math.round(i.latenciaMediaMs())),
                i.resultados().stream().map(r -> new EvaluacionRespuesta.Caso(r.caso().id(), r.caso().tipo().codigo(),
                        r.caso().pregunta(), r.aprobada(), r.recomendados(), r.precisionK())).toList());
    }

    private static double porcentaje(double proporcion) {
        return Math.round(proporcion * 1000) / 10.0;
    }
}
