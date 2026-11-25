package co.styleme.infraestructura.adaptador.salida.persistencia.repositorio;

import co.styleme.infraestructura.adaptador.salida.persistencia.entidad.ProductoEntidad;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductoJpaRepositorio extends JpaRepository<ProductoEntidad, Long>, JpaSpecificationExecutor<ProductoEntidad> {

    Optional<ProductoEntidad> findByCodigo(String codigo);
}
