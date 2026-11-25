package co.styleme.aplicacion.puerto.salida.persistencia;

import co.styleme.dominio.modelo.catalogo.FiltroProductos;
import co.styleme.dominio.modelo.catalogo.Producto;
import java.util.List;
import java.util.Optional;

/** Puerto de salida: lo que la aplicación necesita para leer productos. */
public interface ProductoRepositorioPuerto {

    List<Producto> buscar(FiltroProductos filtro);

    Optional<Producto> buscarPorCodigo(String codigo);
}
