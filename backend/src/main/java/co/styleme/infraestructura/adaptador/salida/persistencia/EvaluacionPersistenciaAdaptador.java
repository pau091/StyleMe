package co.styleme.infraestructura.adaptador.salida.persistencia;

import co.styleme.aplicacion.puerto.salida.persistencia.EvaluacionRepositorioPuerto;
import co.styleme.dominio.modelo.evaluacion.ResultadoCaso;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/**
 * Adaptador de salida: guarda cada resultado de la evaluación en evaluacion_ia.
 * "esperado" y "obtenido" se guardan como JSONB: son la evidencia exacta de cada prueba.
 */
@Component
@Transactional
public class EvaluacionPersistenciaAdaptador implements EvaluacionRepositorioPuerto {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final JdbcTemplate jdbc;

    public EvaluacionPersistenciaAdaptador(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void guardar(String ejecucion, ResultadoCaso r) {
        Map<String, Object> esperado = new LinkedHashMap<>();
        esperado.put("id", r.caso().id());
        esperado.put("relevantes", r.caso().relevantes());
        esperado.put("presupuesto", r.caso().presupuesto());

        Map<String, Object> obtenido = new LinkedHashMap<>();
        obtenido.put("recuperadosSemantica", r.recuperados());
        obtenido.put("recuperadosPalabras", r.recuperadosPalabras());
        obtenido.put("recomendados", r.recomendados());
        obtenido.put("aciertoTop3", r.aciertoTop3());
        obtenido.put("aciertoPalabras", r.aciertoPalabras());
        obtenido.put("respetaPresupuesto", r.respetaPresupuesto());
        obtenido.put("emocion", r.emocion());
        obtenido.put("modo", r.modo() == null ? null : r.modo().name());
        obtenido.put("texto", r.texto());

        jdbc.update("""
                INSERT INTO evaluacion_ia (ejecucion, pregunta, tipo_pregunta, esperado, obtenido,
                                           precision_k, recall_k, sin_alucinacion, aprobada, latencia_ms)
                VALUES (?, ?, ?, CAST(? AS jsonb), CAST(? AS jsonb), ?, ?, ?, ?, ?)
                """,
                ejecucion, r.caso().pregunta(), r.caso().tipo().codigo(),
                JSON.writeValueAsString(esperado), JSON.writeValueAsString(obtenido),
                redondear(r.precisionK()), redondear(r.recallK()), r.sinAlucinacion(), r.aprobada(), r.latenciaMs());
    }

    private static BigDecimal redondear(Double valor) {
        return valor == null ? null : BigDecimal.valueOf(valor).setScale(3, RoundingMode.HALF_UP);
    }
}
