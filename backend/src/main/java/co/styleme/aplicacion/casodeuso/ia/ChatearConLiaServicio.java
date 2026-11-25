package co.styleme.aplicacion.casodeuso.ia;

import co.styleme.aplicacion.puerto.entrada.ia.ChatearConLiaCasoUso;
import co.styleme.aplicacion.puerto.salida.ia.AlmacenVectorialPuerto;
import co.styleme.aplicacion.puerto.salida.ia.EmbeddingPuerto;
import co.styleme.aplicacion.puerto.salida.ia.ModeloLenguajePuerto;
import co.styleme.aplicacion.puerto.salida.ia.PlantillaInstruccionesPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.ConversacionRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.ProductoRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.TiendaRepositorioPuerto;
import co.styleme.dominio.excepcion.ServicioIaNoDisponibleException;
import co.styleme.dominio.modelo.catalogo.FiltroProductos;
import co.styleme.dominio.modelo.catalogo.Producto;
import co.styleme.dominio.modelo.catalogo.Tienda;
import co.styleme.dominio.modelo.ia.FiltroBusqueda;
import co.styleme.dominio.modelo.ia.ResultadoBusqueda;
import co.styleme.dominio.modelo.lia.ConversacionLia;
import co.styleme.dominio.modelo.lia.PreguntaLia;
import co.styleme.dominio.modelo.lia.Prompt;
import co.styleme.dominio.modelo.lia.RespuestaLia;
import co.styleme.dominio.modelo.lia.RespuestaModelo;
import co.styleme.dominio.servicio.LectorPresupuesto;
import co.styleme.dominio.servicio.PlanBLia;
import co.styleme.dominio.servicio.ValidadorRecomendaciones;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Caso de uso: conversar con Lía usando RAG (Retrieval-Augmented Generation).
 *
 *   1. RECUPERAR  la pregunta se convierte en embedding y pgvector trae los fragmentos más parecidos
 *                 (con el presupuesto como filtro duro y un umbral de similitud).
 *   2. AUMENTAR   se arma el prompt: reglas de Lía + tiendas + CONTEXTO recuperado + historial corto.
 *   3. GENERAR    el modelo de lenguaje responde por partes (streaming).
 *   4. VALIDAR    ValidadorRecomendaciones descarta productos que no estaban en el contexto.
 *   5. REGISTRAR  se guarda el resumen de la conversación (sin el texto de la persona).
 *
 * Si la IA no está disponible, responde el PLAN B (reglas + catálogo real).
 */
public class ChatearConLiaServicio implements ChatearConLiaCasoUso {

    /** Cuántos fragmentos recuperar para el contexto (más = más tokens). */
    static final int FRAGMENTOS_CONTEXTO = 8;
    /** Por debajo de este valor un fragmento no aporta al contexto. */
    static final double SIMILITUD_MINIMA_CONTEXTO = 0.55;

    private static final System.Logger LOG = System.getLogger(ChatearConLiaServicio.class.getName());

    private final EmbeddingPuerto embeddings;
    private final AlmacenVectorialPuerto almacen;
    private final ModeloLenguajePuerto modelo;
    private final PlantillaInstruccionesPuerto plantilla;
    private final TiendaRepositorioPuerto tiendas;
    private final ProductoRepositorioPuerto productos;
    private final ConversacionRepositorioPuerto conversaciones;
    private final ValidadorRecomendaciones validador;
    private final LectorPresupuesto lectorPresupuesto;
    private final PlanBLia planB;
    private final Clock reloj;

