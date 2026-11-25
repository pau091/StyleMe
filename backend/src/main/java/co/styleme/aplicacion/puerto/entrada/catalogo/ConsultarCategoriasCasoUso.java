package co.styleme.aplicacion.puerto.entrada.catalogo;

import co.styleme.dominio.modelo.catalogo.Categoria;
import java.util.List;

/** Puerto de entrada: las categorías del catálogo (para menús y filtros). */
public interface ConsultarCategoriasCasoUso {

    List<Categoria> listar();
}
