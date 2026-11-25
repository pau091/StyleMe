package co.styleme.aplicacion.casodeuso.ia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.styleme.aplicacion.puerto.salida.ia.ModeloLenguajePuerto;
import co.styleme.aplicacion.puerto.salida.ia.PlantillaInstruccionesPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.ConversacionRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.ProductoRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.TiendaRepositorioPuerto;
import co.styleme.dominio.excepcion.ServicioIaNoDisponibleException;
import co.styleme.dominio.modelo.catalogo.Categoria;
import co.styleme.dominio.modelo.catalogo.Producto;
import co.styleme.dominio.modelo.catalogo.Tienda;
import co.styleme.dominio.modelo.ia.FragmentoConocimiento;
import co.styleme.dominio.modelo.lia.AccionLia;
import co.styleme.dominio.modelo.lia.ConversacionLia;
import co.styleme.dominio.modelo.lia.DatosModelo;
import co.styleme.dominio.modelo.lia.MensajeConversacion;
import co.styleme.dominio.modelo.lia.PreguntaLia;
import co.styleme.dominio.modelo.lia.Prompt;
import co.styleme.dominio.modelo.lia.RespuestaLia;
import co.styleme.dominio.modelo.lia.RespuestaModelo;
import co.styleme.dominio.servicio.LectorPresupuesto;
import co.styleme.dominio.servicio.PlanBLia;
import co.styleme.dominio.servicio.ValidadorRecomendaciones;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * El flujo RAG completo con puertos FALSOS: sin Gemini, sin pgvector, sin base de datos.
 * El "modelo de lenguaje" falso responde lo que la prueba quiere, para comprobar cada paso.
 */
class ChatearConLiaServicioTest {

    private final Categoria skincare = new Categoria((short) 3, "skincare", "Skincare");
    private final Tienda soBeauty = new Tienda(9L, "tienda-9", "So Beauty", null, skincare, null, null, null, null, null, null, false, true);

    private Falsos.EmbeddingFalso embeddings;
    private Falsos.AlmacenFalso almacen;
    private ModeloLenguajePuerto modelo;
    private ProductoRepositorioPuerto productos;
    private ConversacionRepositorioPuerto conversaciones;
    private ChatearConLiaServicio servicio;
    private final AtomicReference<Prompt> promptRecibido = new AtomicReference<>();

    @BeforeEach
    void preparar() {
        embeddings = new Falsos.EmbeddingFalso();
        almacen = new Falsos.AlmacenFalso();
        modelo = mock(ModeloLenguajePuerto.class);
        productos = mock(ProductoRepositorioPuerto.class);
        conversaciones = mock(ConversacionRepositorioPuerto.class);
        TiendaRepositorioPuerto tiendas = mock(TiendaRepositorioPuerto.class);
        when(tiendas.buscarActivas(null, null)).thenReturn(List.of(soBeauty));
        when(conversaciones.buscarPorSesion(any())).thenReturn(Optional.empty());
        when(conversaciones.guardar(any())).thenAnswer(i -> {
            ConversacionLia c = i.getArgument(0);
            return new ConversacionLia(1L, c.sesionId(), c.tema(), c.categoria(), c.sentimientoFinal(), c.cantidadMensajes(),
                    c.modo(), c.similitudPromedio(), c.latenciaPromedioMs(), c.tokensEntrada(), c.tokensSalida(), c.inicio(), c.fin());
        });
        PlantillaInstruccionesPuerto plantilla = () -> "Eres Lía.\nTIENDAS:\n{{TIENDAS}}\nCONTEXTO:\n{{CONTEXTO}}";

        servicio = new ChatearConLiaServicio(embeddings, almacen, modelo, plantilla, tiendas, productos, conversaciones,
                new ValidadorRecomendaciones(), new LectorPresupuesto(), new PlanBLia(), Clock.systemDefaultZone());

        // Base vectorial falsa con un producto relevante
        var serum = FragmentoConocimiento.deProducto(37, "Sérum Control Oleosidad",
                "Producto: Sérum Control Oleosidad. Tipo de piel: grasa. Controla el brillo de la piel grasa.");
        almacen.guardar(serum, Falsos.EmbeddingFalso.vectorizar(serum.texto()), "modelo-falso");
    }

