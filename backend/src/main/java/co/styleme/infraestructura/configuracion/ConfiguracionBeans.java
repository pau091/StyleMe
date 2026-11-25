package co.styleme.infraestructura.configuracion;

import co.styleme.aplicacion.casodeuso.catalogo.ConsultarCategoriasServicio;
import co.styleme.aplicacion.casodeuso.catalogo.ConsultarProductosServicio;
import co.styleme.aplicacion.casodeuso.catalogo.ConsultarTiendasServicio;
import co.styleme.aplicacion.puerto.entrada.catalogo.ConsultarCategoriasCasoUso;
import co.styleme.aplicacion.puerto.entrada.catalogo.ConsultarProductosCasoUso;
import co.styleme.aplicacion.puerto.entrada.catalogo.ConsultarTiendasCasoUso;
import co.styleme.aplicacion.puerto.salida.persistencia.CategoriaRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.ProductoRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.TiendaRepositorioPuerto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Aquí se "enchufan" los casos de uso con sus adaptadores.
 *
 * Los casos de uso (capa aplicacion) no tienen anotaciones de Spring (regla R3).
 * Spring los conoce porque esta clase los crea con "new" y les pasa los puertos de salida;
 * Spring encuentra solo qué adaptador implementa cada puerto (TiendaPersistenciaAdaptador...).
 */
@Configuration
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
}
