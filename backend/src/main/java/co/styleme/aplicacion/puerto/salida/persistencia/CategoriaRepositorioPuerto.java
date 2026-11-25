package co.styleme.aplicacion.puerto.salida.persistencia;

import co.styleme.dominio.modelo.catalogo.Categoria;
import java.util.List;

/** Puerto de salida: lectura de categorías. */
public interface CategoriaRepositorioPuerto {

    List<Categoria> listarTodas();
}
