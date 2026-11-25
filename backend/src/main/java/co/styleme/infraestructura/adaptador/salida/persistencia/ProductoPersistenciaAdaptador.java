package co.styleme.infraestructura.adaptador.salida.persistencia;

import co.styleme.aplicacion.puerto.salida.persistencia.ProductoRepositorioPuerto;
import co.styleme.dominio.modelo.catalogo.FiltroProductos;
import co.styleme.dominio.modelo.catalogo.Producto;
import co.styleme.infraestructura.adaptador.salida.persistencia.mapeador.CatalogoPersistenciaMapeador;
import co.styleme.infraestructura.adaptador.salida.persistencia.repositorio.ProductoJpaRepositorio;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador de salida: implementa ProductoRepositorioPuerto con JPA + PostgreSQL. */
@Component
@Transactional(readOnly = true)
public class ProductoPersistenciaAdaptador implements ProductoRepositorioPuerto {

    private final ProductoJpaRepositorio repositorio;

    public ProductoPersistenciaAdaptador(ProductoJpaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public List<Producto> buscar(FiltroProductos filtro) {
        return repositorio.findAll(EspecificacionesCatalogo.productos(filtro), Sort.by("codigo"))
                .stream()
                .map(CatalogoPersistenciaMapeador::aDominio)
                .toList();
    }

    @Override
    public Optional<Producto> buscarPorCodigo(String codigo) {
        return repositorio.findByCodigo(codigo).map(CatalogoPersistenciaMapeador::aDominio);
    }
}
