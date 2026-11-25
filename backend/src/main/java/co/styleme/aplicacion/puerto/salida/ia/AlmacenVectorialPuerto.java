package co.styleme.aplicacion.puerto.salida.ia;

import co.styleme.dominio.modelo.ia.Embedding;
import co.styleme.dominio.modelo.ia.FiltroBusqueda;
import co.styleme.dominio.modelo.ia.FragmentoConocimiento;
import co.styleme.dominio.modelo.ia.ResultadoBusqueda;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Puerto de salida: guardar fragmentos con su embedding y buscar los más parecidos. */
public interface AlmacenVectorialPuerto {

    /**
     * Hash del texto de cada fragmento guardado con ESE modelo, por clave.
     * Los fragmentos guardados con otro modelo no aparecen: hay que recalcularlos.
     */
    Map<String, String> hashesPorClave(String modelo);

    /** Guarda el fragmento con su embedding; si ya existe uno con la misma clave, lo reemplaza. */
    void guardar(FragmentoConocimiento fragmento, Embedding embedding, String modelo);

    /** Borra los fragmentos cuya clave no está en la lista (fuentes que ya no existen). Devuelve cuántos borró. */
    int eliminarExcepto(Set<String> clavesVigentes);

    /** Los fragmentos más parecidos a la consulta, aplicando los filtros, de mayor a menor similitud. */
    List<ResultadoBusqueda> buscarParecidos(Embedding consulta, FiltroBusqueda filtro);

    long contar();
}
