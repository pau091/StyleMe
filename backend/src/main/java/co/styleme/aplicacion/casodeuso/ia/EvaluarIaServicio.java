package co.styleme.aplicacion.casodeuso.ia;

import co.styleme.aplicacion.puerto.entrada.ia.ChatearConLiaCasoUso;
import co.styleme.aplicacion.puerto.entrada.ia.EvaluarIaCasoUso;
import co.styleme.aplicacion.puerto.salida.ia.AlmacenVectorialPuerto;
import co.styleme.aplicacion.puerto.salida.ia.ConjuntoEvaluacionPuerto;
import co.styleme.aplicacion.puerto.salida.ia.EmbeddingPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.EvaluacionRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.ProductoRepositorioPuerto;
import co.styleme.dominio.modelo.catalogo.FiltroProductos;
import co.styleme.dominio.modelo.catalogo.Producto;
import co.styleme.dominio.modelo.evaluacion.CasoEvaluacion;
import co.styleme.dominio.modelo.evaluacion.InformeEvaluacion;
import co.styleme.dominio.modelo.evaluacion.ResultadoCaso;
import co.styleme.dominio.modelo.evaluacion.TipoPregunta;
import co.styleme.dominio.modelo.ia.FiltroBusqueda;
import co.styleme.dominio.modelo.ia.ResultadoBusqueda;
import co.styleme.dominio.modelo.ia.TipoFuente;
import co.styleme.dominio.modelo.lia.PreguntaLia;
import co.styleme.dominio.modelo.lia.RespuestaLia;
import co.styleme.dominio.servicio.CalificadorEvaluacion;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Caso de uso: EVALUAR la calidad de la IA (criterio "calidad de respuestas, validación y pruebas de IA").
 *
 * Para cada pregunta del conjunto:
 *   1. Búsqueda SEMÁNTICA (embeddings + pgvector, top-5) y búsqueda por PALABRAS (top-3), para compararlas.
 *   2. Respuesta completa de Lía (RAG) sin guardar la conversación.
 *   3. Calificación con CalificadorEvaluacion (métricas y criterios por tipo de pregunta).
 *   4. Se guarda el resultado en evaluacion_ia con un mismo identificador de ejecución.
 */
public class EvaluarIaServicio implements EvaluarIaCasoUso {

    private static final int TOP_SEMANTICO = 5;
    private static final int TOP_PALABRAS = 3;

    private final ConjuntoEvaluacionPuerto conjunto;
    private final EmbeddingPuerto embeddings;
    private final AlmacenVectorialPuerto almacen;
    private final ProductoRepositorioPuerto productos;
    private final ChatearConLiaCasoUso lia;
    private final EvaluacionRepositorioPuerto evaluaciones;
    private final CalificadorEvaluacion calificador;
    private final Clock reloj;
    /** Pausa entre preguntas para no superar el límite de peticiones por minuto del plan gratuito de Gemini. */
    private final long pausaEntrePreguntasMs;

    public EvaluarIaServicio(ConjuntoEvaluacionPuerto conjunto, EmbeddingPuerto embeddings, AlmacenVectorialPuerto almacen,
            ProductoRepositorioPuerto productos, ChatearConLiaCasoUso lia, EvaluacionRepositorioPuerto evaluaciones,
            CalificadorEvaluacion calificador, Clock reloj, long pausaEntrePreguntasMs) {
        this.conjunto = Objects.requireNonNull(conjunto);
        this.embeddings = Objects.requireNonNull(embeddings);
        this.almacen = Objects.requireNonNull(almacen);
        this.productos = Objects.requireNonNull(productos);
        this.lia = Objects.requireNonNull(lia);
        this.evaluaciones = Objects.requireNonNull(evaluaciones);
        this.calificador = Objects.requireNonNull(calificador);
        this.reloj = Objects.requireNonNull(reloj);
        this.pausaEntrePreguntasMs = Math.max(0, pausaEntrePreguntasMs);
    }

    @Override
    public InformeEvaluacion evaluar() {
        String ejecucion = UUID.randomUUID().toString();
        // Catálogo REAL (sin productos de ejemplo): lo que Lía puede recomendar sin "alucinar"
        Map<String, Producto> catalogoReal = productos.buscar(FiltroProductos.todos()).stream()
                .filter(p -> !p.esEjemplo())
                .collect(Collectors.toMap(Producto::codigo, Function.identity()));

        List<ResultadoCaso> resultados = new ArrayList<>();
        for (CasoEvaluacion caso : conjunto.casos()) {
            if (!resultados.isEmpty()) {
                pausar();
            }
            List<String> semanticos = List.of();
            List<String> porPalabras = List.of();
            if (caso.tipo() == TipoPregunta.CATALOGO) {
                semanticos = buscarSemantico(caso);
                porPalabras = buscarPorPalabras(caso, catalogoReal);
            }
            long inicio = reloj.millis();
            // Sin sesión: las preguntas de prueba no se mezclan con las conversaciones reales del informe
            RespuestaLia respuesta = lia.conversar(new PreguntaLia(caso.pregunta(), List.of(), null), texto -> { });
            int latencia = (int) (reloj.millis() - inicio);

            ResultadoCaso resultado = calificador.calificar(caso, semanticos, porPalabras, respuesta, catalogoReal, latencia);
            evaluaciones.guardar(ejecucion, resultado);
            resultados.add(resultado);
        }
        return new InformeEvaluacion(ejecucion, resultados);
    }

    private void pausar() {
        if (pausaEntrePreguntasMs == 0) {
            return;
        }
        try {
            Thread.sleep(pausaEntrePreguntasMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private List<String> buscarSemantico(CasoEvaluacion caso) {
        var filtro = new FiltroBusqueda(null, caso.presupuesto(), TipoFuente.PRODUCTO, TOP_SEMANTICO, 0.0);
        return almacen.buscarParecidos(embeddings.deConsulta(caso.pregunta()), filtro).stream()
                .map(ResultadoBusqueda::codigoProducto)
                .filter(Objects::nonNull)
                .toList();
    }

    /** Búsqueda tradicional: el texto de la pregunta tal cual, contra nombre y marca (como un buscador básico). */
    private List<String> buscarPorPalabras(CasoEvaluacion caso, Map<String, Producto> catalogoReal) {
        return productos.buscar(new FiltroProductos(null, null, caso.presupuesto(), caso.pregunta())).stream()
                .map(Producto::codigo)
                .filter(catalogoReal::containsKey)
                .limit(TOP_PALABRAS)
                .toList();
    }
}
