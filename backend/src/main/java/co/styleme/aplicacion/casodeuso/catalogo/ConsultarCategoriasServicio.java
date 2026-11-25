package co.styleme.aplicacion.casodeuso.catalogo;

import co.styleme.aplicacion.puerto.entrada.catalogo.ConsultarCategoriasCasoUso;
import co.styleme.aplicacion.puerto.salida.persistencia.CategoriaRepositorioPuerto;
import co.styleme.dominio.modelo.catalogo.Categoria;
import java.util.List;
import java.util.Objects;

/** Caso de uso: listar las categorías del catálogo. */
public class ConsultarCategoriasServicio implements ConsultarCategoriasCasoUso {

    private final CategoriaRepositorioPuerto categorias;

    public ConsultarCategoriasServicio(CategoriaRepositorioPuerto categorias) {
        this.categorias = Objects.requireNonNull(categorias);
    }

    @Override
    public List<Categoria> listar() {
        return categorias.listarTodas();
    }
}
