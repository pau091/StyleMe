package co.styleme.infraestructura.adaptador.salida.persistencia.repositorio;

import co.styleme.infraestructura.adaptador.salida.persistencia.entidad.TiendaEntidad;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** JpaSpecificationExecutor permite armar filtros opcionales (ver EspecificacionesCatalogo). */
public interface TiendaJpaRepositorio extends JpaRepository<TiendaEntidad, Long>, JpaSpecificationExecutor<TiendaEntidad> {

    Optional<TiendaEntidad> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
