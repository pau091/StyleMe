package co.styleme.infraestructura.configuracion;

import co.styleme.aplicacion.casodeuso.analitica.ConsultarIndicadoresServicio;
import co.styleme.aplicacion.casodeuso.analitica.RegistrarEventosServicio;
import co.styleme.aplicacion.casodeuso.comunidad.GestionarFavoritosServicio;
import co.styleme.aplicacion.casodeuso.comunidad.GestionarResenasServicio;
import co.styleme.aplicacion.casodeuso.comunidad.ModerarResenasServicio;
import co.styleme.aplicacion.puerto.entrada.analitica.ConsultarIndicadoresCasoUso;
import co.styleme.aplicacion.puerto.entrada.analitica.RegistrarEventosCasoUso;
import co.styleme.aplicacion.puerto.entrada.comunidad.GestionarFavoritosCasoUso;
import co.styleme.aplicacion.puerto.entrada.comunidad.GestionarResenasCasoUso;
import co.styleme.aplicacion.puerto.entrada.comunidad.ModerarResenasCasoUso;
import co.styleme.aplicacion.puerto.salida.notificacion.NotificadorPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.EventoRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.FavoritoRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.IndicadorRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.ResenaRepositorioPuerto;
import co.styleme.aplicacion.casodeuso.catalogo.ConsultarCategoriasServicio;
import co.styleme.aplicacion.casodeuso.probador.ProbarPrendaServicio;
import co.styleme.aplicacion.puerto.entrada.probador.ProbarPrendaCasoUso;
import co.styleme.aplicacion.puerto.salida.ia.DescargadorImagenPuerto;
import co.styleme.aplicacion.puerto.salida.ia.GeneradorImagenPuerto;
import co.styleme.dominio.servicio.InstruccionesProbador;
import co.styleme.aplicacion.casodeuso.catalogo.ConsultarProductosServicio;
import co.styleme.aplicacion.casodeuso.catalogo.ConsultarTiendasServicio;
import co.styleme.aplicacion.casodeuso.ia.BuscarSemanticoServicio;
import co.styleme.aplicacion.casodeuso.ia.ChatearConLiaServicio;
import co.styleme.aplicacion.casodeuso.ia.EvaluarIaServicio;
import co.styleme.aplicacion.casodeuso.ia.IndexarConocimientoServicio;
import co.styleme.aplicacion.puerto.entrada.catalogo.ConsultarCategoriasCasoUso;
import co.styleme.aplicacion.puerto.entrada.catalogo.ConsultarProductosCasoUso;
import co.styleme.aplicacion.puerto.entrada.catalogo.ConsultarTiendasCasoUso;
import co.styleme.aplicacion.puerto.entrada.ia.BuscarSemanticoCasoUso;
import co.styleme.aplicacion.puerto.entrada.ia.ChatearConLiaCasoUso;
import co.styleme.aplicacion.puerto.entrada.ia.EvaluarIaCasoUso;
import co.styleme.aplicacion.puerto.entrada.ia.IndexarConocimientoCasoUso;
import co.styleme.aplicacion.puerto.salida.ia.AlmacenVectorialPuerto;
import co.styleme.aplicacion.puerto.salida.ia.ConjuntoEvaluacionPuerto;
import co.styleme.aplicacion.puerto.salida.ia.EmbeddingPuerto;
import co.styleme.aplicacion.puerto.salida.ia.GuiaRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.ia.ModeloLenguajePuerto;
import co.styleme.aplicacion.puerto.salida.ia.PlantillaInstruccionesPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.ConversacionRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.EvaluacionRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.CategoriaRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.ProductoRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.TiendaRepositorioPuerto;
import co.styleme.dominio.servicio.CalificadorEvaluacion;
import co.styleme.dominio.servicio.FragmentadorConocimiento;
import co.styleme.dominio.servicio.LectorPresupuesto;
import co.styleme.dominio.servicio.PlanBLia;
import co.styleme.dominio.servicio.ValidadorRecomendaciones;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Aquí se "enchufan" los casos de uso con sus adaptadores.
 *
 * Los casos de uso (capa aplicacion) no tienen anotaciones de Spring (regla R3).
 * Spring los conoce porque esta clase los crea con "new" y les pasa los puertos de salida;
 * Spring encuentra solo qué adaptador implementa cada puerto (TiendaPersistenciaAdaptador...).
 */
@Configuration
@EnableScheduling // activa las tareas programadas (reindexación nocturna)
public class ConfiguracionBeans {

    @Bean
    ConsultarTiendasCasoUso consultarTiendas(TiendaRepositorioPuerto tiendas) {
        return new ConsultarTiendasServicio(tiendas);
    }

    @Bean
    ConsultarProductosCasoUso consultarProductos(ProductoRepositorioPuerto productos, TiendaRepositorioPuerto tiendas) {
        return new ConsultarProductosServicio(productos, tiendas);
    }

