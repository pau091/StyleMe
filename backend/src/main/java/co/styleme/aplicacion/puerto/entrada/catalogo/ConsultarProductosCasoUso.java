package co.styleme.aplicacion.puerto.entrada.catalogo;

import co.styleme.dominio.modelo.catalogo.FiltroProductos;
import co.styleme.dominio.modelo.catalogo.Producto;
import java.util.List;

/** Puerto de entrada: lo que StyleMe permite hacer con los productos. */
public interface ConsultarProductosCasoUso {

    /** Productos activos que cumplen el filtro (categoría, tienda, presupuesto, texto). */
    List<Producto> buscar(FiltroProductos filtro);

    /** Un producto por su código. Lanza ProductoNoEncontradoException si no existe. */
    Producto obtener(String codigo);

    /** Productos de una tienda. Lanza TiendaNoEncontradaException si la tienda no existe. */
    List<Producto> listarDeTienda(String slugTienda);
}