    /** El modelo falso "habla" en dos pedazos y propone p037 (real) y p999 (inventado). */
    private void modeloResponde(String intencion) {
        when(modelo.generar(any(), any())).thenAnswer(i -> {
            promptRecibido.set(i.getArgument(0));
            Consumer<String> alTexto = i.getArgument(1);
            alTexto.accept("Te recomiendo el ");
            alTexto.accept("Sérum Control Oleosidad.");
            return new RespuestaModelo("Te recomiendo el Sérum Control Oleosidad.",
                    new DatosModelo("positivo", intencion, "felicidad", 0.8, List.of("p37", "p999"),
                            List.of(new AccionLia("ver_tienda", "tienda-9", null, null)), List.of("¿Y un protector?")),
                    120, 40);
        });
    }

    private PreguntaLia pregunta(String mensaje) {
        return new PreguntaLia(mensaje, List.of(), "sesion-prueba");
    }

    @Test
    @DisplayName("AUMENTAR: el prompt lleva el contexto recuperado de la base vectorial y las tiendas")
    void promptConContexto() {
        modeloResponde("catalogo");
        servicio.conversar(pregunta("algo para piel grasa que controle el brillo"), t -> { });
        String instrucciones = promptRecibido.get().instrucciones();
        assertThat(instrucciones).contains("[producto p37 · tienda", "Sérum Control Oleosidad", "So Beauty");
        assertThat(instrucciones).doesNotContain("{{CONTEXTO}}", "{{TIENDAS}}");
    }

    @Test
    @DisplayName("GENERAR: el texto llega por partes a quien escucha (streaming)")
    void streaming() {
        modeloResponde("catalogo");
        List<String> pedazos = new ArrayList<>();
        servicio.conversar(pregunta("algo para piel grasa"), pedazos::add);
        assertThat(pedazos).containsExactly("Te recomiendo el ", "Sérum Control Oleosidad.");
    }

    @Test
    @DisplayName("VALIDAR: el producto inventado (p999) se descarta; el del contexto (p37) se conserva")
    void antiAlucinacion() {
        modeloResponde("catalogo");
        RespuestaLia r = servicio.conversar(pregunta("algo para piel grasa que controle el brillo"), t -> { });
        assertThat(r.productos()).containsExactly("p37");
        assertThat(r.acciones()).containsExactly(new AccionLia("ver_tienda", "tienda-9", null, null));
        assertThat(r.modo()).isEqualTo(RespuestaLia.Modo.IA);
    }

    @Test
    @DisplayName("PLAN B: si la IA no está disponible, responde con reglas y productos reales")
    void planB() {
        when(modelo.generar(any(), any())).thenThrow(new ServicioIaNoDisponibleException("sin cupo"));
        var p037 = new Producto(37L, "p037", soBeauty.comoReferencia(), skincare, "Sérum", null, 35_000,
                null, null, null, null, null, false, true, null, null);
        when(productos.buscar(any())).thenReturn(List.of(p037));
        List<String> pedazos = new ArrayList<>();

        RespuestaLia r = servicio.conversar(pregunta("busco un sérum para la piel"), pedazos::add);

        assertThat(r.modo()).isEqualTo(RespuestaLia.Modo.PLAN_B);
        assertThat(r.productos()).containsExactly("p037");
        assertThat(String.join("", pedazos)).contains("modo básico");
    }

    @Test
    @DisplayName("REGISTRAR: guarda el resumen de la conversación y suma los productos recomendados")
    void registraConversacion() {
        modeloResponde("catalogo");
        servicio.conversar(pregunta("algo para piel grasa que controle el brillo"), t -> { });
        verify(conversaciones).guardar(any());
        verify(conversaciones).sumarRecomendaciones(eq(1L), eq(List.of("p37")));
    }

    @Test
    @DisplayName("Sin identificador de sesión no guarda nada (conversación anónima total)")
    void sinSesion() {
        modeloResponde("social");
        servicio.conversar(new PreguntaLia("hola", List.of(new MensajeConversacion(MensajeConversacion.Rol.LIA, "Hola")), null), t -> { });
        verify(conversaciones, never()).guardar(any());
    }

    @Test
    @DisplayName("Si falla el registro, la persona igual recibe su respuesta")
    void registroFallaSinRomper() {
        modeloResponde("catalogo");
        org.mockito.Mockito.doThrow(new IllegalStateException("base caída")).when(conversaciones).guardar(any());
        RespuestaLia r = servicio.conversar(pregunta("algo para piel grasa"), t -> { });
        assertThat(r.texto()).contains("Sérum");
    }
}
