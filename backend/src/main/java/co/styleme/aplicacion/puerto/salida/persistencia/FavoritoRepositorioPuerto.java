package co.styleme.aplicacion.puerto.salida.persistencia;

import co.styleme.dominio.modelo.comunidad.Favorito;
import co.styleme.dominio.modelo.comunidad.Objetivo;
import java.util.List;

/** Puerto de salida: los favoritos (tabla favorito). */
public interface FavoritoRepositorioPuerto {

    /** Agrega el favorito; si ya existía, no hace nada. */
    void agregar(String sesionId, Objetivo objetivo);

    void quitar(String sesionId, Objetivo objetivo);

    List<Favorito> listar(String sesionId);
}