    @Bean
    ConsultarCategoriasCasoUso consultarCategorias(CategoriaRepositorioPuerto categorias) {
        return new ConsultarCategoriasServicio(categorias);
    }

    // ---------- Inteligencia artificial ----------

    /** Servicio del dominio (Java puro): también se registra aquí, sin anotaciones. */
    @Bean
    FragmentadorConocimiento fragmentadorConocimiento() {
        return new FragmentadorConocimiento();
    }

    @Bean
    IndexarConocimientoCasoUso indexarConocimiento(TiendaRepositorioPuerto tiendas, ProductoRepositorioPuerto productos,
            GuiaRepositorioPuerto guias, EmbeddingPuerto embeddings, AlmacenVectorialPuerto almacen,
            FragmentadorConocimiento fragmentador) {
        return new IndexarConocimientoServicio(tiendas, productos, guias, embeddings, almacen, fragmentador);
    }

    @Bean
    BuscarSemanticoCasoUso buscarSemantico(EmbeddingPuerto embeddings, AlmacenVectorialPuerto almacen) {
        return new BuscarSemanticoServicio(embeddings, almacen);
    }

    // ---------- Lía (RAG) ----------

    @Bean
    ValidadorRecomendaciones validadorRecomendaciones() {
        return new ValidadorRecomendaciones();
    }

    @Bean
    LectorPresupuesto lectorPresupuesto() {
        return new LectorPresupuesto();
    }

    @Bean
    PlanBLia planBLia() {
        return new PlanBLia();
    }

    /** Reloj del sistema (en las pruebas se puede reemplazar por uno fijo). */
    @Bean
    Clock reloj() {
        return Clock.systemDefaultZone();
    }

    @Bean
    ChatearConLiaCasoUso chatearConLia(EmbeddingPuerto embeddings, AlmacenVectorialPuerto almacen, ModeloLenguajePuerto modelo,
            PlantillaInstruccionesPuerto plantilla, TiendaRepositorioPuerto tiendas, ProductoRepositorioPuerto productos,
            ConversacionRepositorioPuerto conversaciones, ValidadorRecomendaciones validador, LectorPresupuesto lectorPresupuesto,
            PlanBLia planB, Clock reloj) {
        return new ChatearConLiaServicio(embeddings, almacen, modelo, plantilla, tiendas, productos, conversaciones,
                validador, lectorPresupuesto, planB, reloj);
    }

    // ---------- Evaluación de la IA ----------

    @Bean
    CalificadorEvaluacion calificadorEvaluacion() {
        return new CalificadorEvaluacion();
    }

    @Bean
    EvaluarIaCasoUso evaluarIa(ConjuntoEvaluacionPuerto conjunto, EmbeddingPuerto embeddings, AlmacenVectorialPuerto almacen,
            ProductoRepositorioPuerto productos, ChatearConLiaCasoUso lia, EvaluacionRepositorioPuerto evaluaciones,
            CalificadorEvaluacion calificador, Clock reloj,
            @Value("${styleme.ia.evaluacion-pausa-ms:4500}") long pausaEntrePreguntasMs) {
        return new EvaluarIaServicio(conjunto, embeddings, almacen, productos, lia, evaluaciones, calificador, reloj, pausaEntrePreguntasMs);
    }

    // ---------- Comunidad (reseñas y favoritos) ----------

    @Bean
    GestionarResenasCasoUso gestionarResenas(ResenaRepositorioPuerto resenas, TiendaRepositorioPuerto tiendas,
            ProductoRepositorioPuerto productos, NotificadorPuerto notificador) {
        return new GestionarResenasServicio(resenas, tiendas, productos, notificador);
    }

    @Bean
    ModerarResenasCasoUso moderarResenas(ResenaRepositorioPuerto resenas) {
        return new ModerarResenasServicio(resenas);
    }

    @Bean
    GestionarFavoritosCasoUso gestionarFavoritos(FavoritoRepositorioPuerto favoritos, TiendaRepositorioPuerto tiendas,
            ProductoRepositorioPuerto productos) {
        return new GestionarFavoritosServicio(favoritos, tiendas, productos);
    }

    // ---------- Probador con IA (P-017) ----------

    @Bean
    InstruccionesProbador instruccionesProbador() {
        return new InstruccionesProbador();
    }

    @Bean
    ProbarPrendaCasoUso probarPrenda(ProductoRepositorioPuerto productos, GeneradorImagenPuerto generador,
            DescargadorImagenPuerto descargador, InstruccionesProbador instrucciones) {
        return new ProbarPrendaServicio(productos, generador, descargador, instrucciones);
    }

    // ---------- Analítica (eventos e indicadores) ----------

    @Bean
    RegistrarEventosCasoUso registrarEventos(EventoRepositorioPuerto eventos) {
        return new RegistrarEventosServicio(eventos);
    }

    @Bean
    ConsultarIndicadoresCasoUso consultarIndicadores(IndicadorRepositorioPuerto indicadores) {
        return new ConsultarIndicadoresServicio(indicadores);
    }
}
