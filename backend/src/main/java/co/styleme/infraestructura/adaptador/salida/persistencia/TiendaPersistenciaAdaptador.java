package co.styleme.infraestructura.adaptador.salida.persistencia;

import co.styleme.aplicacion.puerto.salida.persistencia.TiendaRepositorioPuerto;
import co.styleme.dominio.modelo.catalogo.Tienda;
import co.styleme.infraestructura.adaptador.salida.persistencia.mapeador.CatalogoPersistenciaMapeador;
import co.styleme.infraestructura.adaptador.salida.persistencia.repositorio.TiendaJpaRepositorio;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de salida: implementa el puerto TiendaRepositorioPuerto con JPA + PostgreSQL.
 * @Transactional vive aquí, en infraestructura (regla R10).
 */
@Component
@Transactional(readOnly = true)
public class TiendaPersistenciaAdaptador implements TiendaRepositorioPuerto {

    private final TiendaJpaRepositorio repositorio;

    public TiendaPersistenciaAdaptador(TiendaJpaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public List<Tienda> buscarActivas(String categoria, String zona) {
        return repositorio.findAll(EspecificacionesCatalogo.tiendasActivas(categoria, zona), Sort.by("nombre"))
                .stream()
                .map(CatalogoPersistenciaMapeador::aDominio)
                .toList();
    }

    @Override
    public Optional<Tienda> buscarPorSlug(String slug) {
        return repositorio.findBySlug(slug).map(CatalogoPersistenciaMapeador::aDominio);
    }

    @Override
    public boolean existe(String slug) {
        return repositorio.existsBySlug(slug);
    }
}
