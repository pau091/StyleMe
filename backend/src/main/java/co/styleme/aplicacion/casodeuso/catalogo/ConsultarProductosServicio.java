package co.styleme.aplicacion.casodeuso.catalogo;

import co.styleme.aplicacion.puerto.entrada.catalogo.ConsultarProductosCasoUso;
import co.styleme.aplicacion.puerto.salida.persistencia.ProductoRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.TiendaRepositorioPuerto;
import co.styleme.dominio.excepcion.ProductoNoEncontradoException;
import co.styleme.dominio.excepcion.TiendaNoEncontradaException;
import co.styleme.dominio.modelo.catalogo.FiltroProductos;
import co.styleme.dominio.modelo.catalogo.Producto;
import java.util.List;
import java.util.Objects;

/** Caso de uso: consultar productos del catálogo. */
public class ConsultarProductosServicio implements ConsultarProductosCasoUso {

    private final ProductoRepositorioPuerto productos;
    private final TiendaRepositorioPuerto tiendas;

    public ConsultarProductosServicio(ProductoRepositorioPuerto productos, TiendaRepositorioPuerto tiendas) {
        this.productos = Objects.requireNonNull(productos);
        this.tiendas = Objects.requireNonNull(tiendas);
    }

    @Override
    public List<Producto> buscar(FiltroProductos filtro) {
        return productos.buscar(filtro == null ? FiltroProductos.todos() : filtro);
    }

    @Override
    public Producto obtener(String codigo) {
        return productos.buscarPorCodigo(codigo)
                .orElseThrow(() -> new ProductoNoEncontradoException(codigo));
    }

    @Override
    public List<Producto> listarDeTienda(String slugTienda) {
        // Regla: pedir los productos de una tienda que no existe es un error (404),
        // no una lista vacía (que significaría "la tienda existe pero no tiene productos").
        if (!tiendas.existe(slugTienda)) {
            throw new TiendaNoEncontradaException(slugTienda);
        }
        return productos.buscar(FiltroProductos.todos().deLaTienda(slugTienda));
    }
}
