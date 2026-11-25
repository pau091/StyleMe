package co.styleme.aplicacion.puerto.entrada.ia;

import co.styleme.dominio.modelo.ia.FiltroBusqueda;
import co.styleme.dominio.modelo.ia.ResultadoBusqueda;
import java.util.List;

/**
 * Puerto de entrada: búsqueda SEMÁNTICA (por significado, no por palabras exactas).
 * "algo para que no me brille la cara" encuentra productos para piel grasa aunque no digan "brillo".
 */
public interface BuscarSemanticoCasoUso {

    List<ResultadoBusqueda> buscar(String consulta, FiltroBusqueda filtro);
}
