package co.styleme.infraestructura.configuracion;

import co.styleme.aplicacion.casodeuso.catalogo.ConsultarCategoriasServicio;
import co.styleme.aplicacion.casodeuso.catalogo.ConsultarProductosServicio;
import co.styleme.aplicacion.casodeuso.catalogo.ConsultarTiendasServicio;
import co.styleme.aplicacion.casodeuso.ia.BuscarSemanticoServicio;
import co.styleme.aplicacion.casodeuso.ia.ChatearConLiaServicio;
import co.styleme.aplicacion.casodeuso.ia.IndexarConocimientoServicio;
import co.styleme.aplicacion.puerto.entrada.catalogo.ConsultarCategoriasCasoUso;
import co.styleme.aplicacion.puerto.entrada.catalogo.ConsultarProductosCasoUso;
import co.styleme.aplicacion.puerto.entrada.catalogo.ConsultarTiendasCasoUso;
import co.styleme.aplicacion.puerto.entrada.ia.BuscarSemanticoCasoUso;
import co.styleme.aplicacion.puerto.entrada.ia.ChatearConLiaCasoUso;
import co.styleme.aplicacion.puerto.entrada.ia.IndexarConocimientoCasoUso;
import co.styleme.aplicacion.puerto.salida.ia.AlmacenVectorialPuerto;
import co.styleme.aplicacion.puerto.salida.ia.EmbeddingPuerto;
import co.styleme.aplicacion.puerto.salida.ia.GuiaRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.ia.ModeloLenguajePuerto;
import co.styleme.aplicacion.puerto.salida.ia.PlantillaInstruccionesPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.ConversacionRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.CategoriaRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.ProductoRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.TiendaRepositorioPuerto;
import co.styleme.dominio.servicio.FragmentadorConocimiento;
import co.styleme.dominio.servicio.LectorPresupuesto;
import co.styleme.dominio.servicio.PlanBLia;
import co.styleme.dominio.servicio.ValidadorRecomendaciones;
import java.time.Clock;
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
}
