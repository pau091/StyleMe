package co.styleme.infraestructura.adaptador.salida.persistencia;

import co.styleme.aplicacion.puerto.salida.persistencia.CategoriaRepositorioPuerto;
import co.styleme.dominio.modelo.catalogo.Categoria;
import co.styleme.infraestructura.adaptador.salida.persistencia.mapeador.CatalogoPersistenciaMapeador;
import co.styleme.infraestructura.adaptador.salida.persistencia.repositorio.CategoriaJpaRepositorio;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador de salida: implementa CategoriaRepositorioPuerto con JPA + PostgreSQL. */
@Component
@Transactional(readOnly = true)
public class CategoriaPersistenciaAdaptador implements CategoriaRepositorioPuerto {

    private final CategoriaJpaRepositorio repositorio;

    public CategoriaPersistenciaAdaptador(CategoriaJpaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public List<Categoria> listarTodas() {
        return repositorio.findAllByOrderByIdAsc().stream().map(CatalogoPersistenciaMapeador::aDominio).toList();
    }
}
