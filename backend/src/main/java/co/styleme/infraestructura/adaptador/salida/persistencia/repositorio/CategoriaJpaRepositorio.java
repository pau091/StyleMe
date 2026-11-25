package co.styleme.infraestructura.adaptador.salida.persistencia.repositorio;

import co.styleme.infraestructura.adaptador.salida.persistencia.entidad.CategoriaEntidad;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data genera la implementación a partir del nombre de los métodos. */
public interface CategoriaJpaRepositorio extends JpaRepository<CategoriaEntidad, Short> {

    List<CategoriaEntidad> findAllByOrderByIdAsc();
}