    public ChatearConLiaServicio(EmbeddingPuerto embeddings, AlmacenVectorialPuerto almacen, ModeloLenguajePuerto modelo,
            PlantillaInstruccionesPuerto plantilla, TiendaRepositorioPuerto tiendas, ProductoRepositorioPuerto productos,
            ConversacionRepositorioPuerto conversaciones, ValidadorRecomendaciones validador,
            LectorPresupuesto lectorPresupuesto, PlanBLia planB, Clock reloj) {
        this.embeddings = Objects.requireNonNull(embeddings);
        this.almacen = Objects.requireNonNull(almacen);
        this.modelo = Objects.requireNonNull(modelo);
        this.plantilla = Objects.requireNonNull(plantilla);
        this.tiendas = Objects.requireNonNull(tiendas);
        this.productos = Objects.requireNonNull(productos);
        this.conversaciones = Objects.requireNonNull(conversaciones);
        this.validador = Objects.requireNonNull(validador);
        this.lectorPresupuesto = Objects.requireNonNull(lectorPresupuesto);
        this.planB = Objects.requireNonNull(planB);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Override
    public RespuestaLia conversar(PreguntaLia pregunta, Consumer<String> alRecibirTexto) {
        long inicio = reloj.millis();
        Optional<Integer> presupuesto = lectorPresupuesto.leer(pregunta.consultaParaBuscar());
        var emitido = new StringBuilder();
        Consumer<String> emitir = texto -> {
            if (texto != null && !texto.isEmpty()) {
                emitido.append(texto);
                alRecibirTexto.accept(texto);
            }
        };

        RespuestaLia respuesta;
        List<ResultadoBusqueda> contexto = List.of();
        int tokensEntrada = 0;
        int tokensSalida = 0;
        try {
            // 1. RECUPERAR
            var filtro = new FiltroBusqueda(null, presupuesto.orElse(null), null, FRAGMENTOS_CONTEXTO, SIMILITUD_MINIMA_CONTEXTO);
            contexto = almacen.buscarParecidos(embeddings.deConsulta(pregunta.consultaParaBuscar()), filtro);

            // 2. AUMENTAR
            List<Tienda> listaTiendas = tiendas.buscarActivas(null, null);
            Prompt prompt = new Prompt(armarInstrucciones(listaTiendas, contexto), pregunta.historia(), pregunta.mensaje());

            // 3. GENERAR (por partes)
            RespuestaModelo generada = modelo.generar(prompt, emitir);
            tokensEntrada = generada.tokensEntrada();
            tokensSalida = generada.tokensSalida();
            if (generada.textoVisible().isBlank()) {
                throw new ServicioIaNoDisponibleException("El modelo no devolvió texto");
            }

            // 4. VALIDAR (anti-alucinación)
            Set<String> candidatos = contexto.stream()
                    .map(ResultadoBusqueda::codigoProducto)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Set<String> slugs = listaTiendas.stream().map(Tienda::slug).collect(Collectors.toSet());
            respuesta = validador.revisar(generada.datos(), candidatos, slugs, pregunta.mensaje(), generada.textoVisible());
        } catch (ServicioIaNoDisponibleException e) {
            LOG.log(System.Logger.Level.WARNING, "Lía sin IA, se usa el plan B: " + e.getMessage());
            if (!emitido.isEmpty()) {
                // Alcanzó a hablar y se cortó: se conserva lo dicho, sin productos
                respuesta = new RespuestaLia(emitido.toString().trim(), "neutra", 0.5, "neutral", "catalogo",
                        List.of(), List.of(), List.of(), RespuestaLia.Modo.IA);
            } else {
                respuesta = responderConPlanB(pregunta, presupuesto);
                emitir.accept(respuesta.texto());
            }
        }

        // 5. REGISTRAR (si falla, la persona igual recibe su respuesta)
        registrar(pregunta, respuesta, contexto, (int) (reloj.millis() - inicio), tokensEntrada, tokensSalida);
        return respuesta;
    }

    private RespuestaLia responderConPlanB(PreguntaLia pregunta, Optional<Integer> presupuesto) {
        Optional<String> categoria = planB.detectarCategoria(pregunta.consultaParaBuscar());
        List<Producto> encontrados = categoria.isEmpty() ? List.of()
                : productos.buscar(new FiltroProductos(categoria.get(), null, presupuesto.orElse(null), null));
        return planB.responder(categoria, encontrados);
    }

    /** La parte "AUMENTAR": rellena la plantilla con las tiendas y el contexto recuperado. */
    String armarInstrucciones(List<Tienda> listaTiendas, List<ResultadoBusqueda> contexto) {
        String textoTiendas = listaTiendas.stream()
                .map(t -> String.join(" | ", t.slug(), t.nombre(), t.categoriaPrincipal().nombre(),
                        t.zona() == null ? "zona por confirmar" : t.zona().nombre(),
                        Objects.requireNonNullElse(t.horario(), "horario por confirmar")))
                .collect(Collectors.joining("\n"));
        String textoContexto = contexto.isEmpty()
                ? "(no se encontró información relacionada en el catálogo)"
                : contexto.stream().map(ChatearConLiaServicio::lineaContexto).collect(Collectors.joining("\n"));
        return plantilla.instruccionesLia()
                .replace(PlantillaInstruccionesPuerto.TIENDAS, textoTiendas)
                .replace(PlantillaInstruccionesPuerto.CONTEXTO, textoContexto);
    }

    private static String lineaContexto(ResultadoBusqueda r) {
        String etiqueta = switch (r.tipo()) {
            case PRODUCTO -> "[producto " + r.codigoProducto() + " · tienda " + r.slugTienda() + "]";
            case TIENDA -> "[tienda " + r.slugTienda() + "]";
            case GUIA -> "[guía]";
        };
        return etiqueta + " " + r.texto();
    }

    private void registrar(PreguntaLia pregunta, RespuestaLia respuesta, List<ResultadoBusqueda> contexto,
            int latenciaMs, int tokensEntrada, int tokensSalida) {
        if (pregunta.sesionId() == null) {
            return;
        }
        try {
            String categoria = respuesta.productos().stream().findFirst()
                    .flatMap(productos::buscarPorCodigo)
                    .map(p -> p.categoria().slug())
                    .orElse(null);
            double similitud = contexto.stream().mapToDouble(ResultadoBusqueda::similitud).average().orElse(0);
            var intercambio = new ConversacionLia.Intercambio(respuesta.intencion(), categoria, respuesta.sentimiento(),
                    respuesta.modo(), similitud, latenciaMs, tokensEntrada, tokensSalida);
            LocalDateTime ahora = LocalDateTime.now(reloj);
            ConversacionLia conversacion = conversaciones.buscarPorSesion(pregunta.sesionId())
                    .map(c -> c.con(intercambio, ahora))
                    .orElseGet(() -> ConversacionLia.nueva(pregunta.sesionId(), intercambio, ahora));
            ConversacionLia guardada = conversaciones.guardar(conversacion);
            if (!respuesta.productos().isEmpty()) {
                conversaciones.sumarRecomendaciones(guardada.id(), respuesta.productos());
            }
        } catch (RuntimeException e) {
            LOG.log(System.Logger.Level.WARNING, "No se pudo registrar la conversación con Lía: " + e.getMessage());
        }
    }
}
