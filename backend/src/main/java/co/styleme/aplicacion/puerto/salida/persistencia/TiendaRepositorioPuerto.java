package co.styleme.aplicacion.puerto.salida.persistencia;

import co.styleme.dominio.modelo.catalogo.Tienda;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: lo que la aplicación necesita para guardar y leer tiendas.
 * No dice CÓMO (JPA, SQL, archivos...): eso lo decide el adaptador en infraestructura.
 */
public interface TiendaRepositorioPuerto {

    List<Tienda> buscarActivas(String categoria, String zona);

    Optional<Tienda> buscarPorSlug(String slug);

    boolean existe(String slug);
}
